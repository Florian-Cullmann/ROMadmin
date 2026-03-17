import type { FastifyInstance } from 'fastify';
import { verifyAuth, verifyAdmin } from '../hooks/auth.js';
import { createUserSchema, updateUserSchema } from '@romadmin/shared';
import { hashPassword } from '../utils/password.js';
import crypto from 'crypto';

export async function userRoutes(fastify: FastifyInstance) {
  fastify.addHook('onRequest', verifyAuth);
  fastify.addHook('onRequest', verifyAdmin);

  // List users
  fastify.get('/', async () => {
    const users = await fastify.prisma.user.findMany({
      select: { id: true, username: true, email: true, role: true, language: true, createdAt: true, updatedAt: true },
      orderBy: { createdAt: 'asc' },
    });
    return users;
  });

  // Create user
  fastify.post('/', async (request, reply) => {
    const result = createUserSchema.safeParse(request.body);
    if (!result.success) {
      return reply.status(400).send({ error: 'VALIDATION_ERROR', message: result.error.errors.map((e) => e.message).join(', ') });
    }

    const { password, ...rest } = result.data;
    const passwordHash = await hashPassword(password);

    try {
      const user = await fastify.prisma.user.create({
        data: { ...rest, passwordHash },
        select: { id: true, username: true, email: true, role: true, language: true, createdAt: true, updatedAt: true },
      });
      return reply.status(201).send(user);
    } catch {
      return reply.status(409).send({ error: 'CONFLICT', message: 'Username or email already exists' });
    }
  });

  // Update user
  fastify.put<{ Params: { id: string } }>('/:id', async (request, reply) => {
    const result = updateUserSchema.safeParse(request.body);
    if (!result.success) {
      return reply.status(400).send({ error: 'VALIDATION_ERROR', message: result.error.errors.map((e) => e.message).join(', ') });
    }

    const { password, ...rest } = result.data;
    const data: Record<string, unknown> = { ...rest };
    if (password) {
      data.passwordHash = await hashPassword(password);
    }

    try {
      const user = await fastify.prisma.user.update({
        where: { id: parseInt(request.params.id) },
        data,
        select: { id: true, username: true, email: true, role: true, language: true, createdAt: true, updatedAt: true },
      });
      return user;
    } catch {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'User not found' });
    }
  });

  // Delete user
  fastify.delete<{ Params: { id: string } }>('/:id', async (request, reply) => {
    const userId = parseInt(request.params.id);
    if (userId === request.user.id) {
      return reply.status(400).send({ error: 'SELF_DELETE', message: 'Cannot delete your own account' });
    }
    try {
      await fastify.prisma.user.delete({ where: { id: userId } });
      return { success: true };
    } catch {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'User not found' });
    }
  });

  // Generate API key for user
  fastify.post<{ Params: { id: string } }>('/:id/api-key', async (request, reply) => {
    const apiKey = crypto.randomBytes(32).toString('hex');
    try {
      await fastify.prisma.user.update({
        where: { id: parseInt(request.params.id) },
        data: { apiKey },
      });
      // Return the raw key - it's only shown once
      return { apiKey };
    } catch {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'User not found' });
    }
  });
}
