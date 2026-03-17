import type { FastifyInstance } from 'fastify';
import { verifyAuth, verifyAdmin } from '../hooks/auth.js';
import { fetchGameMetadata, searchIGDB, applyIGDBResult } from '../services/igdb.js';
import type { IGDBSearchResult } from '../services/igdb.js';

export async function gameRoutes(fastify: FastifyInstance) {
  fastify.addHook('onRequest', verifyAuth);

  // List games for a platform (via query param or nested route)
  fastify.get<{
    Querystring: { platformId?: string; search?: string; sort?: string; order?: string; page?: string; limit?: string };
  }>('/', async (request) => {
    const { platformId, search, sort = 'displayName', order = 'asc', page = '1', limit = '24' } = request.query;

    const pageNum = Math.max(1, parseInt(page));
    const limitNum = Math.min(100, Math.max(1, parseInt(limit)));

    const where: Record<string, unknown> = {};
    if (platformId) where.platformId = parseInt(platformId);
    if (search) where.displayName = { contains: search };

    const orderBy: Record<string, string> = {};
    const allowedSorts = ['displayName', 'releaseDate', 'fileSize', 'createdAt'];
    if (allowedSorts.includes(sort)) {
      orderBy[sort] = order === 'desc' ? 'desc' : 'asc';
    }

    const [data, total] = await Promise.all([
      fastify.prisma.game.findMany({
        where,
        orderBy,
        skip: (pageNum - 1) * limitNum,
        take: limitNum,
        include: { platform: true },
      }),
      fastify.prisma.game.count({ where }),
    ]);

    return {
      data: data.map((g) => ({ ...g, fileSize: g.fileSize.toString() })),
      total,
      page: pageNum,
      limit: limitNum,
      totalPages: Math.ceil(total / limitNum),
    };
  });

  // Get single game
  fastify.get<{ Params: { id: string } }>('/:id', async (request, reply) => {
    const game = await fastify.prisma.game.findUnique({
      where: { id: parseInt(request.params.id) },
      include: { platform: true },
    });
    if (!game) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
    }
    return { ...game, fileSize: game.fileSize.toString() };
  });

  // Update game metadata (admin only)
  fastify.put<{ Params: { id: string }; Body: { displayName?: string; description?: string } }>(
    '/:id',
    { onRequest: [verifyAdmin] },
    async (request, reply) => {
      const { displayName, description } = request.body;
      try {
        const game = await fastify.prisma.game.update({
          where: { id: parseInt(request.params.id) },
          data: { ...(displayName && { displayName }), ...(description !== undefined && { description }) },
        });
        return { ...game, fileSize: game.fileSize.toString() };
      } catch {
        return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
      }
    }
  );

  // Trigger IGDB metadata fetch for a single game
  fastify.post<{ Params: { id: string } }>('/:id/fetch-metadata', { onRequest: [verifyAdmin] }, async (request, reply) => {
    const game = await fastify.prisma.game.findUnique({
      where: { id: parseInt(request.params.id) },
      include: { platform: true },
    });
    if (!game) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
    }

    const updated = await fetchGameMetadata(fastify, game.id);
    if (!updated) {
      return reply.status(404).send({ error: 'NO_MATCH', message: 'No IGDB match found for this game' });
    }
    return { ...updated, fileSize: updated.fileSize.toString() };
  });

  // Search IGDB manually
  fastify.get<{ Params: { id: string }; Querystring: { q: string } }>(
    '/:id/search-igdb',
    { onRequest: [verifyAdmin] },
    async (request, reply) => {
      const game = await fastify.prisma.game.findUnique({
        where: { id: parseInt(request.params.id) },
        include: { platform: true },
      });
      if (!game) {
        return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
      }

      const query = request.query.q || game.displayName;
      const results = await searchIGDB(fastify, query, game.platform.igdbPlatformId);
      return results;
    }
  );

  // Apply a chosen IGDB result to a game
  fastify.post<{ Params: { id: string }; Body: IGDBSearchResult }>(
    '/:id/apply-igdb',
    { onRequest: [verifyAdmin] },
    async (request, reply) => {
      const game = await fastify.prisma.game.findUnique({
        where: { id: parseInt(request.params.id) },
      });
      if (!game) {
        return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
      }

      const updated = await applyIGDBResult(fastify, game.id, request.body);
      return { ...updated, fileSize: updated.fileSize.toString() };
    }
  );
}
