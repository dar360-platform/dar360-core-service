import prisma from '@/lib/db';
import { Dar360Role, User } from '@prisma/client';
import bcrypt from 'bcryptjs';
import { nanoid } from 'nanoid';
import { notificationService } from './notification.service';

export class UserService {
  async createUser(data: {
    email: string;
    passwordPlain: string;
    fullName: string;
    phone: string;
    role?: Dar360Role;
    reraLicenseNumber?: string;
    agencyName?: string;
    emiratesId?: string;
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
        emiratesId: data.emiratesId,
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
        emiratesId: true,
        isActive: true,
        invitedById: true,
        updatedAt: true,
        resetToken: true,
        resetTokenExpiresAt: true,
      },
    });
    return user;
  }

  // ... (previous methods)

  async forgotPassword(email: string): Promise<void> {
    const user = await prisma.user.findUnique({
      where: { email: email.toLowerCase() },
    });

    if (!user) return; // Do nothing if user not found for security

    const resetToken = nanoid(32);
    const resetTokenExpiresAt = new Date(Date.now() + 60 * 60 * 1000); // 1 hour

    await prisma.user.update({
      where: { id: user.id },
      data: {
        resetToken,
        resetTokenExpiresAt,
      },
    });

    const resetUrl = `${process.env.NEXT_PUBLIC_APP_URL}/auth/reset-password?token=${resetToken}`;
    
    await notificationService.sendEmail(
      user.email,
      'Reset your password',
      `<p>Click <a href="${resetUrl}">here</a> to reset your password. This link is valid for 1 hour.</p>`,
      'PASSWORD_RESET',
      `Click this link to reset your password: ${resetUrl}`
    );
  }

  async resetPassword(token: string, newPasswordPlain: string): Promise<void> {
    const user = await prisma.user.findFirst({
      where: {
        resetToken: token,
        resetTokenExpiresAt: { gt: new Date() },
      },
    });

    if (!user) {
      throw new Error('Invalid or expired token');
    }

    const passwordHash = await bcrypt.hash(newPasswordPlain, 12);

    await prisma.user.update({
      where: { id: user.id },
      data: {
        passwordHash,
        resetToken: null,
        resetTokenExpiresAt: null,
      },
    });
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
          emiratesId: true,
          isActive: true,
          createdAt: true,
          invitedById: true,
          updatedAt: true,
          resetToken: true,
          resetTokenExpiresAt: true,
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
    emiratesId?: string;
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
        emiratesId: true,
        isActive: true,
        invitedById: true,
        createdAt: true,
        updatedAt: true,
        resetToken: true,
        resetTokenExpiresAt: true,
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
