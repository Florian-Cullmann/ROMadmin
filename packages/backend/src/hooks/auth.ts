import type { FastifyRequest, FastifyReply } from 'fastify';

export async function verifyAuth(request: FastifyRequest, reply: FastifyReply) {
  const authHeader = request.headers.authorization;
  if (!authHeader?.startsWith('Bearer ')) {
    return reply.status(401).send({ error: 'UNAUTHORIZED', message: 'Missing or invalid authorization header' });
  }

  const token = authHeader.slice(7);
  const session = await request.server.prisma.session.findUnique({
    where: { token },
    include: { user: { select: { id: true, username: true, role: true } } },
  });

  if (!session) {
    return reply.status(401).send({ error: 'UNAUTHORIZED', message: 'Invalid session token' });
  }

  request.user = session.user;
}

export async function verifyAdmin(request: FastifyRequest, reply: FastifyReply) {
  if (request.user.role !== 'ADMIN') {
    return reply.status(403).send({ error: 'FORBIDDEN', message: 'Admin access required' });
  }
}
