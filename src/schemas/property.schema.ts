import { z } from 'zod';
import { PropertyType } from '@prisma/client';

export const createPropertySchema = z.object({
  agentId: z.string().min(1, 'Agent ID is required'),
  ownerId: z.string().optional(),
  title: z.string().min(1, 'Title is required'),
  description: z.string().optional(),
  type: z.nativeEnum(PropertyType),
  bedrooms: z.number().int().min(0, 'Bedrooms must be a non-negative integer'),
  bathrooms: z.number().int().min(0, 'Bathrooms must be a non-negative integer'),
  rentAmount: z.number().positive('Rent amount must be a positive number'),
  addressLine: z.string().min(1, 'Address line is required'),
  areaName: z.string().optional(),
  amenities: z.array(z.string()).optional(),
});

export const searchPropertySchema = z.object({
  type: z.nativeEnum(PropertyType).optional(),
  minBedrooms: z.coerce.number().int().min(0).optional(),
  maxBedrooms: z.coerce.number().int().min(0).optional(),
  minRent: z.coerce.number().positive().optional(),
  maxRent: z.coerce.number().positive().optional(),
  areaName: z.string().optional(),
  page: z.coerce.number().int().min(1).default(1),
  limit: z.coerce.number().int().min(1).max(100).default(20),
});

export type CreatePropertySchema = z.infer<typeof createPropertySchema>;
export type SearchPropertySchema = z.infer<typeof searchPropertySchema>;

export const updatePropertySchema = createPropertySchema.partial();
export type UpdatePropertySchema = z.infer<typeof updatePropertySchema>;

export const uploadPropertyImageSchema = z.object({
  fileName: z.string().min(1, 'File name is required'),
  contentType: z.string().min(1, 'Content type is required'),
  isPrimary: z.boolean().optional().default(false),
  displayOrder: z.number().int().min(0).optional().default(0),
});
export type UploadPropertyImageSchema = z.infer<typeof uploadPropertyImageSchema>;


