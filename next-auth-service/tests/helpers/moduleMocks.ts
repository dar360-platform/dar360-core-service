import { vi } from "vitest";

import { prismaMock } from "./prismaMock";

export const hashMock = vi.fn(async () => "hashed-password");
export const compareMock = vi.fn(async () => true);

vi.mock("@/lib/db", () => ({
  default: prismaMock,
  prisma: prismaMock,
}));

vi.mock("bcryptjs", () => ({
  default: {
    hash: hashMock,
    compare: compareMock,
  },
  hash: hashMock,
  compare: compareMock,
}));
