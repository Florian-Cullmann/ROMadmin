export const SETTING_KEYS = {
  INSTALLED: 'installed',
  ROM_ROOT_PATH: 'rom_root_path',
  LANGUAGE: 'language',
  IGDB_CLIENT_ID: 'igdb_client_id',
  IGDB_CLIENT_SECRET: 'igdb_client_secret',
} as const;

export type SettingKey = (typeof SETTING_KEYS)[keyof typeof SETTING_KEYS];
