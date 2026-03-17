import type { FastifyInstance } from 'fastify';
import { installGuard } from '../hooks/install-guard.js';
import { installRoutes } from './install.js';
import { authRoutes } from './auth.js';
import { platformRoutes } from './platforms.js';
import { gameRoutes } from './games.js';
import { saveRoutes } from './saves.js';
import { settingsRoutes } from './settings.js';
import { userRoutes } from './users.js';
import { scannerRoutes } from './scanner.js';
import { downloadRoutes } from './downloads.js';

export async function registerRoutes(fastify: FastifyInstance) {
  // Install guard: block everything except /api/install/* if not installed
  fastify.addHook('onRequest', installGuard);

  // Routes
  await fastify.register(installRoutes, { prefix: '/install' });
  await fastify.register(authRoutes, { prefix: '/auth' });
  await fastify.register(platformRoutes, { prefix: '/platforms' });
  await fastify.register(gameRoutes, { prefix: '/games' });
  await fastify.register(saveRoutes, { prefix: '/saves' });
  await fastify.register(downloadRoutes, { prefix: '/downloads' });
  await fastify.register(settingsRoutes, { prefix: '/settings' });
  await fastify.register(userRoutes, { prefix: '/users' });
  await fastify.register(scannerRoutes, { prefix: '/scanner' });
}
