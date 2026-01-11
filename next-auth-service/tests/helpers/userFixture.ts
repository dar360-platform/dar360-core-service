import type { Dar360Role, User } from "@prisma/client";

type UserOverrides = Partial<User>;

export const createUserFixture = (overrides: UserOverrides = {}): User => ({
  id: "user-id",
  email: "user@example.com",
  passwordHash: "hashed-password",
  role: "AGENT" satisfies Dar360Role,
  fullName: "Test User",
  phone: "+971500000000",
  reraLicenseNumber: null,
  reraVerifiedAt: null,
  agencyName: null,
  invitedById: null,
  isActive: true,
  createdAt: new Date("2024-01-01T00:00:00Z"),
  updatedAt: new Date("2024-01-01T00:00:00Z"),
  ...overrides,
});

export const toPublicUser = (user: User) => {
  const result = { ...user };
  delete (result as { passwordHash?: string }).passwordHash;
  return result as Omit<User, "passwordHash">;
};
