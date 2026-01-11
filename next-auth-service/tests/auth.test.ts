import "./helpers/moduleMocks";

import { beforeEach, describe, expect, it, vi } from "vitest";

vi.mock("next-auth", async () => {
  const actual = await vi.importActual<typeof import("next-auth")>("next-auth");
  return {
    ...actual,
    getServerSession: vi.fn(),
  };
});

import { HttpError } from "@/lib/errors";
import * as authModule from "@/lib/auth";
import type { AuthUser } from "@/lib/auth";
import * as nextAuthModule from "next-auth";
import type { Session } from "next-auth";
import type { JWT } from "next-auth/jwt";
import { prismaMock, resetPrismaMock } from "./helpers/prismaMock";
import { compareMock } from "./helpers/moduleMocks";
import { createUserFixture } from "./helpers/userFixture";

const getServerSessionMock = vi.mocked(nextAuthModule.getServerSession);

beforeEach(() => {
  resetPrismaMock();
  compareMock.mockReset();
  compareMock.mockResolvedValue(true);
  getServerSessionMock.mockReset();
});

describe("auth configuration", () => {
  const credentialsProvider = authModule.authOptions.providers[0] as {
    authorize?: (credentials: Record<string, unknown>) => Promise<Record<string, unknown> | null>;
    options: {
      authorize?: (credentials: Record<string, unknown>) => Promise<Record<string, unknown> | null>;
    };
  };

  it("authorizes valid credentials", async () => {
    resetPrismaMock();
    prismaMock.user.findUnique.mockResolvedValueOnce(
      createUserFixture({
        id: "agent-1",
        email: "agent@example.com",
        role: "AGENT",
        fullName: "Agent One",
      }),
    );
    compareMock.mockResolvedValueOnce(true);

    const authorize = credentialsProvider.options.authorize!;
    const result = await authorize({
      email: "agent@example.com",
      password: "StrongPass1!",
    });

    expect(result).toMatchObject({
      id: "agent-1",
      email: "agent@example.com",
      fullName: "Agent One",
      role: "AGENT",
    });
  });

  it("returns null when credentials invalid", async () => {
    resetPrismaMock();
    prismaMock.user.findUnique.mockResolvedValueOnce(
      createUserFixture({
        id: "agent-1",
        email: "agent@example.com",
        role: "AGENT",
      }),
    );
    compareMock.mockResolvedValueOnce(false);

    const authorize = credentialsProvider.options.authorize!;
    const result = await authorize({
      email: "agent@example.com",
      password: "WrongPass",
    });

    expect(result).toBeNull();
  });

  it("adds role and fullName to JWT token", async () => {
    const token = await authModule.authOptions.callbacks?.jwt?.({
      token: {},
      user: {
        id: "agent-1",
        role: "AGENT",
        fullName: "Agent One",
        email: "agent@example.com",
        name: "Agent One",
        image: null,
      } satisfies AuthUser,
      account: null,
      profile: null,
      isNewUser: false,
    });

    expect(token).toMatchObject({
      role: "AGENT",
      fullName: "Agent One",
    });
  });

  it("projects session user data from token", async () => {
    const sessionInput: Session = {
      user: {
        id: "",
        role: "AGENT",
        fullName: "",
        email: null,
        name: null,
        image: null,
      } satisfies AuthUser,
      expires: new Date("2024-03-01T00:00:00Z").toISOString(),
    };
    const tokenInput: JWT = {
      sub: "user-1",
      role: "AGENT",
      fullName: "Agent One",
    };

    const session = await authModule.authOptions.callbacks?.session?.({
      session: sessionInput,
      token: tokenInput,
      newSession: null,
      trigger: "update",
    });

    expect(session?.user).toMatchObject({
      id: "user-1",
      role: "AGENT",
      fullName: "Agent One",
    });
  });
});

describe("requireUser", () => {
  it("returns session user when present", async () => {
    const sessionUser: AuthUser = {
      id: "user-1",
      role: "AGENT",
      fullName: "Agent One",
      email: "agent@example.com",
      name: "Agent One",
      image: null,
    };
    getServerSessionMock.mockResolvedValueOnce({
      user: sessionUser,
      expires: new Date("2024-03-01T00:00:00Z").toISOString(),
    });

    const result = await authModule.requireUser();

    expect(result).toMatchObject({ id: "user-1" });
  });

  it("throws unauthorized when session missing", async () => {
    getServerSessionMock.mockResolvedValueOnce(null);

    await expect(authModule.requireUser()).rejects.toBeInstanceOf(HttpError);
  });

  it("throws forbidden when role mismatch", async () => {
    getServerSessionMock.mockResolvedValueOnce({
      user: {
        id: "user-1",
        role: "TENANT",
        fullName: "Tenant",
        email: "tenant@example.com",
        name: "Tenant",
        image: null,
      } satisfies AuthUser,
      expires: new Date("2024-03-01T00:00:00Z").toISOString(),
    });

    await expect(authModule.requireUser(["AGENT"])).rejects.toBeInstanceOf(HttpError);
  });
});
