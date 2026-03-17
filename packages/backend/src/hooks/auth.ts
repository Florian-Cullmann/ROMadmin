import type { FastifyRequest, FastifyReply } from 'fastify';
import bcrypt from 'bcrypt';

export async function verifyAuth(request: FastifyRequest, reply: FastifyReply) {
  // Check for API key first
  const apiKey = request.headers['x-api-key'] as string | undefined;
  if (apiKey) {
    const user = await request.server.prisma.user.findFirst({
      where: { apiKey },
    });
    if (user) {
      request.user = { id: user.id, username: user.username, role: user.role };
      return;
    }
    return reply.status(401).send({ error: 'INVALID_API_KEY', message: 'Invalid API key' });
  }

  // Fall back to JWT
  try {
    await request.jwtVerify();
  } catch {
    return reply.status(401).send({ error: 'UNAUTHORIZED', message: 'Invalid or expired token' });
  }
}

export async function verifyAdmin(request: FastifyRequest, reply: FastifyReply) {
  if (request.user.role !== 'ADMIN') {
    return reply.status(403).send({ error: 'FORBIDDEN', message: 'Admin access required' });
  }
}
