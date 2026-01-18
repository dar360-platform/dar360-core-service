import { z } from 'zod';
import { Dar360Role } from '@prisma/client';

// Base schema without refinement
const userSchemaBase = z.object({
  email: z.string().email(),
  password: z.string().min(8, 'Password must be at least 8 characters long'),
  fullName: z.string().min(1, 'Full name is required').optional(),
  firstName: z.string().optional(),
  lastName: z.string().optional(),
  phone: z.string().min(10, 'Phone number is required'),
  role: z.nativeEnum(Dar360Role).default(Dar360Role.TENANT),
  reraLicenseNumber: z.string().optional(),
  reraBrokerId: z.string().optional(), // Frontend field name alias
  agencyName: z.string().optional(),
  emiratesId: z.string().optional(), // For owners/tenants
});

// Create schema with refinement
export const userSchema = userSchemaBase.refine((data) => {
  // Require fullName or (firstName and lastName)
  return data.fullName || (data.firstName && data.lastName);
}, {
  message: 'Either fullName or both firstName and lastName are required',
  path: ['fullName'],
});

// Update schema based on base schema (without refinement) to allow .partial()
export const updateUserSchema = userSchemaBase.partial().omit({ password: true }).extend({
  isActive: z.boolean().optional(),
  emiratesId: z.string().optional(),
});

export type UserSchema = z.infer<typeof userSchema>;
export type UpdateUserSchema = z.infer<typeof updateUserSchema>;
