import { describe, it, expect, vi } from 'vitest';
import { UserService } from './user.service';
import prisma from '@/lib/db';
import bcrypt from 'bcryptjs';
import { Dar360Role } from '@prisma/client';

// Mock bcrypt for password hashing
vi.mock('bcryptjs', () => ({
  default: {
    hash: vi.fn((password) => Promise.resolve(`hashed_${password}`)),
    compare: vi.fn(),
  },
}));

describe('UserService', () => {
  let userService: UserService;

  beforeEach(() => {
    userService = new UserService();
    vi.clearAllMocks(); // Clear mocks before each test
  });

  describe('createUser', () => {
    it('should create a new user and hash the password', async () => {
      const userData = {
        email: 'test@example.com',
        passwordPlain: 'password123',
        fullName: 'Test User',
        phone: '1234567890',
        role: Dar360Role.TENANT,
      };

      const mockCreatedUser = {
        id: 'user123',
        email: userData.email,
        fullName: userData.fullName,
        phone: userData.phone,
        role: userData.role,
        passwordHash: 'hashed_password123',
        createdAt: new Date(),
        updatedAt: new Date(),
        isActive: true,
        reraLicenseNumber: null,
        reraVerifiedAt: null,
        agencyName: null,
        invitedById: null,
      };

      (prisma.user.create as vi.Mock).mockResolvedValue(mockCreatedUser);

      const createdUser = await userService.createUser(userData);

      expect(bcrypt.hash).toHaveBeenCalledWith(userData.passwordPlain, 12);
      expect(prisma.user.create).toHaveBeenCalledWith({
        data: {
          email: userData.email,
          passwordHash: 'hashed_password123',
          fullName: userData.fullName,
          phone: userData.phone,
          role: userData.role,
          reraLicenseNumber: undefined,
          agencyName: undefined,
        },
        select: expect.any(Object), // Ensure select object is passed
      });
      expect(createdUser.email).toEqual(userData.email);
      expect(createdUser.fullName).toEqual(userData.fullName);
    });

    it('should return an existing user by ID', async () => {
        const mockUser = {
            id: 'user123',
            email: 'test@example.com',
            fullName: 'Test User',
            phone: '1234567890',
            role: Dar360Role.TENANT,
            passwordHash: 'hashed_password123',
            createdAt: new Date(),
            updatedAt: new Date(),
            isActive: true,
            reraLicenseNumber: null,
            reraVerifiedAt: null,
            agencyName: null,
            invitedById: null,
        };
        (prisma.user.findUnique as vi.Mock).mockResolvedValue(mockUser);

        const user = await userService.findUserById('user123');

        expect(prisma.user.findUnique).toHaveBeenCalledWith({ where: { id: 'user123' } });
        expect(user).toEqual(mockUser);
    });

    it('should return null if user by ID is not found', async () => {
        (prisma.user.findUnique as vi.Mock).mockResolvedValue(null);

        const user = await userService.findUserById('nonexistent');

        expect(prisma.user.findUnique).toHaveBeenCalledWith({ where: { id: 'nonexistent' } });
        expect(user).toBeNull();
    });

    it('should return an existing user by email', async () => {
        const mockUser = {
            id: 'user123',
            email: 'test@example.com',
            fullName: 'Test User',
            phone: '1234567890',
            role: Dar360Role.TENANT,
            passwordHash: 'hashed_password123',
            createdAt: new Date(),
            updatedAt: new Date(),
            isActive: true,
            reraLicenseNumber: null,
            reraVerifiedAt: null,
            agencyName: null,
            invitedById: null,
        };
        (prisma.user.findUnique as vi.Mock).mockResolvedValue(mockUser);

        const user = await userService.findUserByEmail('test@example.com');

        expect(prisma.user.findUnique).toHaveBeenCalledWith({ where: { email: 'test@example.com' } });
        expect(user).toEqual(mockUser);
    });

    it('should return null if user by email is not found', async () => {
        (prisma.user.findUnique as vi.Mock).mockResolvedValue(null);

        const user = await userService.findUserByEmail('nonexistent@example.com');

        expect(prisma.user.findUnique).toHaveBeenCalledWith({ where: { email: 'nonexistent@example.com' } });
        expect(user).toBeNull();
    });

    it('should verify RERA for an agent', async () => {
        const userId = 'agent123';
        const reraLicenseNumber = 'RERA123';
        const agencyName = 'Test Agency';

        const mockUpdatedUser = {
            id: userId,
            email: 'agent@example.com',
            fullName: 'Test Agent',
            phone: '1234567890',
            role: Dar360Role.AGENT,
            passwordHash: 'hashed_password123',
            createdAt: new Date(),
            updatedAt: new Date(),
            isActive: true,
            reraLicenseNumber: reraLicenseNumber,
            reraVerifiedAt: new Date(),
            agencyName: agencyName,
            invitedById: null,
        };

        (prisma.user.update as vi.Mock).mockResolvedValue(mockUpdatedUser);

        const updatedUser = await userService.verifyRera(userId, reraLicenseNumber, agencyName);

        expect(prisma.user.update).toHaveBeenCalledWith({
            where: { id: userId },
            data: {
                reraLicenseNumber,
                agencyName,
                reraVerifiedAt: expect.any(Date),
            },
        });
        expect(updatedUser.reraLicenseNumber).toEqual(reraLicenseNumber);
        expect(updatedUser.agencyName).toEqual(agencyName);
        expect(updatedUser.reraVerifiedAt).toBeInstanceOf(Date);
    });

    it('should list users with pagination and role filter', async () => {
        const mockUsers = [
            { id: 'u1', email: 'u1@ex.com', fullName: 'User One', role: Dar360Role.TENANT, isActive: true, passwordHash: 'abc', createdAt: new Date(), updatedAt: new Date(), phone: '123', reraLicenseNumber: null, reraVerifiedAt: null, agencyName: null, invitedById: null },
            { id: 'u2', email: 'u2@ex.com', fullName: 'User Two', role: Dar360Role.TENANT, isActive: true, passwordHash: 'abc', createdAt: new Date(), updatedAt: new Date(), phone: '123', reraLicenseNumber: null, reraVerifiedAt: null, agencyName: null, invitedById: null },
        ];
        (prisma.user.findMany as vi.Mock).mockResolvedValue(mockUsers);
        (prisma.user.count as vi.Mock).mockResolvedValue(2);

        const result = await userService.listUsers(1, 10, Dar360Role.TENANT);

        expect(prisma.user.findMany).toHaveBeenCalledWith({
            where: { role: Dar360Role.TENANT },
            skip: 0,
            take: 10,
            select: expect.any(Object),
        });
        expect(prisma.user.count).toHaveBeenCalledWith({ where: { role: Dar360Role.TENANT } });
        expect(result.data).toEqual(mockUsers);
        expect(result.pagination).toEqual({ page: 1, limit: 10, total: 2, totalPages: 1 });
    });
  });
});