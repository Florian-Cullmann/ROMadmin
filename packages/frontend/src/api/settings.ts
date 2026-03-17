import { apiRequest } from './client';

export function getSettings() {
  return apiRequest<Record<string, string>>('/settings');
}

export function updateSettings(data: Record<string, string>) {
  return apiRequest<{ success: boolean }>('/settings', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}
