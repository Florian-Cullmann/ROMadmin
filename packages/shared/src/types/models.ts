export type UserRole = 'ADMIN' | 'USER';

export interface User {
  id: number;
  username: string;
  email: string;
  role: UserRole;
  language: string;
  createdAt: string;
  updatedAt: string;
}

export interface Platform {
  id: number;
  folderName: string;
  displayName: string;
  slug: string;
  igdbPlatformId: number | null;
  thumbnailUrl: string | null;
  fileExtensions: string;
  sortOrder: number;
  createdAt: string;
  updatedAt: string;
  _count?: { games: number };
}

export interface Game {
  id: number;
  platformId: number;
  fileName: string;
  filePath: string;
  fileSize: string; // BigInt serialized as string
  displayName: string;
  slug: string;
  igdbGameId: number | null;
  description: string | null;
  thumbnailUrl: string | null;
  coverUrl: string | null;
  releaseDate: string | null;
  metadataFetched: boolean;
  createdAt: string;
  updatedAt: string;
  platform?: Platform;
}

export interface SaveFile {
  id: number;
  gameId: number;
  userId: number;
  fileName: string;
  filePath: string;
  fileSize: string;
  deviceName: string | null;
  isLatest: boolean;
  uploadedAt: string;
  game?: Game;
  user?: User;
}

export interface Setting {
  key: string;
  value: string;
}
