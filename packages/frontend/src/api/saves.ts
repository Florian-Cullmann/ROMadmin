import { apiRequest } from './client';
import type { SaveFile } from '@romadmin/shared';

export function getSaves(gameId: number) {
  return apiRequest<SaveFile[]>(`/saves/game/${gameId}`);
}

export async function uploadSave(gameId: number, file: File, deviceName?: string) {
  const formData = new FormData();
  formData.append('file', file);
  formData.append('gameId', gameId.toString());
  if (deviceName) formData.append('deviceName', deviceName);

  return apiRequest<SaveFile>('/saves/upload', {
    method: 'POST',
    body: formData,
  });
}

export function deleteSave(saveId: number) {
  return apiRequest<{ success: boolean }>(`/saves/${saveId}`, { method: 'DELETE' });
}

export function downloadSaveUrl(saveId: number) {
  return `/api/saves/${saveId}/download`;
}
