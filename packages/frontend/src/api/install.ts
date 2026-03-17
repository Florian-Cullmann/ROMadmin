import { apiRequest } from './client';
import type { InstallStatusResponse, InstallRequest } from '@romadmin/shared';

export function getInstallStatus() {
  return apiRequest<InstallStatusResponse>('/install/status');
}

export function checkPath(path: string) {
  return apiRequest<{ valid: boolean; entries: string[] }>('/install/check-path', {
    method: 'POST',
    body: JSON.stringify({ path }),
  });
}

export function completeInstall(data: InstallRequest) {
  return apiRequest<{ success: boolean }>('/install/complete', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}
