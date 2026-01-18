import { z } from 'zod';
import { ViewingStatus, ViewingOutcome } from '@prisma/client';

export const createViewingSchema = z.object({
  propertyId: z.string().cuid(),
  tenantName: z.string(),
  tenantPhone: z.string(),
  tenantEmail: z.string().email().optional(),
  scheduledAt: z.coerce.date(),
  notes: z.string().optional(),
});

export const searchViewingSchema = z.object({
  status: z.nativeEnum(ViewingStatus).optional(),
  date_from: z.coerce.date().optional(),
  date_to: z.coerce.date().optional(),
  page: z.coerce.number().int().min(1).optional().default(1),
  limit: z.coerce.number().int().min(1).max(100).optional().default(10),
});

export const updateViewingSchema = createViewingSchema.partial();

const frontendViewingOutcomeSchema = z.enum([
  'interested',
  'not_interested',
  'no_show',
  'offer_made',
  'pending',
]);

export const updateViewingOutcomeSchema = z.object({
  outcome: z.union([z.nativeEnum(ViewingOutcome), frontendViewingOutcomeSchema]),
  notes: z.string().optional(),
});
