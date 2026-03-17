import type { User } from './models.js';

export interface ApiError {
  error: string;
  message: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface RefreshRequest {
  refreshToken: string;
}

export interface RefreshResponse {
  accessToken: string;
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
