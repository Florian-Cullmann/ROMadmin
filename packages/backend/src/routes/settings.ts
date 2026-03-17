import type { FastifyInstance } from 'fastify';
import { verifyAuth, verifyAdmin } from '../hooks/auth.js';

export async function settingsRoutes(fastify: FastifyInstance) {
  fastify.addHook('onRequest', verifyAuth);
  fastify.addHook('onRequest', verifyAdmin);

  // Get all settings
  fastify.get('/', async () => {
    const settings = await fastify.prisma.setting.findMany();
    const result: Record<string, string> = {};
    for (const s of settings) {
      // Don't expose secrets directly
      if (s.key.includes('secret')) {
        result[s.key] = '••••••••';
      } else {
        result[s.key] = s.value;
      }
    }
    return result;
  });

  // Update settings
  fastify.put<{ Body: Record<string, string> }>('/', async (request) => {
    const updates = request.body;
    for (const [key, value] of Object.entries(updates)) {
      await fastify.prisma.setting.upsert({
        where: { key },
        update: { value },
        create: { key, value },
      });
    }
    return { success: true };
  });
}
