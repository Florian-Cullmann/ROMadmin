import type { FastifyInstance } from 'fastify';
import { installSchema } from '@romadmin/shared';
import { SETTING_KEYS, DEFAULT_PLATFORMS } from '@romadmin/shared';
import { hashPassword } from '../utils/password.js';
import { resetInstallCache } from '../hooks/install-guard.js';
import fs from 'fs/promises';

export async function installRoutes(fastify: FastifyInstance) {
  // Check installation status
  fastify.get('/status', async () => {
    const setting = await fastify.prisma.setting.findUnique({
      where: { key: SETTING_KEYS.INSTALLED },
    });
    return { installed: setting?.value === 'true' };
  });

  // Verify ROM path exists
  fastify.post<{ Body: { path: string } }>('/check-path', async (request, reply) => {
    const { path } = request.body;
    try {
      const stat = await fs.stat(path);
      if (!stat.isDirectory()) {
        return reply.status(400).send({ error: 'NOT_DIRECTORY', message: 'Path is not a directory' });
      }
      const entries = await fs.readdir(path);
      return { valid: true, entries };
    } catch {
      return reply.status(400).send({ error: 'INVALID_PATH', message: 'Path does not exist or is not accessible' });
    }
  });

  // Complete installation
  fastify.post('/complete', async (request, reply) => {
    // Check if already installed
    const existing = await fastify.prisma.setting.findUnique({
      where: { key: SETTING_KEYS.INSTALLED },
    });
    if (existing?.value === 'true') {
      return reply.status(400).send({ error: 'ALREADY_INSTALLED', message: 'Application is already installed' });
    }

    // Validate input
    const result = installSchema.safeParse(request.body);
    if (!result.success) {
      return reply.status(400).send({
        error: 'VALIDATION_ERROR',
        message: result.error.errors.map((e) => e.message).join(', '),
      });
    }

    const { admin, romPath, language, igdb } = result.data;

    // Create admin user
    const passwordHash = await hashPassword(admin.password);
    await fastify.prisma.user.create({
      data: {
        username: admin.username,
        email: admin.email,
        passwordHash,
        role: 'ADMIN',
        language,
      },
    });

    // Seed default platforms
    for (const platform of DEFAULT_PLATFORMS) {
      await fastify.prisma.platform.upsert({
        where: { slug: platform.slug },
        update: {},
        create: platform,
      });
    }

    // Save settings
    const settings = [
      { key: SETTING_KEYS.ROM_ROOT_PATH, value: romPath },
      { key: SETTING_KEYS.LANGUAGE, value: language },
      { key: SETTING_KEYS.IGDB_CLIENT_ID, value: igdb.clientId },
      { key: SETTING_KEYS.IGDB_CLIENT_SECRET, value: igdb.clientSecret },
      { key: SETTING_KEYS.INSTALLED, value: 'true' },
    ];

    for (const setting of settings) {
      await fastify.prisma.setting.upsert({
        where: { key: setting.key },
        update: { value: setting.value },
        create: setting,
      });
    }

    // Reset install cache so subsequent requests work
    resetInstallCache();

    return { success: true };
  });
}
