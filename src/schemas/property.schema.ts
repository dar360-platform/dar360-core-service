import { z } from 'zod';
import { PropertyStatus, PropertyType, RentFrequency } from '@prisma/client';

const propertyTypeSchema = z.preprocess(
  (value) => (typeof value === 'string' ? value.toUpperCase() : value),
  z.nativeEnum(PropertyType)
);

const propertyStatusSchema = z.preprocess(
  (value) => (typeof value === 'string' ? value.toUpperCase() : value),
  z.nativeEnum(PropertyStatus)
);

const rentFrequencySchema = z.preprocess(
  (value) => (typeof value === 'string' ? value.toUpperCase() : value),
  z.nativeEnum(RentFrequency)
);

export const propertySchema = z.object({
  title: z.string().min(1, 'Title must be at least 1 characters long'),
  description: z.string().optional(),
  type: propertyTypeSchema,
  status: propertyStatusSchema.optional(),
  bedrooms: z.coerce.number().int().min(0, 'Bedrooms must be a non-negative number'),
  bathrooms: z.coerce.number().int().min(1, 'Bathrooms must be at least 1'),
  areaSqft: z.coerce.number().positive('Area must be a positive number').optional(),
  rentAmount: z.coerce.number().positive('Rent amount must be a positive number'),
  rentFrequency: rentFrequencySchema.optional(),
  depositAmount: z.coerce.number().min(0, 'Deposit must be a non-negative number').optional(),
  numberOfCheques: z.coerce.number().int().min(1).max(12).optional(),
  addressLine: z.string().min(1, 'Address line is required'),
  buildingName: z.string().optional(),
  unit: z.string().optional(),
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
    type: propertyTypeSchema.optional(),
    status: propertyStatusSchema.optional(),
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
  status: propertyStatusSchema,
});

export const uploadPropertyImageSchema = z.object({
  fileName: z.string(),
  contentType: z.string(),
  isPrimary: z.boolean().optional().default(false),
  displayOrder: z.number().int().optional().default(0),
});
