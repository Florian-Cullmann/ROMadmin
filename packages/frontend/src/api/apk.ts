import { apiRequest } from './client';

export interface ApkInfo {
  exists: boolean;
  size?: string;
  updatedAt?: string;
}

export function getApkInfo() {
  return apiRequest<ApkInfo>('/downloads/apk/info');
}

export async function uploadApk(file: File) {
  const formData = new FormData();
  formData.append('file', file);

  return apiRequest<{ success: boolean; size: string }>('/downloads/apk', {
    method: 'POST',
    body: formData,
  });
}

export function deleteApk() {
  return apiRequest<{ success: boolean }>('/downloads/apk', { method: 'DELETE' });
}

export function getApkDownloadUrl() {
  return '/api/downloads/apk';
}
