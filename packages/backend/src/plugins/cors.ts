import fp from 'fastify-plugin';
import fastifyCors from '@fastify/cors';
import type { FastifyInstance } from 'fastify';

export const corsPlugin = fp(async (fastify: FastifyInstance) => {
  await fastify.register(fastifyCors, {
    origin: fastify.env.CORS_ORIGIN ?? true,
    credentials: true,
  });
});
