import type { FastifyInstance } from 'fastify';
import { loginSchema } from '@romadmin/shared';
import { verifyPassword } from '../utils/password.js';
import { verifyAuth } from '../hooks/auth.js';
import crypto from 'crypto';

export async function authRoutes(fastify: FastifyInstance) {
  // Login — creates a session and returns a token
  fastify.post('/login', async (request, reply) => {
    const result = loginSchema.safeParse(request.body);
    if (!result.success) {
      return reply.status(400).send({ error: 'VALIDATION_ERROR', message: 'Invalid credentials' });
    }

    const { username, password } = result.data;

    const user = await fastify.prisma.user.findUnique({ where: { username } });
    if (!user) {
      return reply.status(401).send({ error: 'INVALID_CREDENTIALS', message: 'Invalid username or password' });
    }

    const valid = await verifyPassword(password, user.passwordHash);
    if (!valid) {
      return reply.status(401).send({ error: 'INVALID_CREDENTIALS', message: 'Invalid username or password' });
    }

    const { deviceName } = result.data as { username: string; password: string; deviceName?: string };

    const token = crypto.randomBytes(32).toString('hex');
    await fastify.prisma.session.create({
      data: { token, userId: user.id, deviceName: deviceName || null },
    });

    return {
      token,
      user: {
        id: user.id,
        username: user.username,
        email: user.email,
        role: user.role,
        language: user.language,
        createdAt: user.createdAt.toISOString(),
        updatedAt: user.updatedAt.toISOString(),
      },
    };
  });

  // Logout — deletes the current session
  fastify.post('/logout', { onRequest: [verifyAuth] }, async (request) => {
    const authHeader = request.headers.authorization!;
    const token = authHeader.slice(7);
    await fastify.prisma.session.deleteMany({ where: { token } });
    return { success: true };
  });

  // Get current user
  fastify.get('/me', { onRequest: [verifyAuth] }, async (request) => {
    const user = await fastify.prisma.user.findUnique({
      where: { id: request.user.id },
      select: { id: true, username: true, email: true, role: true, language: true, createdAt: true, updatedAt: true },
    });
    return user;
  });
}
