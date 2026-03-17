import { z } from 'zod';

export const installSchema = z.object({
  admin: z.object({
    username: z.string().min(3).max(50),
    email: z.string().email(),
    password: z.string().min(8),
  }),
  romPath: z.string().min(1, 'ROM path is required'),
  language: z.enum(['en', 'de']),
  igdb: z.object({
    clientId: z.string().min(1, 'IGDB Client ID is required'),
    clientSecret: z.string().min(1, 'IGDB Client Secret is required'),
  }),
});

export type InstallInput = z.infer<typeof installSchema>;
