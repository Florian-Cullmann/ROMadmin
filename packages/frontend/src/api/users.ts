import { apiRequest } from './client';
import type { User } from '@romadmin/shared';

export function getUsers() {
  return apiRequest<User[]>('/users');
}

export function createUser(data: { username: string; email: string; password: string; role: string; language: string }) {
  return apiRequest<User>('/users', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function deleteUser(id: number) {
  return apiRequest<{ success: boolean }>(`/users/${id}`, { method: 'DELETE' });
}

export function generateApiKey(userId: number) {
  return apiRequest<{ apiKey: string }>(`/users/${userId}/api-key`, { method: 'POST' });
}
