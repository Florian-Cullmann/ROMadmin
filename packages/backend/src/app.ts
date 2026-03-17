import Fastify from 'fastify';
import type { Env } from './env.js';
import { prismaPlugin } from './plugins/prisma.js';
import { authPlugin } from './plugins/auth.js';
import { corsPlugin } from './plugins/cors.js';
import { multipartPlugin } from './plugins/multipart.js';
import { staticPlugin } from './plugins/static.js';
import { registerRoutes } from './routes/index.js';

export async function buildApp(env: Env) {
  const app = Fastify({
    logger: {
      level: env.NODE_ENV === 'development' ? 'debug' : 'info',
    },
  });

  // Decorate with env
  app.decorate('env', env);

  // Register plugins
  await app.register(corsPlugin);
  await app.register(prismaPlugin);
  await app.register(authPlugin, { jwtSecret: env.JWT_SECRET, jwtRefreshSecret: env.JWT_REFRESH_SECRET });
  await app.register(multipartPlugin);
  await app.register(staticPlugin, { frontendPath: env.NODE_ENV === 'production' ? '../frontend/dist' : undefined });

  // Register routes
  await app.register(registerRoutes, { prefix: '/api' });

  // In production, serve frontend for all non-API routes
  if (env.NODE_ENV === 'production') {
    app.setNotFoundHandler(async (_request, reply) => {
      return reply.sendFile('index.html');
    });
  }

  return app;
}
