import { z } from 'zod';
import { ContractStatus } from '@prisma/client';

export const createContractSchema = z.object({
  propertyId: z.string().min(1, 'Property ID is required'),
  agentId: z.string().min(1, 'Agent ID is required'),
  tenantName: z.string().min(1, 'Tenant name is required'),
  tenantPhone: z.string().min(10, 'Tenant phone number is required'),
  tenantEmail: z.string().email('Invalid email format'),
  tenantEmiratesId: z.string().min(1, 'Tenant Emirates ID is required'),
  startDate: z.string().datetime('Invalid start date format').transform((str) => new Date(str)),
  endDate: z.string().datetime('Invalid end date format').transform((str) => new Date(str)),
  rentAmount: z.number().positive('Rent amount must be a positive number'),
  depositAmount: z.number().min(0, 'Deposit amount must be a non-negative number'),
  numberOfCheques: z.number().int().min(1).max(12).optional().default(1),
  cheques: z.number().int().min(1).max(12).optional(), // Frontend field name alias
  paymentTerms: z.string().optional(),
}).transform((data) => ({
  ...data,
  numberOfCheques: data.numberOfCheques || data.cheques || 1,
}));

export const updateContractSchema = z.object({
  propertyId: z.string().min(1, 'Property ID is required').optional(),
  agentId: z.string().min(1, 'Agent ID is required').optional(),
  tenantName: z.string().min(1, 'Tenant name is required').optional(),
  tenantPhone: z.string().min(10, 'Tenant phone number is required').optional(),
  tenantEmail: z.string().email('Invalid email format').optional(),
  tenantEmiratesId: z.string().min(1, 'Tenant Emirates ID is required').optional(),
  startDate: z.string().datetime('Invalid start date format').transform((str) => new Date(str)).optional(),
  endDate: z.string().datetime('Invalid end date format').transform((str) => new Date(str)).optional(),
  rentAmount: z.number().positive('Rent amount must be a positive number').optional(),
  depositAmount: z.number().min(0, 'Deposit amount must be a non-negative number').optional(),
  numberOfCheques: z.number().int().min(1).max(12).optional(),
  cheques: z.number().int().min(1).max(12).optional(), // Frontend field name alias
  paymentTerms: z.string().optional(),
  status: z.nativeEnum(ContractStatus).optional(),
}).partial().transform((data) => ({
  ...data,
  numberOfCheques: data.numberOfCheques || data.cheques,
})); // Allow partial updates

export const sendOtpSchema = z.object({
  // No specific fields needed for sending OTP, contractId is from path
});

export const verifyOtpSchema = z.object({
  otp: z.string().length(6, 'OTP must be 6 digits long'),
});

export const searchContractSchema = z.object({
  status: z.nativeEnum(ContractStatus).optional(),
  propertyId: z.string().optional(),
  agentId: z.string().optional(),
  ownerId: z.string().optional(),
  page: z.coerce.number().int().min(1).default(1),
  limit: z.coerce.number().int().min(1).max(100).default(20),
});

export type CreateContractSchema = z.infer<typeof createContractSchema>;
export type UpdateContractSchema = z.infer<typeof updateContractSchema>;
export type SendOtpSchema = z.infer<typeof sendOtpSchema>;
export type VerifyOtpSchema = z.infer<typeof verifyOtpSchema>;
export type SearchContractSchema = z.infer<typeof searchContractSchema>;
