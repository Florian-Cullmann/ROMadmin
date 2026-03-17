import type { FastifyInstance } from 'fastify';
import { verifyAuth } from '../hooks/auth.js';
import { verifyAdmin } from '../hooks/auth.js';
import { platformSchema } from '@romadmin/shared';

export async function platformRoutes(fastify: FastifyInstance) {
  // All platform routes require auth
  fastify.addHook('onRequest', verifyAuth);

  // List all platforms with game counts
  fastify.get('/', async () => {
    const platforms = await fastify.prisma.platform.findMany({
      orderBy: { sortOrder: 'asc' },
      include: { _count: { select: { games: true } } },
    });
    return platforms;
  });

  // Get single platform
  fastify.get<{ Params: { id: string } }>('/:id', async (request, reply) => {
    const platform = await fastify.prisma.platform.findUnique({
      where: { id: parseInt(request.params.id) },
      include: { _count: { select: { games: true } } },
    });
    if (!platform) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Platform not found' });
    }
    return platform;
  });

  // Create platform (admin only)
  fastify.post('/', { onRequest: [verifyAdmin] }, async (request, reply) => {
    const result = platformSchema.safeParse(request.body);
    if (!result.success) {
      return reply.status(400).send({ error: 'VALIDATION_ERROR', message: result.error.errors.map((e) => e.message).join(', ') });
    }
    const platform = await fastify.prisma.platform.create({ data: result.data });
    return reply.status(201).send(platform);
  });

  // Update platform (admin only)
  fastify.put<{ Params: { id: string } }>('/:id', { onRequest: [verifyAdmin] }, async (request, reply) => {
    const result = platformSchema.partial().safeParse(request.body);
    if (!result.success) {
      return reply.status(400).send({ error: 'VALIDATION_ERROR', message: result.error.errors.map((e) => e.message).join(', ') });
    }
    try {
      const platform = await fastify.prisma.platform.update({
        where: { id: parseInt(request.params.id) },
        data: result.data,
      });
      return platform;
    } catch {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Platform not found' });
    }
  });

  // Delete platform (admin only)
  fastify.delete<{ Params: { id: string } }>('/:id', { onRequest: [verifyAdmin] }, async (request, reply) => {
    try {
      await fastify.prisma.platform.delete({ where: { id: parseInt(request.params.id) } });
      return { success: true };
    } catch {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Platform not found' });
    }
  });
}
