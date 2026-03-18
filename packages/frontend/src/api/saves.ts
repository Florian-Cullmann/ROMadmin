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

export async function downloadSave(saveId: number) {
  const { token } = (await import('../stores/auth')).useAuthStore.getState();
  const response = await fetch(`/api/saves/${saveId}/download`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!response.ok) throw new Error('Download failed');
  const blob = await response.blob();
  const disposition = response.headers.get('Content-Disposition');
  const match = disposition?.match(/filename="(.+?)"/);
  const filename = match?.[1] || 'save';
  const url = URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  a.click();
  URL.revokeObjectURL(url);
}
