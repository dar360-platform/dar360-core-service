import "./helpers/moduleMocks";

import crypto from "node:crypto";

import { beforeEach, describe, expect, it, vi } from "vitest";

import { HttpError } from "@/lib/errors";
import {
  createUser,
  deleteUser,
  getInvitedOwners,
  getUserById,
  inviteOwner,
  listUsers,
  registerUser,
  requestPasswordReset,
  resetPassword,
  updateCurrentUser,
  updateUser,
  verifyRera,
} from "@/services/user.service";
import { prismaMock, resetPrismaMock } from "./helpers/prismaMock";
import { createUserFixture } from "./helpers/userFixture";
import { hashMock } from "./helpers/moduleMocks";

describe("user.service", () => {
  beforeEach(() => {
    resetPrismaMock();
    hashMock.mockResolvedValue("hashed-password");
  });

  describe("registerUser", () => {
    it("creates a new active user when email is unused", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(null);
      prismaMock.user.create.mockResolvedValueOnce(
        createUserFixture({
        id: "u1",
        email: "agent@example.com",
          role: "AGENT",
          fullName: "Agent",
        }),
      );

      const result = await registerUser({
        email: "agent@example.com",
        password: "MyStrongPass1!",
        fullName: "Agent",
        phone: "+971500000000",
        role: "AGENT",
      });

      expect(hashMock).toHaveBeenCalledWith("MyStrongPass1!", expect.any(Number));
      expect(prismaMock.user.create).toHaveBeenCalledWith({
        data: expect.objectContaining({
          email: "agent@example.com",
          passwordHash: "hashed-password",
          role: "AGENT",
        }),
      });
      expect(result).toMatchObject({
        id: "u1",
        email: "agent@example.com",
        role: "AGENT",
        fullName: "Agent",
      });
      expect(result).not.toHaveProperty("passwordHash");
    });

    it("activates invited user if pending", async () => {
      const existingUser = createUserFixture({
        id: "owner-1",
        email: "owner@example.com",
        role: "OWNER",
        fullName: "Pending Owner",
        phone: "+971500000002",
        agencyName: "Agency",
        invitedById: "agent-1",
        isActive: false,
      });
      prismaMock.user.findUnique.mockResolvedValueOnce(existingUser);
      prismaMock.user.update.mockResolvedValueOnce(
        createUserFixture({
          ...existingUser,
          isActive: true,
          fullName: "Updated Owner",
          phone: "+971500000003",
        }),
      );

      const result = await registerUser({
        email: "owner@example.com",
        password: "NewPass123!",
        fullName: "Updated Owner",
        phone: "+971500000003",
        role: "OWNER",
      });

      expect(prismaMock.user.update).toHaveBeenCalled();
      expect(result).toMatchObject({
        id: "owner-1",
        isActive: true,
        fullName: "Updated Owner",
      });
    });

    it("throws when attempting to register an existing active user", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(
        createUserFixture({ id: "u1", email: "agent@example.com" }),
      );

      await expect(
        registerUser({
          email: "agent@example.com",
          password: "MyStrongPass1!",
          fullName: "Agent",
          phone: "+971500000000",
          role: "AGENT",
        }),
      ).rejects.toBeInstanceOf(HttpError);
    });
  });

  describe("createUser", () => {
    it("creates user invited by agent", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(null);
      prismaMock.user.create.mockResolvedValueOnce(
        createUserFixture({ id: "tenant-1", email: "tenant@example.com", role: "TENANT" }),
      );

      const result = await createUser(
        {
          email: "tenant@example.com",
          password: "StrongPass1!",
          fullName: "Tenant",
          phone: "+971500000001",
          role: "TENANT",
        },
        "agent-id",
      );

      expect(prismaMock.user.create).toHaveBeenCalledWith({
        data: expect.objectContaining({
          invitedById: "agent-id",
        }),
      });
      expect(result).toMatchObject({ id: "tenant-1", email: "tenant@example.com" });
    });

    it("throws if email already exists", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(createUserFixture({ id: "exists" }));

      await expect(
        createUser({
          email: "tenant@example.com",
          password: "StrongPass1!",
          fullName: "Tenant",
          phone: "+971500000001",
          role: "TENANT",
        }),
      ).rejects.toBeInstanceOf(HttpError);
    });
  });

  describe("listUsers", () => {
    it("returns users filtered by query", async () => {
      prismaMock.user.findMany.mockResolvedValueOnce([
        createUserFixture({
          id: "u1",
          email: "agent@example.com",
          role: "AGENT",
          fullName: "Agent",
          phone: "123",
        }),
      ]);

      const result = await listUsers({ role: "agent", isActive: "true" });

      expect(prismaMock.user.findMany).toHaveBeenCalledWith({
        where: { role: "AGENT", isActive: true },
        orderBy: { createdAt: "desc" },
      });
      expect(result).toHaveLength(1);
      expect(result[0]).not.toHaveProperty("passwordHash");
    });
  });

  describe("getUserById", () => {
    it("returns existing user", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(
        createUserFixture({ id: "u1", email: "agent@example.com" }),
      );

      const result = await getUserById("u1");

      expect(result).toMatchObject({ id: "u1" });
    });

    it("throws when user not found", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(null);

      await expect(getUserById("missing")).rejects.toBeInstanceOf(HttpError);
    });
  });

  describe("updateUser", () => {
    it("updates user fields when data valid", async () => {
      prismaMock.user.findFirst.mockResolvedValueOnce(null);
      prismaMock.user.update.mockResolvedValueOnce(
        createUserFixture({ id: "u1", email: "new@example.com" }),
      );

      const result = await updateUser("u1", {
        email: "new@example.com",
      });

      expect(prismaMock.user.update).toHaveBeenCalledWith({
        where: { id: "u1" },
        data: expect.objectContaining({ email: "new@example.com" }),
      });
      expect(result.email).toBe("new@example.com");
    });

    it("rejects when email is taken", async () => {
      prismaMock.user.findFirst.mockResolvedValueOnce(createUserFixture({ id: "u2" }));

      await expect(updateUser("u1", { email: "used@example.com" })).rejects.toBeInstanceOf(HttpError);
    });
  });

  describe("updateCurrentUser", () => {
    it("updates current user fields", async () => {
      prismaMock.user.update.mockResolvedValueOnce(
        createUserFixture({ id: "u1", email: "agent@example.com", fullName: "Agent New" }),
      );

      const result = await updateCurrentUser("u1", { fullName: "Agent New" });

      expect(prismaMock.user.update).toHaveBeenCalledWith({
        where: { id: "u1" },
        data: { fullName: "Agent New" },
      });
      expect(result.fullName).toBe("Agent New");
    });
  });

  describe("verifyRera", () => {
    it("updates license info and timestamp", async () => {
      const now = new Date("2024-02-01");
      vi.setSystemTime(now);

      prismaMock.user.update.mockResolvedValueOnce(
        createUserFixture({
          id: "u1",
          reraLicenseNumber: "RERA-123",
          reraVerifiedAt: now,
        }),
      );

      const result = await verifyRera("u1", { reraNumber: "RERA-123" });

      expect(prismaMock.user.update).toHaveBeenCalledWith({
        where: { id: "u1" },
        data: {
          reraLicenseNumber: "RERA-123",
          reraVerifiedAt: expect.any(Date),
        },
      });
      expect(result).toMatchObject({ reraLicenseNumber: "RERA-123" });
    });
  });

  describe("requestPasswordReset", () => {
    it("creates reset token when user exists", async () => {
      const user = createUserFixture({ id: "user-1", email: "agent@example.com" });
      const buffer = Buffer.alloc(32, "a");
      const expectedToken = buffer.toString("hex");

      prismaMock.user.findUnique.mockResolvedValueOnce(user);
      const randomSpy = vi.spyOn(crypto, "randomBytes").mockReturnValueOnce(buffer);
      prismaMock.passwordResetToken.create.mockResolvedValueOnce({});

      const result = await requestPasswordReset({ email: user.email });

      expect(randomSpy).toHaveBeenCalledWith(32);
      expect(prismaMock.passwordResetToken.deleteMany).toHaveBeenCalledWith({ where: { userId: user.id } });
      expect(prismaMock.passwordResetToken.create).toHaveBeenCalledWith({
        data: expect.objectContaining({
          token: expectedToken,
          userId: user.id,
        }),
      });
      expect(result).toMatchObject({ success: true, token: expectedToken });

      randomSpy.mockRestore();
    });

    it("silently succeeds when user not found", async () => {
      prismaMock.user.findUnique.mockResolvedValueOnce(null);

      const result = await requestPasswordReset({ email: "missing@example.com" });

      expect(result).toEqual({ success: true });
      expect(prismaMock.passwordResetToken.create).not.toHaveBeenCalled();
    });
  });

  describe("resetPassword", () => {
    it("resets password with valid token", async () => {
      const tokenRecord = {
        id: "token-1",
        token: "reset-token",
        userId: "user-1",
        expiresAt: new Date(Date.now() + 60_000),
        createdAt: new Date(),
      };
      prismaMock.passwordResetToken.findUnique.mockResolvedValueOnce(tokenRecord);
      prismaMock.user.update.mockResolvedValueOnce(createUserFixture({ id: "user-1" }));

      const result = await resetPassword({ token: "reset-token", password: "NewPass123!" });

      expect(hashMock).toHaveBeenCalledWith("NewPass123!", expect.any(Number));
      expect(prismaMock.user.update).toHaveBeenCalledWith({
        where: { id: "user-1" },
        data: { passwordHash: "hashed-password" },
      });
      expect(prismaMock.passwordResetToken.deleteMany).toHaveBeenCalledWith({ where: { userId: "user-1" } });
      expect(result).toMatchObject({ id: "user-1" });
    });

    it("throws when token invalid", async () => {
      prismaMock.passwordResetToken.findUnique.mockResolvedValueOnce(null);

      await expect(resetPassword({ token: "invalid", password: "Pass1234!" })).rejects.toBeInstanceOf(HttpError);
    });

    it("throws when token expired", async () => {
      prismaMock.passwordResetToken.findUnique.mockResolvedValueOnce({
        id: "token-1",
        token: "expired",
        userId: "user-1",
        expiresAt: new Date(Date.now() - 60_000),
        createdAt: new Date(),
      });

      await expect(resetPassword({ token: "expired", password: "Pass1234!" })).rejects.toBeInstanceOf(HttpError);
    });
  });

  describe("deleteUser", () => {
    it("soft deletes the user", async () => {
      prismaMock.user.update.mockResolvedValueOnce(createUserFixture({ id: "u1" }));

      await deleteUser("u1");

      expect(prismaMock.user.update).toHaveBeenCalledWith({
        where: { id: "u1" },
        data: { isActive: false },
      });
    });
  });

  describe("inviteOwner", () => {
    it("creates inactive owner with random password", async () => {
      const randomBytesSpy = vi.spyOn(crypto, "randomBytes").mockReturnValueOnce(Buffer.from("abc"));
      prismaMock.user.findUnique.mockResolvedValueOnce(null);
      prismaMock.user.create.mockResolvedValueOnce(
        createUserFixture({
          id: "owner-1",
          email: "owner@example.com",
          role: "OWNER",
          invitedById: "agent-1",
          isActive: false,
        }),
      );

      const result = await inviteOwner("agent-1", {
        email: "owner@example.com",
        fullName: "Owner Prospect",
        phone: "+971500000005",
      });

      expect(randomBytesSpy).toHaveBeenCalledWith(12);
      expect(prismaMock.user.create).toHaveBeenCalledWith({
        data: expect.objectContaining({
          email: "owner@example.com",
          invitedById: "agent-1",
          role: "OWNER",
          isActive: false,
        }),
      });
      expect(result).toMatchObject({ id: "owner-1", role: "OWNER", isActive: false });
    });
  });

  describe("getInvitedOwners", () => {
    it("lists owners invited by agent", async () => {
      prismaMock.user.findMany.mockResolvedValueOnce([
        createUserFixture({ id: "owner-1", email: "owner@example.com", role: "OWNER" }),
      ]);

      const result = await getInvitedOwners("agent-1");

      expect(prismaMock.user.findMany).toHaveBeenCalledWith({
        where: { invitedById: "agent-1", role: "OWNER" },
        orderBy: { createdAt: "desc" },
      });
      expect(result).toHaveLength(1);
    });
  });
});
