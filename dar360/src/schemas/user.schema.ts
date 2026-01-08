import { z } from 'zod';
import { Dar360Role } from '@prisma/client';

export const userSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8, 'Password must be at least 8 characters long'),
  fullName: z.string().min(1, 'Full name is required'),
  phone: z.string().min(10, 'Phone number is required'),
  role: z.nativeEnum(Dar360Role).default(Dar360Role.TENANT),
  reraLicenseNumber: z.string().optional(),
  agencyName: z.string().optional(),
});

export const updateUserSchema = userSchema.partial().omit({ password: true }).extend({
  isActive: z.boolean().optional(),
});

export type UserSchema = z.infer<typeof userSchema>;
export type UpdateUserSchema = z.infer<typeof updateUserSchema>;
