import fp from 'fastify-plugin';
import fastifyCors from '@fastify/cors';
import type { FastifyInstance } from 'fastify';

export const corsPlugin = fp(async (fastify: FastifyInstance) => {
  await fastify.register(fastifyCors, {
    origin: true,
    credentials: true,
  });
});
