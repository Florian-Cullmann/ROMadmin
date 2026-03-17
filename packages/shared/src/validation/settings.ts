import { z } from 'zod';

export const updateSettingsSchema = z.record(z.string(), z.string());

export const platformSchema = z.object({
  folderName: z.string().min(1).max(50),
  displayName: z.string().min(1).max(100),
  slug: z.string().min(1).max(50),
  igdbPlatformId: z.number().int().nullable().optional(),
  thumbnailUrl: z.string().url().nullable().optional(),
  fileExtensions: z.string().min(1),
  sortOrder: z.number().int().default(0),
});

export type UpdateSettingsInput = z.infer<typeof updateSettingsSchema>;
export type PlatformInput = z.infer<typeof platformSchema>;
