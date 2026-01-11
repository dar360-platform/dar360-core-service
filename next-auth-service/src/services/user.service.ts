import { Dar360Role, User } from "@prisma/client";
import bcrypt from "bcryptjs";
import crypto from "node:crypto";

import prisma from "@/lib/db";
import { HttpError } from "@/lib/errors";
import {
  createUserSchema,
  currentUserUpdateSchema,
  ownerInviteSchema,
  registerSchema,
  updateUserSchema,
  userFiltersSchema,
  verifyReraSchema,
} from "@/schemas/user.schema";

const SALT_ROUNDS = 10;

export type PublicUser = Omit<User, "passwordHash">;

function toPublicUser(user: User): PublicUser {
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  const { passwordHash, ...rest } = user;
  return rest;
}

async function hashPassword(password: string) {
  return bcrypt.hash(password, SALT_ROUNDS);
}

export async function registerUser(payload: unknown) {
  const data = registerSchema.parse(payload);
  const email = data.email.toLowerCase();
  const existing = await prisma.user.findUnique({ where: { email } });

  if (existing) {
    if (!existing.isActive && existing.invitedById) {
      const updated = await prisma.user.update({
        where: { id: existing.id },
        data: {
          passwordHash: await hashPassword(data.password),
          fullName: data.fullName,
          phone: data.phone,
          agencyName: data.agencyName ?? existing.agencyName,
          reraLicenseNumber: data.reraLicenseNumber ?? existing.reraLicenseNumber,
          isActive: true,
          updatedAt: new Date(),
        },
      });
      return toPublicUser(updated);
    }
    throw new HttpError(409, "Email already registered");
  }

  const created = await prisma.user.create({
    data: {
      email,
      passwordHash: await hashPassword(data.password),
      role: data.role,
      fullName: data.fullName,
      phone: data.phone,
      agencyName: data.agencyName,
      reraLicenseNumber: data.reraLicenseNumber,
    },
  });
  return toPublicUser(created);
}

export async function createUser(payload: unknown, invitedById?: string) {
  const data = createUserSchema.parse(payload);
  const email = data.email.toLowerCase();
  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) {
    throw new HttpError(409, "Email already registered");
  }
  const created = await prisma.user.create({
    data: {
      email,
      passwordHash: await hashPassword(data.password),
      role: data.role,
      fullName: data.fullName,
      phone: data.phone,
      agencyName: data.agencyName,
      reraLicenseNumber: data.reraLicenseNumber,
      invitedById: invitedById ?? data.invitedById,
    },
  });
  return toPublicUser(created);
}

export async function listUsers(query: Record<string, string | string[] | undefined>) {
  const parsed = userFiltersSchema.parse({
    role: typeof query.role === "string" ? query.role.toUpperCase() : undefined,
    isActive: typeof query.isActive === "string" ? query.isActive : undefined,
  });

  const users = await prisma.user.findMany({
    where: {
      role: parsed.role ?? undefined,
      isActive: parsed.isActive ?? undefined,
    },
    orderBy: { createdAt: "desc" },
  });
  return users.map(toPublicUser);
}

export async function getUserById(userId: string) {
  const user = await prisma.user.findUnique({ where: { id: userId } });
  if (!user) {
    throw new HttpError(404, "User not found");
  }
  return toPublicUser(user);
}

export async function updateUser(userId: string, payload: unknown) {
  const data = updateUserSchema.parse(payload);

  if (data.email) {
    const existing = await prisma.user.findFirst({
      where: { email: data.email.toLowerCase(), NOT: { id: userId } },
    });
    if (existing) {
      throw new HttpError(409, "Email already registered");
    }
  }

  const user = await prisma.user.update({
    where: { id: userId },
    data: {
      email: data.email?.toLowerCase(),
      fullName: data.fullName,
      phone: data.phone,
      agencyName: data.agencyName,
      role: data.role,
      reraLicenseNumber: data.reraLicenseNumber ?? undefined,
      isActive: data.isActive ?? undefined,
    },
  });

  return toPublicUser(user);
}

export async function deleteUser(userId: string) {
  await prisma.user.update({
    where: { id: userId },
    data: { isActive: false },
  });
}

export async function updateCurrentUser(userId: string, payload: unknown) {
  const data = currentUserUpdateSchema.parse(payload);
  const user = await prisma.user.update({
    where: { id: userId },
    data,
  });
  return toPublicUser(user);
}

export async function verifyRera(userId: string, payload: unknown) {
  const data = verifyReraSchema.parse(payload);
  const user = await prisma.user.update({
    where: { id: userId },
    data: {
      reraLicenseNumber: data.licenseNumber,
      reraVerifiedAt: new Date(),
    },
  });
  return toPublicUser(user);
}

export async function inviteOwner(inviterId: string, payload: unknown) {
  const data = ownerInviteSchema.parse(payload);
  const email = data.email.toLowerCase();

  const existing = await prisma.user.findUnique({ where: { email } });
  if (existing) {
    throw new HttpError(409, "An account with this email already exists");
  }

  const randomPassword = crypto.randomBytes(12).toString("hex");
  const invitee = await prisma.user.create({
    data: {
      email,
      fullName: data.fullName,
      phone: data.phone,
      agencyName: data.agencyName,
      role: Dar360Role.OWNER,
      passwordHash: await hashPassword(randomPassword),
      invitedById: inviterId,
      isActive: false,
    },
  });

  return toPublicUser(invitee);
}

export async function getInvitedOwners(inviterId: string) {
  const owners = await prisma.user.findMany({
    where: {
      invitedById: inviterId,
      role: Dar360Role.OWNER,
    },
    orderBy: { createdAt: "desc" },
  });
  return owners.map(toPublicUser);
}
