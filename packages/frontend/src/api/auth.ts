import { apiRequest } from './client';
import type { LoginResponse, User } from '@romadmin/shared';

export function login(username: string, password: string, rememberMe?: boolean) {
  return apiRequest<LoginResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ username, password, rememberMe }),
  });
}

export function getMe() {
  return apiRequest<User>('/auth/me');
}

export function logout() {
  return apiRequest<{ success: boolean }>('/auth/logout', { method: 'POST' });
}
