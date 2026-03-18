import fp from 'fastify-plugin';
import type { FastifyInstance } from 'fastify';

export const authPlugin = fp(async (_fastify: FastifyInstance) => {
  // Session-based auth — no plugin setup needed.
  // Auth is handled by the verifyAuth hook querying the sessions table.
});
