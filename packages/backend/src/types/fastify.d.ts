import type { PrismaClient } from '@prisma/client';
import type { Env } from '../env.js';

declare module 'fastify' {
  interface FastifyInstance {
    prisma: PrismaClient;
    env: Env;
  }

  interface FastifyRequest {
    user: {
      id: number;
      username: string;
      role: 'ADMIN' | 'USER';
    };
  }
}
