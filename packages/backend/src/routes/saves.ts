import type { FastifyInstance } from 'fastify';
import type { SyncStatusRequest, SyncAction, SyncStatusGame } from '@romadmin/shared';
import { verifyAuth } from '../hooks/auth.js';
import fs from 'fs/promises';
import path from 'path';

function getMultipartFieldValue(field: unknown): string | null {
  if (typeof field === 'object' && field !== null && 'value' in field) {
    return (field as { value: string }).value;
  }
  return field ? String(field) : null;
}

export async function saveRoutes(fastify: FastifyInstance) {
  fastify.addHook('onRequest', verifyAuth);

  // Upload save file
  fastify.post('/upload', async (request, reply) => {
    const data = await request.file();
    if (!data) {
      return reply.status(400).send({ error: 'NO_FILE', message: 'No file uploaded' });
    }

    const gameId = parseInt(getMultipartFieldValue(data.fields.gameId) ?? '');
    const deviceName = getMultipartFieldValue(data.fields.deviceName);
    const clientTimestampRaw = getMultipartFieldValue(data.fields.clientTimestamp);

    if (!gameId || isNaN(gameId)) {
      return reply.status(400).send({ error: 'INVALID_GAME_ID', message: 'Valid gameId is required' });
    }

    const game = await fastify.prisma.game.findUnique({
      where: { id: gameId },
      include: { platform: true },
    });
    if (!game) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
    }

    // Build save path: /saves/{userId}/{platformSlug}/{gameSlug}/
    const savePath = path.join(
      fastify.env.SAVE_PATH,
      request.user.id.toString(),
      game.platform.slug,
      game.slug
    );
    await fs.mkdir(savePath, { recursive: true });

    const timestamp = Date.now();
    const fileName = `${path.parse(data.filename).name}_${timestamp}${path.parse(data.filename).ext}`;
    const filePath = path.join(savePath, fileName);

    // Write file
    const buffer = await data.toBuffer();
    await fs.writeFile(filePath, buffer);

    // Parse client timestamp if provided
    const clientTimestamp = clientTimestampRaw ? new Date(clientTimestampRaw) : null;

    // Mark previous saves as not latest
    await fastify.prisma.saveFile.updateMany({
      where: { gameId, userId: request.user.id, isLatest: true },
      data: { isLatest: false },
    });

    // Create save record
    const saveFile = await fastify.prisma.saveFile.create({
      data: {
        gameId,
        userId: request.user.id,
        fileName,
        filePath,
        fileSize: BigInt(buffer.length),
        deviceName,
        clientTimestamp: clientTimestamp && !isNaN(clientTimestamp.getTime()) ? clientTimestamp : null,
        isLatest: true,
      },
    });

    return reply.status(201).send({ ...saveFile, fileSize: saveFile.fileSize.toString() });
  });

  // List saves for a game
  fastify.get<{ Params: { gameId: string } }>('/game/:gameId', async (request) => {
    const where: Record<string, unknown> = { gameId: parseInt(request.params.gameId) };
    if (request.user.role !== 'ADMIN') {
      where.userId = request.user.id;
    }

    const saves = await fastify.prisma.saveFile.findMany({
      where,
      orderBy: { uploadedAt: 'desc' },
    });
    return saves.map((s: typeof saves[number]) => ({ ...s, fileSize: s.fileSize.toString() }));
  });

  // Download save file
  fastify.get<{ Params: { saveId: string } }>('/:saveId/download', async (request, reply) => {
    const save = await fastify.prisma.saveFile.findUnique({
      where: { id: parseInt(request.params.saveId) },
    });
    if (!save) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Save file not found' });
    }
    if (request.user.role !== 'ADMIN' && save.userId !== request.user.id) {
      return reply.status(403).send({ error: 'FORBIDDEN', message: 'Access denied' });
    }

    const buffer = await fs.readFile(save.filePath);
    return reply
      .header('Content-Disposition', `attachment; filename="${save.fileName}"`)
      .header('Content-Type', 'application/octet-stream')
      .send(buffer);
  });

  // Delete save file
  fastify.delete<{ Params: { saveId: string } }>('/:saveId', async (request, reply) => {
    const save = await fastify.prisma.saveFile.findUnique({
      where: { id: parseInt(request.params.saveId) },
    });
    if (!save) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Save file not found' });
    }
    if (request.user.role !== 'ADMIN' && save.userId !== request.user.id) {
      return reply.status(403).send({ error: 'FORBIDDEN', message: 'Access denied' });
    }

    try {
      await fs.unlink(save.filePath);
    } catch {
      // File may already be gone
    }

    await fastify.prisma.saveFile.delete({ where: { id: save.id } });
    return { success: true };
  });

  // Save sync status — batch check which games need upload/download
  fastify.post<{ Body: SyncStatusRequest }>('/sync-status', async (request, reply) => {
    const { games: clientGames } = request.body;

    if (!Array.isArray(clientGames) || clientGames.length === 0) {
      return reply.status(400).send({ error: 'VALIDATION_ERROR', message: 'games array is required' });
    }

    const gameIds = clientGames.map((g) => g.gameId);

    const serverSaves = await fastify.prisma.saveFile.findMany({
      where: {
        gameId: { in: gameIds },
        userId: request.user.id,
        isLatest: true,
      },
    });

    type SaveRecord = typeof serverSaves[number];
    const savesByGameId: Record<number, SaveRecord> = {};
    for (const s of serverSaves) {
      savesByGameId[s.gameId] = s;
    }

    const results: SyncStatusGame[] = clientGames.map(({ gameId, localTimestamp }) => {
      const serverSave: SaveRecord | undefined = savesByGameId[gameId];

      if (!serverSave) {
        return { gameId, action: 'no_server_save' as SyncAction };
      }

      const serverSaveInfo = {
        id: serverSave.id,
        uploadedAt: serverSave.uploadedAt.toISOString(),
        clientTimestamp: serverSave.clientTimestamp?.toISOString() ?? null,
        fileName: serverSave.fileName,
        fileSize: serverSave.fileSize.toString(),
        deviceName: serverSave.deviceName,
      };

      if (!localTimestamp) {
        return { gameId, action: 'download' as SyncAction, serverSave: serverSaveInfo };
      }

      // Compare against clientTimestamp (the file's mtime when it was uploaded)
      // rather than uploadedAt (server receipt time) to avoid ping-pong
      const compareTime = (serverSave.clientTimestamp ?? serverSave.uploadedAt).getTime();
      const localTime = new Date(localTimestamp).getTime();

      const action: SyncAction = compareTime > localTime ? 'download'
        : localTime > compareTime ? 'upload'
        : 'in_sync';

      fastify.log.info(
        `sync-status gameId=${gameId}: clientTimestamp=${serverSave.clientTimestamp?.toISOString() ?? 'null'} ` +
        `uploadedAt=${serverSave.uploadedAt.toISOString()} compareTime=${compareTime} ` +
        `localTime=${localTime} diff=${localTime - compareTime}ms → ${action}`
      );

      return { gameId, action, serverSave: serverSaveInfo };
    });

    return { games: results };
  });
}
