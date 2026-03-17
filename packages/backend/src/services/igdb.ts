import type { FastifyInstance } from 'fastify';
import { SETTING_KEYS } from '@romadmin/shared';

interface TwitchToken {
  accessToken: string;
  expiresAt: number;
}

let tokenCache: TwitchToken | null = null;

async function getAccessToken(clientId: string, clientSecret: string): Promise<string> {
  if (tokenCache && tokenCache.expiresAt > Date.now()) {
    return tokenCache.accessToken;
  }

  const response = await fetch('https://id.twitch.tv/oauth2/token', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      client_id: clientId,
      client_secret: clientSecret,
      grant_type: 'client_credentials',
    }),
  });

  if (!response.ok) {
    throw new Error(`Twitch OAuth failed: ${response.statusText}`);
  }

  const data = (await response.json()) as { access_token: string; expires_in: number };
  tokenCache = {
    accessToken: data.access_token,
    expiresAt: Date.now() + data.expires_in * 1000 - 60000, // 1 min buffer
  };

  return tokenCache.accessToken;
}

interface IGDBGame {
  id: number;
  name: string;
  summary?: string;
  first_release_date?: number;
  cover?: {
    image_id: string;
  };
}

async function igdbRequest(clientId: string, accessToken: string, endpoint: string, body: string): Promise<unknown> {
  const response = await fetch(`https://api.igdb.com/v4/${endpoint}`, {
    method: 'POST',
    headers: {
      'Client-ID': clientId,
      Authorization: `Bearer ${accessToken}`,
      'Content-Type': 'text/plain',
    },
    body,
  });

  if (!response.ok) {
    throw new Error(`IGDB request failed: ${response.statusText}`);
  }

  return response.json();
}

export async function fetchGameMetadata(fastify: FastifyInstance, gameId: number) {
  const game = await fastify.prisma.game.findUnique({
    where: { id: gameId },
    include: { platform: true },
  });
  if (!game || !game.platform.igdbPlatformId) return null;

  const clientId = (await fastify.prisma.setting.findUnique({ where: { key: SETTING_KEYS.IGDB_CLIENT_ID } }))?.value;
  const clientSecret = (await fastify.prisma.setting.findUnique({ where: { key: SETTING_KEYS.IGDB_CLIENT_SECRET } }))?.value;
  if (!clientId || !clientSecret) return null;

  const accessToken = await getAccessToken(clientId, clientSecret);
  const query = `search "${game.displayName}"; fields name,summary,first_release_date,cover.image_id; where platforms = (${game.platform.igdbPlatformId}); limit 5;`;

  const results = (await igdbRequest(clientId, accessToken, 'games', query)) as IGDBGame[];
  if (!results.length) return null;

  // Pick best match (first result from IGDB search)
  const match = results[0];

  const updateData: Record<string, unknown> = {
    igdbGameId: match.id,
    metadataFetched: true,
  };

  if (match.summary) updateData.description = match.summary;
  if (match.first_release_date) updateData.releaseDate = new Date(match.first_release_date * 1000);
  if (match.cover?.image_id) {
    updateData.coverUrl = `https://images.igdb.com/igdb/image/upload/t_cover_big/${match.cover.image_id}.jpg`;
    updateData.thumbnailUrl = `https://images.igdb.com/igdb/image/upload/t_thumb/${match.cover.image_id}.jpg`;
  }

  const updated = await fastify.prisma.game.update({
    where: { id: gameId },
    data: updateData,
  });

  return updated;
}

export interface IGDBSearchResult {
  igdbId: number;
  name: string;
  summary?: string;
  releaseDate?: string;
  coverUrl?: string;
  thumbnailUrl?: string;
}

export async function searchIGDB(
  fastify: FastifyInstance,
  searchQuery: string,
  igdbPlatformId?: number | null,
): Promise<IGDBSearchResult[]> {
  const clientId = (await fastify.prisma.setting.findUnique({ where: { key: SETTING_KEYS.IGDB_CLIENT_ID } }))?.value;
  const clientSecret = (await fastify.prisma.setting.findUnique({ where: { key: SETTING_KEYS.IGDB_CLIENT_SECRET } }))?.value;
  if (!clientId || !clientSecret) return [];

  const accessToken = await getAccessToken(clientId, clientSecret);
  const platformFilter = igdbPlatformId ? `where platforms = (${igdbPlatformId});` : '';
  const query = `search "${searchQuery.replace(/"/g, '\\"')}"; fields name,summary,first_release_date,cover.image_id; ${platformFilter} limit 10;`;

  const results = (await igdbRequest(clientId, accessToken, 'games', query)) as IGDBGame[];

  return results.map((r) => ({
    igdbId: r.id,
    name: r.name,
    summary: r.summary,
    releaseDate: r.first_release_date ? new Date(r.first_release_date * 1000).toISOString() : undefined,
    coverUrl: r.cover?.image_id
      ? `https://images.igdb.com/igdb/image/upload/t_cover_big/${r.cover.image_id}.jpg`
      : undefined,
    thumbnailUrl: r.cover?.image_id
      ? `https://images.igdb.com/igdb/image/upload/t_thumb/${r.cover.image_id}.jpg`
      : undefined,
  }));
}

export async function applyIGDBResult(fastify: FastifyInstance, gameId: number, result: IGDBSearchResult) {
  const updateData: Record<string, unknown> = {
    igdbGameId: result.igdbId,
    metadataFetched: true,
  };
  if (result.summary) updateData.description = result.summary;
  if (result.releaseDate) updateData.releaseDate = new Date(result.releaseDate);
  if (result.coverUrl) updateData.coverUrl = result.coverUrl;
  if (result.thumbnailUrl) updateData.thumbnailUrl = result.thumbnailUrl;

  return fastify.prisma.game.update({
    where: { id: gameId },
    data: updateData,
  });
}

export async function fetchAllMetadata(fastify: FastifyInstance) {
  const games = await fastify.prisma.game.findMany({
    where: { metadataFetched: false },
    include: { platform: true },
  });

  let fetched = 0;
  for (const game of games) {
    try {
      await fetchGameMetadata(fastify, game.id);
      fetched++;
      // Rate limit: 4 requests per second
      await new Promise((resolve) => setTimeout(resolve, 260));
    } catch (err) {
      fastify.log.error(err, `Failed to fetch metadata for game ${game.id}`);
    }
  }

  return { fetched, total: games.length };
}
