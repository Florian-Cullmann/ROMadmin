import type { FastifyInstance } from 'fastify';
import { verifyAuth, verifyAdmin } from '../hooks/auth.js';
import fs from 'fs';
import fsPromises from 'fs/promises';
import path from 'path';
import { pack } from 'tar-stream';
import { pipeline } from 'stream/promises';
import { Readable } from 'stream';

const APK_FILENAME = 'romadmin.apk';

export async function downloadRoutes(fastify: FastifyInstance) {
  // --- APK endpoints (public download, admin upload) ---

  // Public APK download (no auth required — device needs to grab this easily)
  fastify.get('/apk', async (request, reply) => {
    const apkPath = path.join(fastify.env.MEDIA_PATH, APK_FILENAME);
    try {
      await fsPromises.access(apkPath);
    } catch {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'No APK uploaded yet' });
    }
    const stat = await fsPromises.stat(apkPath);
    const stream = fs.createReadStream(apkPath);
    return reply
      .header('Content-Length', stat.size.toString())
      .header('Content-Type', 'application/vnd.android.package-archive')
      .header('Content-Disposition', `attachment; filename="${APK_FILENAME}"`)
      .send(stream);
  });

  // APK info (no auth — so device setup can check if APK exists)
  fastify.get('/apk/info', async (request, reply) => {
    const apkPath = path.join(fastify.env.MEDIA_PATH, APK_FILENAME);
    try {
      const stat = await fsPromises.stat(apkPath);
      return { exists: true, size: stat.size.toString(), updatedAt: stat.mtime.toISOString() };
    } catch {
      return { exists: false };
    }
  });

  // Upload APK (admin only)
  fastify.post('/apk', { onRequest: [verifyAuth, verifyAdmin] }, async (request, reply) => {
    const data = await request.file();
    if (!data) {
      return reply.status(400).send({ error: 'NO_FILE', message: 'No file uploaded' });
    }
    if (!data.filename.endsWith('.apk')) {
      return reply.status(400).send({ error: 'INVALID_FILE', message: 'Only .apk files are allowed' });
    }

    const apkDir = fastify.env.MEDIA_PATH;
    await fsPromises.mkdir(apkDir, { recursive: true });
    const apkPath = path.join(apkDir, APK_FILENAME);

    const buffer = await data.toBuffer();
    await fsPromises.writeFile(apkPath, buffer);

    return { success: true, size: buffer.length.toString() };
  });

  // Delete APK (admin only)
  fastify.delete('/apk', { onRequest: [verifyAuth, verifyAdmin] }, async (request, reply) => {
    const apkPath = path.join(fastify.env.MEDIA_PATH, APK_FILENAME);
    try {
      await fsPromises.unlink(apkPath);
    } catch {
      // already gone
    }
    return { success: true };
  });

  // --- Game download endpoints (auth required per-route) ---

  // Download a game file (supports Range headers for resume)
  fastify.get<{ Params: { id: string } }>('/games/:id', { onRequest: [verifyAuth] }, async (request, reply) => {
    const game = await fastify.prisma.game.findUnique({
      where: { id: parseInt(request.params.id) },
    });
    if (!game) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Game not found' });
    }

    try {
      await fsPromises.access(game.filePath);
    } catch {
      return reply.status(404).send({ error: 'FILE_NOT_FOUND', message: 'Game file not found on disk' });
    }

    // Directory-based games: stream as tar archive
    if (game.isDirectory) {
      return streamDirectoryGame(game.filePath, game.fileName, reply);
    }

    // Single file: stream with Range support
    return streamFileGame(game.filePath, game.fileName, request, reply);
  });

  // Platform download manifest
  fastify.get<{ Params: { id: string } }>('/platforms/:id/manifest', { onRequest: [verifyAuth] }, async (request, reply) => {
    const platform = await fastify.prisma.platform.findUnique({
      where: { id: parseInt(request.params.id) },
    });
    if (!platform) {
      return reply.status(404).send({ error: 'NOT_FOUND', message: 'Platform not found' });
    }

    const games = await fastify.prisma.game.findMany({
      where: { platformId: platform.id },
      orderBy: { displayName: 'asc' },
      select: {
        id: true,
        fileName: true,
        fileSize: true,
        displayName: true,
        thumbnailUrl: true,
        isDirectory: true,
      },
    });

    const totalSize = games.reduce((sum: bigint, g: typeof games[number]) => sum + g.fileSize, BigInt(0));

    return {
      platform: {
        id: platform.id,
        folderName: platform.folderName,
        displayName: platform.displayName,
      },
      games: games.map((g: typeof games[number]) => ({
        ...g,
        fileSize: g.fileSize.toString(),
      })),
      totalSize: totalSize.toString(),
    };
  });
}

/** Stream a single-file game with Range header support */
async function streamFileGame(
  filePath: string,
  fileName: string,
  request: { headers: Record<string, string | string[] | undefined> },
  reply: {
    status: (code: number) => typeof reply;
    header: (name: string, value: string) => typeof reply;
    send: (data: unknown) => unknown;
  }
) {
  const stat = await fsPromises.stat(filePath);
  const fileSize = stat.size;
  const etag = `"${stat.mtime.getTime().toString(16)}-${fileSize.toString(16)}"`;

  const rangeHeader = request.headers['range'] as string | undefined;

  if (rangeHeader) {
    const match = rangeHeader.match(/bytes=(\d+)-(\d*)/);
    if (!match) {
      return reply.status(416)
        .header('Content-Range', `bytes */${fileSize}`)
        .send({ error: 'INVALID_RANGE', message: 'Invalid range header' });
    }

    const start = parseInt(match[1]);
    const end = match[2] ? parseInt(match[2]) : fileSize - 1;

    if (start >= fileSize || end >= fileSize || start > end) {
      return reply.status(416)
        .header('Content-Range', `bytes */${fileSize}`)
        .send({ error: 'INVALID_RANGE', message: 'Range not satisfiable' });
    }

    const chunkSize = end - start + 1;
    const stream = fs.createReadStream(filePath, { start, end });

    return reply.status(206)
      .header('Content-Range', `bytes ${start}-${end}/${fileSize}`)
      .header('Content-Length', chunkSize.toString())
      .header('Content-Type', 'application/octet-stream')
      .header('Content-Disposition', `attachment; filename="${encodeURIComponent(fileName)}"`)
      .header('Accept-Ranges', 'bytes')
      .header('ETag', etag)
      .send(stream);
  }

  // Full file download
  const stream = fs.createReadStream(filePath);
  return reply
    .header('Content-Length', fileSize.toString())
    .header('Content-Type', 'application/octet-stream')
    .header('Content-Disposition', `attachment; filename="${encodeURIComponent(fileName)}"`)
    .header('Accept-Ranges', 'bytes')
    .header('ETag', etag)
    .send(stream);
}

/** Stream a directory-based game as a tar archive */
async function streamDirectoryGame(
  dirPath: string,
  dirName: string,
  reply: {
    header: (name: string, value: string) => typeof reply;
    send: (data: unknown) => unknown;
  }
) {
  const tarPack = pack();

  // Walk directory and add files to tar
  const addDirToTar = async (currentPath: string, basePath: string) => {
    const entries = await fsPromises.readdir(currentPath, { withFileTypes: true });
    for (const entry of entries) {
      const fullPath = path.join(currentPath, entry.name);
      const relativePath = path.relative(basePath, fullPath);

      if (entry.isFile()) {
        const stat = await fsPromises.stat(fullPath);
        const fileStream = fs.createReadStream(fullPath);
        const entry$ = tarPack.entry({ name: relativePath, size: stat.size });
        await pipeline(fileStream, entry$);
      } else if (entry.isDirectory()) {
        await addDirToTar(fullPath, basePath);
      }
    }
  };

  // Start packing in the background
  (async () => {
    try {
      await addDirToTar(dirPath, dirPath);
      tarPack.finalize();
    } catch {
      tarPack.destroy();
    }
  })();

  return reply
    .header('Content-Type', 'application/x-tar')
    .header('Content-Disposition', `attachment; filename="${encodeURIComponent(dirName)}.tar"`)
    .header('X-Original-Format', 'directory')
    .send(tarPack);
}
