import { z } from 'zod';

export const saveUploadSchema = z.object({
  gameId: z.coerce.number().int().positive(),
  deviceName: z.string().max(100).optional(),
});

export type SaveUploadInput = z.infer<typeof saveUploadSchema>;
