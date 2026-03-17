export interface DefaultPlatform {
  folderName: string;
  displayName: string;
  slug: string;
  igdbPlatformId: number;
  fileExtensions: string;
  sortOrder: number;
}

export const DEFAULT_PLATFORMS: DefaultPlatform[] = [
  { folderName: 'gb',       displayName: 'Game Boy',                       slug: 'game-boy',           igdbPlatformId: 33,  fileExtensions: '.gb',                sortOrder: 1 },
  { folderName: 'gbc',      displayName: 'Game Boy Color',                 slug: 'game-boy-color',     igdbPlatformId: 22,  fileExtensions: '.gbc',               sortOrder: 2 },
  { folderName: 'gba',      displayName: 'Game Boy Advance',               slug: 'game-boy-advance',   igdbPlatformId: 24,  fileExtensions: '.gba',               sortOrder: 3 },
  { folderName: 'nes',      displayName: 'Nintendo Entertainment System',  slug: 'nes',                igdbPlatformId: 18,  fileExtensions: '.nes',               sortOrder: 4 },
  { folderName: 'snes',     displayName: 'Super Nintendo',                 slug: 'snes',               igdbPlatformId: 19,  fileExtensions: '.smc,.sfc',          sortOrder: 5 },
  { folderName: 'n64',      displayName: 'Nintendo 64',                    slug: 'n64',                igdbPlatformId: 4,   fileExtensions: '.n64,.z64,.v64',     sortOrder: 6 },
  { folderName: 'nds',      displayName: 'Nintendo DS',                    slug: 'nds',                igdbPlatformId: 20,  fileExtensions: '.nds',               sortOrder: 7 },
  { folderName: '3ds',      displayName: 'Nintendo 3DS',                   slug: '3ds',                igdbPlatformId: 37,  fileExtensions: '.3ds,.cia',          sortOrder: 8 },
  { folderName: 'ngc',      displayName: 'GameCube',                       slug: 'gamecube',           igdbPlatformId: 21,  fileExtensions: '.iso,.gcm',          sortOrder: 9 },
  { folderName: 'wii',      displayName: 'Wii',                            slug: 'wii',                igdbPlatformId: 5,   fileExtensions: '.iso,.wbfs',         sortOrder: 10 },
  { folderName: 'switch',   displayName: 'Nintendo Switch',                slug: 'switch',             igdbPlatformId: 130, fileExtensions: '.nsp,.xci,.nsz,.xcz', sortOrder: 11 },
  { folderName: 'genesis',  displayName: 'Sega Genesis',                   slug: 'genesis',            igdbPlatformId: 29,  fileExtensions: '.md,.gen,.bin',      sortOrder: 12 },
  { folderName: 'psx',      displayName: 'PlayStation',                    slug: 'playstation',        igdbPlatformId: 7,   fileExtensions: '.bin,.iso,.img,.pbp', sortOrder: 13 },
  { folderName: 'ps2',      displayName: 'PlayStation 2',                  slug: 'ps2',                igdbPlatformId: 8,   fileExtensions: '.iso,.bin',          sortOrder: 14 },
  { folderName: 'psp',      displayName: 'PlayStation Portable',           slug: 'psp',                igdbPlatformId: 38,  fileExtensions: '.iso,.cso',          sortOrder: 15 },
];
