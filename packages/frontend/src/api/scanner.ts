import { apiRequest } from './client';
import type { ScanStatus } from '@romadmin/shared';

export function startScan() {
  return apiRequest<{ message: string }>('/scanner/run', { method: 'POST' });
}

export function getScanStatus() {
  return apiRequest<ScanStatus>('/scanner/status');
}
