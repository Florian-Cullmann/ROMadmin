import { apiRequest } from './client';
import type { Platform } from '@romadmin/shared';

export function getPlatforms() {
  return apiRequest<Platform[]>('/platforms');
}

export function getPlatform(id: number) {
  return apiRequest<Platform>(`/platforms/${id}`);
}
