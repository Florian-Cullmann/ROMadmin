import type { FastifyInstance } from 'fastify';
import type { SyncStatusRequest, SyncAction, SyncStatusGame } from '@romadmin/shared';
import { verifyAuth } from '../hooks/auth.js';
import fs from 'fs/promises';
import path from 'path';

export async function saveRoutes(fastify: FastifyInstance) {
  fastify.addHook('onRequest', verifyAuth);

  // Upload save file
  fastify.post('/upload', async (request, reply) => {
    const data = await request.file();
    if (!data) {
      return reply.status(400).send({ error: 'NO_FILE', message: 'No file uploaded' });
    }

    const gameIdField = data.fields.gameId;
    const gameIdRaw = typeof gameIdField === 'object' && gameIdField !== null && 'value' in gameIdField
      ? (gameIdField as { value: string }).value
      : String(gameIdField ?? '');
    const gameId = parseInt(gameIdRaw);

    const deviceNameField = data.fields.deviceName;
    const deviceName = typeof deviceNameField === 'object' && deviceNameField !== null && 'value' in deviceNameField
      ? (deviceNameField as { value: string }).value
      : (deviceNameField ? String(deviceNameField) : null);

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
        isLatest: true,
      },
    });

    return reply.status(201).send({ ...saveFile, fileSize: saveFile.fileSize.toString() });
  });

  // List saves for a game
  fastify.get<{ Params: { gameId: string } }>('/game/:gameId', async (request) => {
    const where: Record<string, unknown> = { gameId: parseInt(request.params.gameId) };
    // Non-admin users can only see their own saves
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

    // Delete from disk
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

    // Get the latest save for each game for this user
    const serverSaves = await fastify.prisma.saveFile.findMany({
      where: {
        gameId: { in: gameIds },
        userId: request.user.id,
        isLatest: true,
      },
    });

    // Index by gameId for quick lookup
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
        fileName: serverSave.fileName,
        fileSize: serverSave.fileSize.toString(),
        deviceName: serverSave.deviceName,
      };

      if (!localTimestamp) {
        return { gameId, action: 'download' as SyncAction, serverSave: serverSaveInfo };
      }

      const serverTime = serverSave.uploadedAt.getTime();
      const localTime = new Date(localTimestamp).getTime();

      if (serverTime > localTime) {
        return { gameId, action: 'download' as SyncAction, serverSave: serverSaveInfo };
      } else if (localTime > serverTime) {
        return { gameId, action: 'upload' as SyncAction, serverSave: serverSaveInfo };
      } else {
        return { gameId, action: 'in_sync' as SyncAction, serverSave: serverSaveInfo };
      }
    });

    return { games: results };
  });
}
