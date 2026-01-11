import { afterEach, vi } from "vitest";

process.env.NEXTAUTH_SECRET ??= "test-secret";

afterEach(() => {
  vi.restoreAllMocks();
  vi.clearAllMocks();
});
