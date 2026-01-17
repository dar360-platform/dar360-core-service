import { NextRequest, NextResponse } from 'next/server';
import prisma from '@/lib/db';
import bcrypt from 'bcryptjs';
import { z } from 'zod';

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(1),
});

// POST /api/auth/login
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { email, password } = loginSchema.parse(body);

    const normalizedEmail = email.toLowerCase();

    // Get IP and User Agent for logging
    const ipAddress = request.headers.get('x-forwarded-for') || 'unknown';
    const userAgent = request.headers.get('user-agent') || 'unknown';

    // Find user
    const user = await prisma.user.findUnique({
      where: { email: normalizedEmail },
    });

    if (!user || !user.isActive) {
      await prisma.loginHistory.create({
        data: {
          email: normalizedEmail,
          ipAddress,
          userAgent,
          status: 'FAILURE',
          reason: user ? 'Account inactive' : 'User not found',
        },
      });
      return NextResponse.json(
        { error: 'Invalid credentials' },
        { status: 401 }
      );
    }

    // Verify password
    const isValid = await bcrypt.compare(password, user.passwordHash);
    if (!isValid) {
      await prisma.loginHistory.create({
        data: {
          userId: user.id,
          email: normalizedEmail,
          ipAddress,
          userAgent,
          status: 'FAILURE',
          reason: 'Invalid password',
        },
      });
      return NextResponse.json(
        { error: 'Invalid credentials' },
        { status: 401 }
      );
    }

    // Log successful login
    await prisma.loginHistory.create({
      data: {
        userId: user.id,
        email: normalizedEmail,
        ipAddress,
        userAgent,
        status: 'SUCCESS',
      },
    });

    // Return user data (excluding sensitive info)
    return NextResponse.json({
      data: {
        id: user.id,
        email: user.email,
        fullName: user.fullName,
        phone: user.phone,
        role: user.role,
        reraLicenseNumber: user.reraLicenseNumber,
        reraVerifiedAt: user.reraVerifiedAt,
        agencyName: user.agencyName,
        isActive: user.isActive,
        createdAt: user.createdAt,
      },
      message: 'Login successful',
    });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json(
        { error: 'Validation failed', details: error.errors },
        { status: 400 }
      );
    }
    console.error('POST /api/auth/login error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
