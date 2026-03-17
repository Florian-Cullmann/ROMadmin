import type { FastifyRequest, FastifyReply } from 'fastify';
import { SETTING_KEYS } from '@romadmin/shared';

let installedCache: boolean | null = null;

export function resetInstallCache() {
  installedCache = null;
}

export async function installGuard(request: FastifyRequest, reply: FastifyReply) {
  const path = request.url;

  // Always allow install status check
  if (path.startsWith('/api/install')) {
    return;
  }

  // Check if installed (with cache)
  if (installedCache === null) {
    const setting = await request.server.prisma.setting.findUnique({
      where: { key: SETTING_KEYS.INSTALLED },
    });
    installedCache = setting?.value === 'true';
  }

  if (!installedCache) {
    return reply.status(503).send({
      error: 'NOT_INSTALLED',
      message: 'Application is not installed yet',
    });
  }
}
