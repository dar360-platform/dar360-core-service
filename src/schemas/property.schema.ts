import { z } from 'zod';
import { PropertyStatus, PropertyType, RentFrequency } from '@prisma/client';

export const propertySchema = z.object({
  title: z.string().min(1, 'Title must be at least 1 characters long'),
  description: z.string().optional(),
  type: z.nativeEnum(PropertyType),
  status: z.nativeEnum(PropertyStatus).optional(),
  bedrooms: z.coerce.number().int().min(0, 'Bedrooms must be a non-negative number'),
  bathrooms: z.coerce.number().int().min(1, 'Bathrooms must be at least 1'),
  areaSqft: z.coerce.number().positive('Area must be a positive number').optional(),
  rentAmount: z.coerce.number().positive('Rent amount must be a positive number'),
  rentFrequency: z.nativeEnum(RentFrequency).optional(),
  depositAmount: z.coerce.number().positive('Deposit must be a positive number').optional(),
  addressLine: z.string().min(1, 'Address line is required'),
  buildingName: z.string().optional(),
  areaName: z.string().optional(),
  city: z.string().optional(),
  latitude: z.coerce.number().optional(),
  longitude: z.coerce.number().optional(),
  amenities: z.array(z.string()).optional(),
  ownerId: z.string().cuid('Invalid owner ID').optional(),
});

export const createPropertySchema = propertySchema;

export const updatePropertySchema = propertySchema.partial();

export const searchPropertySchema = z.object({
    type: z.nativeEnum(PropertyType).optional(),
    status: z.nativeEnum(PropertyStatus).optional(),
    bedrooms_min: z.coerce.number().int().min(0).optional(),
    bedrooms_max: z.coerce.number().int().min(0).optional(),
    rent_min: z.coerce.number().positive().optional(),
    rent_max: z.coerce.number().positive().optional(),
    area_min: z.coerce.number().positive().optional(),
    area_max: z.coerce.number().positive().optional(),
    areaName: z.string().optional(),
    page: z.coerce.number().int().min(1).optional().default(1),
    limit: z.coerce.number().int().min(1).max(100).optional().default(10),
});

export const updatePropertyStatusSchema = z.object({
  status: z.nativeEnum(PropertyStatus),
});

export const uploadPropertyImageSchema = z.object({
  fileName: z.string(),
  contentType: z.string(),
  isPrimary: z.boolean().optional().default(false),
  displayOrder: z.number().int().optional().default(0),
});
