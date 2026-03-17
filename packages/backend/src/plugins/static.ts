import fp from 'fastify-plugin';
import fastifyStatic from '@fastify/static';
import type { FastifyInstance } from 'fastify';
import { fileURLToPath } from 'url';
import path from 'path';

interface StaticPluginOptions {
  frontendPath?: string;
}

const __dirname = path.dirname(fileURLToPath(import.meta.url));

export const staticPlugin = fp(async (fastify: FastifyInstance, opts: StaticPluginOptions) => {
  if (opts.frontendPath) {
    const root = path.resolve(__dirname, '..', '..', opts.frontendPath);
    await fastify.register(fastifyStatic, {
      root,
      wildcard: false,
    });
  }
});
