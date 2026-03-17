import type { FastifyInstance } from 'fastify';
import type { ScanStatus } from '@romadmin/shared';
import { SETTING_KEYS } from '@romadmin/shared';
import { slugify, fileNameToDisplayName } from '../utils/slug.js';
import fs from 'fs/promises';
import path from 'path';

let scanStatus: ScanStatus = {
  state: 'idle',
  totalFiles: 0,
  processedFiles: 0,
  newGames: 0,
  updatedGames: 0,
};

export function getScanStatus(): ScanStatus {
  return { ...scanStatus };
}

/** Recursively sum the size of all files in a directory */
async function getDirSize(dirPath: string): Promise<bigint> {
  let total = BigInt(0);
  const entries = await fs.readdir(dirPath, { withFileTypes: true });
  for (const entry of entries) {
    const fullPath = path.join(dirPath, entry.name);
    if (entry.isFile()) {
      const stat = await fs.stat(fullPath);
      total += BigInt(stat.size);
    } else if (entry.isDirectory()) {
      total += await getDirSize(fullPath);
    }
  }
  return total;
}

interface ScannedGame {
  name: string;       // entry name (file or folder)
  filePath: string;   // full path
  fileSize: bigint;
  isDirectory: boolean;
}

/** Scan a platform folder for games (files matching extensions + subdirectories) */
async function scanPlatformFolder(
  platformPath: string,
  extensions: string[]
): Promise<ScannedGame[]> {
  const entries = await fs.readdir(platformPath, { withFileTypes: true });
  const games: ScannedGame[] = [];

  for (const entry of entries) {
    // Skip hidden files/folders
    if (entry.name.startsWith('.')) continue;

    const fullPath = path.join(platformPath, entry.name);

    if (entry.isFile()) {
      const ext = path.extname(entry.name).toLowerCase();
      if (extensions.includes(ext)) {
        const stat = await fs.stat(fullPath);
        games.push({
          name: entry.name,
          filePath: fullPath,
          fileSize: BigInt(stat.size),
          isDirectory: false,
        });
      }
    } else if (entry.isDirectory()) {
      // Treat subdirectories as games (e.g., Switch games stored as folders)
      const dirSize = await getDirSize(fullPath);
      games.push({
        name: entry.name,
        filePath: fullPath,
        fileSize: dirSize,
        isDirectory: true,
      });
    }
  }

  return games;
}

export async function scanLibrary(fastify: FastifyInstance) {
  scanStatus = {
    state: 'running',
    totalFiles: 0,
    processedFiles: 0,
    newGames: 0,
    updatedGames: 0,
    startedAt: new Date().toISOString(),
  };

  try {
    const romPathSetting = await fastify.prisma.setting.findUnique({
      where: { key: SETTING_KEYS.ROM_ROOT_PATH },
    });
    if (!romPathSetting) {
      throw new Error('ROM root path not configured');
    }
    const romRoot = romPathSetting.value;

    const platforms = await fastify.prisma.platform.findMany();

    for (const platform of platforms) {
      const platformPath = path.join(romRoot, platform.folderName);

      try {
        await fs.access(platformPath);
      } catch {
        continue;
      }

      const extensions = platform.fileExtensions.split(',').map((e) => e.trim().toLowerCase());
      const games = await scanPlatformFolder(platformPath, extensions);

      scanStatus.totalFiles += games.length;

      for (const game of games) {
        const displayName = fileNameToDisplayName(game.name);
        const slug = slugify(displayName);

        const existing = await fastify.prisma.game.findUnique({
          where: { platformId_fileName: { platformId: platform.id, fileName: game.name } },
        });

        if (existing) {
          if (existing.fileSize !== game.fileSize) {
            await fastify.prisma.game.update({
              where: { id: existing.id },
              data: { fileSize: game.fileSize },
            });
            scanStatus.updatedGames++;
          }
        } else {
          await fastify.prisma.game.create({
            data: {
              platformId: platform.id,
              fileName: game.name,
              filePath: game.filePath,
              fileSize: game.fileSize,
              displayName,
              slug,
            },
          });
          scanStatus.newGames++;
        }

        scanStatus.processedFiles++;
      }
    }

    scanStatus.state = 'completed';
    scanStatus.completedAt = new Date().toISOString();
  } catch (err) {
    scanStatus.state = 'error';
    scanStatus.error = err instanceof Error ? err.message : 'Unknown error';
    scanStatus.completedAt = new Date().toISOString();
    throw err;
  }
}
