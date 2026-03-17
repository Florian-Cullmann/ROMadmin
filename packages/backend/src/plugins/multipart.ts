import fp from 'fastify-plugin';
import fastifyMultipart from '@fastify/multipart';
import type { FastifyInstance } from 'fastify';

export const multipartPlugin = fp(async (fastify: FastifyInstance) => {
  await fastify.register(fastifyMultipart, {
    limits: {
      fileSize: 200 * 1024 * 1024, // 200MB
    },
  });
});
