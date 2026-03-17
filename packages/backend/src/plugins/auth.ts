import fp from 'fastify-plugin';
import fastifyJwt from '@fastify/jwt';
import fastifyCookie from '@fastify/cookie';
import type { FastifyInstance } from 'fastify';

interface AuthPluginOptions {
  jwtSecret: string;
  jwtRefreshSecret: string;
}

export const authPlugin = fp(async (fastify: FastifyInstance, opts: AuthPluginOptions) => {
  await fastify.register(fastifyJwt, {
    secret: opts.jwtSecret,
    sign: { expiresIn: '15m' },
  });

  await fastify.register(fastifyCookie);

  // Store refresh secret for manual verification
  fastify.decorate('jwtRefreshSecret', opts.jwtRefreshSecret);
});
