import { apiRequest } from './client';
import type { Game, PaginatedResponse } from '@romadmin/shared';

interface GameListParams {
  platformId?: number;
  search?: string;
  sort?: string;
  order?: string;
  page?: number;
  limit?: number;
}

export function getGames(params: GameListParams) {
  const query = new URLSearchParams();
  if (params.platformId) query.set('platformId', params.platformId.toString());
  if (params.search) query.set('search', params.search);
  if (params.sort) query.set('sort', params.sort);
  if (params.order) query.set('order', params.order);
  if (params.page) query.set('page', params.page.toString());
  if (params.limit) query.set('limit', params.limit.toString());
  return apiRequest<PaginatedResponse<Game>>(`/games?${query.toString()}`);
}

export function getGame(id: number) {
  return apiRequest<Game>(`/games/${id}`);
}

export function fetchGameMetadata(id: number) {
  return apiRequest<Game>(`/games/${id}/fetch-metadata`, { method: 'POST' });
}

export interface IGDBSearchResult {
  igdbId: number;
  name: string;
  summary?: string;
  releaseDate?: string;
  coverUrl?: string;
  thumbnailUrl?: string;
}

export function searchIGDB(gameId: number, query: string) {
  return apiRequest<IGDBSearchResult[]>(`/games/${gameId}/search-igdb?q=${encodeURIComponent(query)}`);
}

export function applyIGDBResult(gameId: number, result: IGDBSearchResult) {
  return apiRequest<Game>(`/games/${gameId}/apply-igdb`, {
    method: 'POST',
    body: JSON.stringify(result),
  });
}
