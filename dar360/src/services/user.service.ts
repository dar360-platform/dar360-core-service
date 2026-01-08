import prisma from '@/lib/db';
import { Dar360Role, User } from '@prisma/client';
import bcrypt from 'bcryptjs';

export class UserService {
  async createUser(data: {
    email: string;
    passwordPlain: string;
    fullName: string;
    phone: string;
    role?: Dar360Role;
    reraLicenseNumber?: string;
    agencyName?: string;
  }): Promise<Omit<User, 'passwordHash'>> {
    const passwordHash = await bcrypt.hash(data.passwordPlain, 12);
    const user = await prisma.user.create({
      data: {
        email: data.email.toLowerCase(),
        passwordHash,
        fullName: data.fullName,
        phone: data.phone,
        role: data.role || Dar360Role.TENANT,
        reraLicenseNumber: data.reraLicenseNumber,
        agencyName: data.agencyName,
      },
      select: {
        id: true,
        email: true,
        fullName: true,
        phone: true,
        role: true,
        createdAt: true,
        reraLicenseNumber: true,
        reraVerifiedAt: true,
        agencyName: true,
        isActive: true,
        invitedById: true,
        updatedAt: true,
      },
    });
    return user;
  }

  async verifyRera(userId: string, reraLicenseNumber: string, agencyName: string): Promise<User> {
    const updatedUser = await prisma.user.update({
      where: { id: userId },
      data: {
        reraLicenseNumber,
        agencyName,
        reraVerifiedAt: new Date(),
      },
    });
    return updatedUser;
  }

  async findUserById(id: string): Promise<User | null> {
    return prisma.user.findUnique({ where: { id } });
  }

  async findUserByEmail(email: string): Promise<User | null> {
    return prisma.user.findUnique({ where: { email: email.toLowerCase() } });
  }

  async listUsers(
    page: number,
    limit: number,
    role?: Dar360Role
  ): Promise<{ data: Omit<User, 'passwordHash'>[]; pagination: { page: number; limit: number; total: number; totalPages: number } }> {
    const where: { role?: Dar360Role } = {};
    if (role) {
      where.role = role;
    }

    const [users, total] = await Promise.all([
      prisma.user.findMany({
        where,
        skip: (page - 1) * limit,
        take: limit,
        select: {
          id: true,
          email: true,
          fullName: true,
          phone: true,
          role: true,
          reraLicenseNumber: true,
          reraVerifiedAt: true,
          agencyName: true,
          isActive: true,
          createdAt: true,
          invitedById: true,
          updatedAt: true,
        },
      }),
      prisma.user.count({ where }),
    ]);

    return {
      data: users,
      pagination: { page, limit, total, totalPages: Math.ceil(total / limit) },
    };
  }

  async updateUser(id: string, data: {
    fullName?: string;
    phone?: string;
    role?: Dar360Role;
    reraLicenseNumber?: string;
    agencyName?: string;
    isActive?: boolean;
  }): Promise<Omit<User, 'passwordHash'>> {
    const user = await prisma.user.update({
      where: { id },
      data,
      select: {
        id: true,
        email: true,
        fullName: true,
        phone: true,
        role: true,
        reraLicenseNumber: true,
        reraVerifiedAt: true,
        agencyName: true,
        isActive: true,
        invitedById: true,
        createdAt: true,
        updatedAt: true,
      },
    });
    return user;
  }

  async deleteUser(id: string): Promise<void> {
    await prisma.user.delete({ where: { id } });
  }

  async updateUserStatus(id: string, isActive: boolean): Promise<User> {
    return prisma.user.update({
      where: { id },
      data: { isActive },
    });
  }
}

export const userService = new UserService();
