import { Dar360Role } from "@prisma/client";
import { z } from "zod";

const passwordSchema = z
  .string()
  .min(8, "Password must be at least 8 characters long")
  .max(128, "Password must be at most 128 characters long");

const phoneSchema = z
  .string()
  .min(5, "Phone number is too short")
  .max(32, "Phone number is too long")
  .regex(/^[\d+()[\]\-\s]+$/, "Phone number contains invalid characters");

export const loginSchema = z.object({
  email: z.string().email("Invalid email address"),
  password: z.string().min(1, "Password is required"),
});

export const registerSchema = z.object({
  email: z.string().email(),
  password: passwordSchema,
  fullName: z.string().min(2).max(255),
  phone: phoneSchema,
  role: z.nativeEnum(Dar360Role),
  agencyName: z.string().min(2).max(255).optional(),
  reraLicenseNumber: z.string().min(3).max(64).optional(),
});

export const createUserSchema = registerSchema.extend({
  invitedById: z.string().cuid().optional(),
});

export const updateUserSchema = z
  .object({
    email: z.string().email().optional(),
    fullName: z.string().min(2).max(255).optional(),
    phone: phoneSchema.optional(),
    role: z.nativeEnum(Dar360Role).optional(),
    agencyName: z.string().min(2).max(255).optional(),
    reraLicenseNumber: z.string().min(3).max(64).nullable().optional(),
    isActive: z.boolean().optional(),
  })
  .refine((data) => Object.keys(data).length > 0, {
    message: "At least one field must be provided",
  });

export const currentUserUpdateSchema = z
  .object({
    fullName: z.string().min(2).max(255).optional(),
    phone: phoneSchema.optional(),
    agencyName: z.string().min(2).max(255).optional(),
  })
  .refine((data) => Object.keys(data).length > 0, {
    message: "At least one field must be provided",
  });

export const userFiltersSchema = z.object({
  role: z.nativeEnum(Dar360Role).optional(),
  isActive: z
    .enum(["true", "false"])
    .optional()
    .transform((value) => (value === undefined ? undefined : value === "true")),
});

export const verifyReraSchema = z.object({
  licenseNumber: z.string().min(3).max(64),
});

export const ownerInviteSchema = z.object({
  fullName: z.string().min(2).max(255),
  email: z.string().email(),
  phone: phoneSchema,
  agencyName: z.string().min(2).max(255).optional(),
});
