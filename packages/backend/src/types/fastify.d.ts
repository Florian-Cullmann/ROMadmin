import type { PrismaClient } from '@prisma/client';
import type { Env } from '../env.js';

declare module 'fastify' {
  interface FastifyInstance {
    prisma: PrismaClient;
    env: Env;
    jwtRefreshSecret: string;
  }
}

declare module '@fastify/jwt' {
  interface FastifyJWT {
    payload: {
      id: number;
      username: string;
      role: 'ADMIN' | 'USER';
    };
    user: {
      id: number;
      username: string;
      role: 'ADMIN' | 'USER';
    };
  }
}
