import type { FastifyInstance } from 'fastify';
import { loginSchema } from '@romadmin/shared';
import { verifyPassword } from '../utils/password.js';
import { verifyAuth } from '../hooks/auth.js';
import jwt from 'jsonwebtoken';
import crypto from 'crypto';

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

    const { rememberMe } = result.data as { username: string; password: string; rememberMe?: boolean };
    const payload = { id: user.id, username: user.username, role: user.role };
    const accessToken = fastify.jwt.sign(payload);
    const refreshToken = jwt.sign(payload, fastify.jwtRefreshSecret, { expiresIn: rememberMe ? '30d' : '7d' });

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

      // Verify user still exists
      const user = await fastify.prisma.user.findUnique({ where: { id: decoded.id } });
      if (!user) {
        return reply.status(401).send({ error: 'INVALID_TOKEN', message: 'User no longer exists' });
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

  // Generate API key for current user (self-service)
  // Returns existing key if one exists, so multiple devices can share the same key
  fastify.post('/generate-api-key', { onRequest: [verifyAuth] }, async (request) => {
    const user = await fastify.prisma.user.findUnique({
      where: { id: request.user.id },
      select: { apiKey: true },
    });

    if (user?.apiKey) {
      return { apiKey: user.apiKey };
    }

    const apiKey = crypto.randomBytes(32).toString('hex');
    await fastify.prisma.user.update({
      where: { id: request.user.id },
      data: { apiKey },
    });
    return { apiKey };
  });
}
