import { vi } from "vitest";

type PrismaModelMock = Record<string, ReturnType<typeof vi.fn>>;

type PrismaMock = {
  user: PrismaModelMock;
};

const createModelMock = (): PrismaModelMock => ({
  findUnique: vi.fn(),
  findMany: vi.fn(),
  create: vi.fn(),
  update: vi.fn(),
  findFirst: vi.fn(),
});

export const prismaMock: PrismaMock = {
  user: createModelMock(),
};

export const resetPrismaMock = () => {
  Object.values(prismaMock).forEach((model) => {
    Object.values(model).forEach((fn) => fn.mockReset());
  });
};
