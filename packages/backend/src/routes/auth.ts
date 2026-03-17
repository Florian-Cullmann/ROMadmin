import type { FastifyInstance } from 'fastify';
import { loginSchema } from '@romadmin/shared';
import { verifyPassword } from '../utils/password.js';
import { verifyAuth } from '../hooks/auth.js';
import jwt from 'jsonwebtoken';

export async function authRoutes(fastify: FastifyInstance) {
  // Login
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

    const payload = { id: user.id, username: user.username, role: user.role };
    const accessToken = fastify.jwt.sign(payload);
    const refreshToken = jwt.sign(payload, fastify.jwtRefreshSecret, { expiresIn: '7d' });

    // Store refresh token
    await fastify.prisma.user.update({
      where: { id: user.id },
      data: { refreshToken },
    });

    return {
      accessToken,
      refreshToken,
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

  // Refresh token
  fastify.post<{ Body: { refreshToken: string } }>('/refresh', async (request, reply) => {
    const { refreshToken } = request.body;
    if (!refreshToken) {
      return reply.status(400).send({ error: 'MISSING_TOKEN', message: 'Refresh token is required' });
    }

    try {
      const decoded = jwt.verify(refreshToken, fastify.jwtRefreshSecret) as { id: number; username: string; role: string };

      // Verify token is still stored (not revoked)
      const user = await fastify.prisma.user.findUnique({ where: { id: decoded.id } });
      if (!user || user.refreshToken !== refreshToken) {
        return reply.status(401).send({ error: 'INVALID_TOKEN', message: 'Refresh token is invalid or revoked' });
      }

      const payload = { id: user.id, username: user.username, role: user.role };
      const accessToken = fastify.jwt.sign(payload);

      return { accessToken };
    } catch {
      return reply.status(401).send({ error: 'INVALID_TOKEN', message: 'Refresh token is invalid or expired' });
    }
  });

  // Logout
  fastify.post('/logout', { onRequest: [verifyAuth] }, async (request) => {
    await fastify.prisma.user.update({
      where: { id: request.user.id },
      data: { refreshToken: null },
    });
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
