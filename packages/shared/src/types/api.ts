import type { User } from './models.js';

export interface ApiError {
  error: string;
  message: string;
}

export interface LoginRequest {
  username: string;
  password: string;
  deviceName?: string;
}

export interface LoginResponse {
  token: string;
  user: User;
}

export interface InstallStatusResponse {
  installed: boolean;
}

export interface InstallRequest {
  admin: {
    username: string;
    email: string;
    password: string;
  };
  romPath: string;
  language: string;
  igdb: {
    clientId: string;
    clientSecret: string;
  };
}

export interface PaginatedResponse<T> {
  data: T[];
  total: number;
  page: number;
  limit: number;
  totalPages: number;
}

export interface ScanStatus {
  state: 'idle' | 'running' | 'completed' | 'error';
  totalFiles: number;
  processedFiles: number;
  newGames: number;
  updatedGames: number;
  startedAt?: string;
  completedAt?: string;
  error?: string;
}

// Download manifest for bulk download planning
export interface PlatformManifest {
  platform: {
    id: number;
    folderName: string;
    displayName: string;
  };
  games: Array<{
    id: number;
    fileName: string;
    fileSize: string;
    displayName: string;
    thumbnailUrl: string | null;
    isDirectory: boolean;
  }>;
  totalSize: string;
}

// Save sync protocol types
export interface SyncStatusRequest {
  deviceName: string;
  games: Array<{
    gameId: number;
    localTimestamp: string | null;
  }>;
}

export type SyncAction = 'download' | 'upload' | 'in_sync' | 'no_server_save';

export interface SyncStatusGame {
  gameId: number;
  action: SyncAction;
  serverSave?: {
    id: number;
    uploadedAt: string;
    clientTimestamp: string | null;
    fileName: string;
    fileSize: string;
    deviceName: string | null;
  };
}

export interface SyncStatusResponse {
  games: SyncStatusGame[];
}
