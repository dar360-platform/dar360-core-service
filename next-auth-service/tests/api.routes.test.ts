import { describe, expect, it, vi } from "vitest";

import { HttpError } from "@/lib/errors";
import * as authModule from "@/lib/auth";
import type { AuthUser } from "@/lib/auth";
import * as userService from "@/services/user.service";
import { POST as registerPOST } from "@/app/api/auth/register/route";
import { POST as logoutPOST } from "@/app/api/auth/logout/route";
import { GET as usersGET, POST as usersPOST } from "@/app/api/users/route";
import {
  GET as userDetailGET,
  PUT as userDetailPUT,
  DELETE as userDetailDELETE,
} from "@/app/api/users/[id]/route";
import { GET as meGET, PUT as mePUT } from "@/app/api/users/me/route";
import { POST as verifyReraPOST } from "@/app/api/users/verify-rera/route";
import { GET as ownersGET } from "@/app/api/users/owners/route";
import { POST as ownerInvitePOST } from "@/app/api/users/owners/invite/route";
import { createUserFixture, toPublicUser } from "./helpers/userFixture";

const createRequest = <Fn extends (req: unknown, ...args: unknown[]) => unknown>(
  url: string,
  {
    method = "GET",
    body,
    headers,
  }: { method?: string; body?: unknown; headers?: Record<string, string> } = {},
) => {
  const headersInstance = new Headers(headers);
  const parsedBody = body;
  const nextUrl = new URL(url);

  const request = {
    method,
    headers: headersInstance,
    nextUrl,
    async json() {
      if (parsedBody === undefined) {
        throw new Error("Request body not set in test helper");
      }
      return parsedBody;
    },
  };

  return request as Parameters<Fn>[0];
};

const mockAgent: AuthUser = {
  id: "agent-1",
  role: "AGENT",
  fullName: "Agent One",
  email: "agent@example.com",
  name: "Agent One",
  image: null,
};

describe("API Routes", () => {
  it("registers user successfully", async () => {
    const payload = { email: "test@example.com" };
    const registerSpy = vi
      .spyOn(userService, "registerUser")
      .mockResolvedValueOnce(toPublicUser(createUserFixture({ id: "user-1", email: "test@example.com" })));

    const res = await registerPOST(
      createRequest<typeof registerPOST>("http://localhost/api/auth/register", { method: "POST", body: payload }),
    );
    const json = await res.json();

    expect(res.status).toBe(201);
    expect(json).toMatchObject({ email: "test@example.com" });
    expect(registerSpy).toHaveBeenCalledWith(payload);
  });

  it("handles registration errors", async () => {
    vi.spyOn(userService, "registerUser").mockRejectedValueOnce(new HttpError(409, "exists"));

    const res = await registerPOST(
      createRequest<typeof registerPOST>("http://localhost/api/auth/register", {
        method: "POST",
        body: { email: "dup@example.com" },
      }),
    );

    expect(res.status).toBe(409);
  });

  it("clears auth cookies on logout", async () => {
    const res = await logoutPOST();
    expect(res.status).toBe(200);
    expect(res.cookies.get("next-auth.session-token")?.value).toBe("");
  });

  it("lists users for agent", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "listUsers").mockResolvedValueOnce([
      toPublicUser(createUserFixture({ id: "user-1" })),
    ]);

    const res = await usersGET(createRequest<typeof usersGET>("http://localhost/api/users"));
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toHaveLength(1);
  });

  it("returns error when list users fails", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "listUsers").mockRejectedValueOnce(new HttpError(500, "boom"));

    const res = await usersGET(createRequest<typeof usersGET>("http://localhost/api/users"));

    expect(res.status).toBe(500);
  });

  it("creates user", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "createUser").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "user-2", email: "new@example.com" })),
    );

    const res = await usersPOST(
      createRequest<typeof usersPOST>("http://localhost/api/users", {
        method: "POST",
        body: { email: "new@example.com" },
      }),
    );
    const json = await res.json();

    expect(res.status).toBe(201);
    expect(json).toMatchObject({ id: "user-2" });
  });

  it("gets user detail", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "getUserById").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "user-1", email: "user@example.com" })),
    );

    const res = await userDetailGET(createRequest<typeof userDetailGET>("http://localhost/api/users/user-1"), {
      params: { id: "user-1" },
    });
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toMatchObject({ id: "user-1" });
  });

  it("updates user detail", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "updateUser").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "user-1", fullName: "Updated" })),
    );

    const res = await userDetailPUT(
      createRequest<typeof userDetailPUT>("http://localhost/api/users/user-1", {
        method: "PUT",
        body: { fullName: "Updated" },
      }),
      { params: { id: "user-1" } },
    );
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toMatchObject({ fullName: "Updated" });
  });

  it("deletes user", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "deleteUser").mockResolvedValueOnce();

    const res = await userDetailDELETE(
      createRequest<typeof userDetailDELETE>("http://localhost/api/users/user-1"),
      { params: { id: "user-1" } },
    );
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toMatchObject({ success: true });
  });

  it("returns current user", async () => {
    const sessionUser: AuthUser = {
      id: "user-1",
      role: "AGENT",
      fullName: "Agent One",
      email: "agent@example.com",
      name: "Agent One",
      image: null,
    };
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(sessionUser);
    vi.spyOn(userService, "getUserById").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "user-1", email: "agent@example.com" })),
    );

    const res = await meGET(createRequest<typeof meGET>("http://localhost/api/users/me"));
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toMatchObject({ id: "user-1" });
  });

  it("updates current user", async () => {
    const sessionUser: AuthUser = {
      id: "user-1",
      role: "AGENT",
      fullName: "Agent One",
      email: "agent@example.com",
      name: "Agent One",
      image: null,
    };
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(sessionUser);
    vi.spyOn(userService, "updateCurrentUser").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "user-1", fullName: "New" })),
    );

    const res = await mePUT(
      createRequest<typeof mePUT>("http://localhost/api/users/me", { method: "PUT", body: { fullName: "New" } }),
    );
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toMatchObject({ fullName: "New" });
  });

  it("verifies RERA license", async () => {
    const sessionUser: AuthUser = {
      id: "user-1",
      role: "AGENT",
      fullName: "Agent One",
      email: "agent@example.com",
      name: "Agent One",
      image: null,
    };
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(sessionUser);
    vi.spyOn(userService, "verifyRera").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "user-1", reraLicenseNumber: "RERA-123" })),
    );

    const res = await verifyReraPOST(
      createRequest<typeof verifyReraPOST>("http://localhost/api/users/verify-rera", {
        method: "POST",
        body: { licenseNumber: "RERA-123" },
      }),
    );
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toMatchObject({ reraLicenseNumber: "RERA-123" });
  });

  it("lists invited owners", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "getInvitedOwners").mockResolvedValueOnce([
      toPublicUser(createUserFixture({ id: "owner-1", role: "OWNER" })),
    ]);

    const res = await ownersGET(createRequest<typeof ownersGET>("http://localhost/api/users/owners"));
    const json = await res.json();

    expect(res.status).toBe(200);
    expect(json).toHaveLength(1);
  });

  it("invites owner", async () => {
    vi.spyOn(authModule, "requireUser").mockResolvedValueOnce(mockAgent);
    vi.spyOn(userService, "inviteOwner").mockResolvedValueOnce(
      toPublicUser(createUserFixture({ id: "owner-1", role: "OWNER" })),
    );

    const res = await ownerInvitePOST(
      createRequest<typeof ownerInvitePOST>("http://localhost/api/users/owners/invite", {
        method: "POST",
        body: { email: "owner@example.com" },
      }),
    );
    const json = await res.json();

    expect(res.status).toBe(201);
    expect(json).toMatchObject({ id: "owner-1" });
  });
});
