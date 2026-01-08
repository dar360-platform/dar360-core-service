import { beforeAll, beforeEach, afterAll, afterEach, vi } from 'vitest';
import prisma from '@/lib/db';

// Mock Prisma Client
vi.mock('@/lib/db', () => ({
  default: {
    user: {
      findUnique: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      count: vi.fn(),
      findMany: vi.fn(),
    },
    property: {
      findUnique: vi.fn(),
      create: vi.fn(),
      update: vi.fn(),
      count: vi.fn(),
      findMany: vi.fn(),
      delete: vi.fn(),
    },
    propertyImage: {
      create: vi.fn(),
      findUnique: vi.fn(),
      delete: vi.fn(),
    },
    propertyShare: {
      create: vi.fn(),
      update: vi.fn(),
      findUnique: vi.fn(),
    },
    viewing: {
      create: vi.fn(),
      findUnique: vi.fn(),
      update: vi.fn(),
      delete: vi.fn(),
      count: vi.fn(),
      findMany: vi.fn(),
    },
    contract: {
      count: vi.fn(),
      create: vi.fn(),
      findUnique: vi.fn(),
      update: vi.fn(),
      findMany: vi.fn(),
    },
    notificationLog: {
      create: vi.fn(),
    }
  },
}));

beforeEach(() => {
  // Reset all mocks before each test
  vi.clearAllMocks();
});

// Optionally, you can set up global mocks or configurations here
// For example, mocking environment variables if your code depends on them
// vi.stubEnv('NODE_ENV', 'test');