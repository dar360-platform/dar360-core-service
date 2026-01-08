import { z } from 'zod';
import { ViewingOutcome, ViewingStatus } from '@prisma/client';

export const createViewingSchema = z.object({
  propertyId: z.string().min(1, 'Property ID is required'),
  agentId: z.string().min(1, 'Agent ID is required'),
  tenantName: z.string().min(1, 'Tenant name is required'),
  tenantPhone: z.string().min(10, 'Tenant phone number is required'),
  tenantEmail: z.string().email('Invalid email format').optional().or(z.literal('')),
  scheduledAt: z.string().datetime('Invalid datetime format').transform((str) => new Date(str)),
  notes: z.string().optional(),
});

export const updateViewingSchema = z.object({
  propertyId: z.string().min(1, 'Property ID is required').optional(),
  agentId: z.string().min(1, 'Agent ID is required').optional(),
  tenantName: z.string().min(1, 'Tenant name is required').optional(),
  tenantPhone: z.string().min(10, 'Tenant phone number is required').optional(),
  tenantEmail: z.string().email('Invalid email format').optional().or(z.literal('')),
  scheduledAt: z.string().datetime('Invalid datetime format').transform((str) => new Date(str)).optional(),
  status: z.nativeEnum(ViewingStatus).optional(),
  outcome: z.nativeEnum(ViewingOutcome).optional(),
  notes: z.string().optional(),
});

export const updateViewingOutcomeSchema = z.object({
  outcome: z.nativeEnum(ViewingOutcome),
  notes: z.string().optional(),
});

export const searchViewingSchema = z.object({
  status: z.nativeEnum(ViewingStatus).optional(),
  propertyId: z.string().optional(),
  agentId: z.string().optional(),
  startDate: z.string().datetime('Invalid datetime format').transform((str) => new Date(str)).optional(),
  endDate: z.string().datetime('Invalid datetime format').transform((str) => new Date(str)).optional(),
  page: z.coerce.number().int().min(1).default(1),
  limit: z.coerce.number().int().min(1).max(100).default(20),
});

export type CreateViewingSchema = z.infer<typeof createViewingSchema>;
export type UpdateViewingSchema = z.infer<typeof updateViewingSchema>;
export type UpdateViewingOutcomeSchema = z.infer<typeof updateViewingOutcomeSchema>;
export type SearchViewingSchema = z.infer<typeof searchViewingSchema>;