import { NextRequest, NextResponse } from 'next/server';
import prisma from '@/lib/db';
import bcrypt from 'bcryptjs';
import { z } from 'zod';
import { Dar360Role } from '@prisma/client';

const registerSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8, 'Password must be at least 8 characters long'),
  role: z.nativeEnum(Dar360Role),

  // Optional fields that can come from the frontend form
  firstName: z.string().optional(),
  lastName: z.string().optional(),
  fullName: z.string().optional(),
  phone: z.string().optional(),

  // Agent-specific fields
  reraBrokerId: z.string().optional(), // Frontend field name
  reraLicenseNumber: z.string().optional(), // Backend field name
  agencyName: z.string().optional(),

  // Owner/Tenant-specific fields
  emiratesId: z.string().optional(),
}).refine((data) => {
  // Require fullName or (firstName and lastName)
  return data.fullName || (data.firstName && data.lastName);
}, {
  message: 'Either fullName or both firstName and lastName are required',
  path: ['fullName'],
});

// POST /api/auth/register
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const validated = registerSchema.parse(body);

    const normalizedEmail = validated.email.toLowerCase();

    // Check if user already exists
    const existingUser = await prisma.user.findUnique({
      where: { email: normalizedEmail },
    });

    if (existingUser) {
      return NextResponse.json(
        { error: 'Email already registered' },
        { status: 409 }
      );
    }

    // Construct fullName from firstName and lastName if needed
    const fullName = validated.fullName ||
      `${validated.firstName} ${validated.lastName}`.trim();

    // Handle reraBrokerId vs reraLicenseNumber (frontend uses reraBrokerId)
    const reraLicenseNumber = validated.reraLicenseNumber || validated.reraBrokerId;

    // Validate role-specific required fields
    if (validated.role === Dar360Role.AGENT) {
      if (!validated.phone) {
        return NextResponse.json(
          { error: 'Phone number is required for agents' },
          { status: 400 }
        );
      }
      if (!reraLicenseNumber) {
        return NextResponse.json(
          { error: 'RERA Broker ID is required for agents' },
          { status: 400 }
        );
      }
      if (!validated.agencyName) {
        return NextResponse.json(
          { error: 'Agency name is required for agents' },
          { status: 400 }
        );
      }
    }

    if (validated.role === Dar360Role.OWNER || validated.role === Dar360Role.TENANT) {
      if (!validated.phone) {
        return NextResponse.json(
          { error: 'Phone number is required' },
          { status: 400 }
        );
      }
      if (!validated.emiratesId) {
        return NextResponse.json(
          { error: 'Emirates ID is required for owners and tenants' },
          { status: 400 }
        );
      }
    }

    // Hash password
    const passwordHash = await bcrypt.hash(validated.password, 12);

    // Create user
    const user = await prisma.user.create({
      data: {
        email: normalizedEmail,
        passwordHash,
        fullName,
        phone: validated.phone || '',
        role: validated.role,
        reraLicenseNumber,
        agencyName: validated.agencyName,
        emiratesId: validated.emiratesId,
      },
      select: {
        id: true,
        email: true,
        fullName: true,
        phone: true,
        role: true,
        reraLicenseNumber: true,
        agencyName: true,
        emiratesId: true,
        isActive: true,
        createdAt: true,
      },
    });

    return NextResponse.json({
      data: user,
      message: 'Registration successful',
    }, { status: 201 });
  } catch (error: any) {
    if (error.code === 'P2002') {
      return NextResponse.json(
        { error: 'Email already registered' },
        { status: 409 }
      );
    }
    if (error.name === 'ZodError') {
      return NextResponse.json(
        { error: 'Validation failed', details: error.errors },
        { status: 400 }
      );
    }
    console.error('POST /api/auth/register error:', error);
    return NextResponse.json(
      { error: 'Internal server error' },
      { status: 500 }
    );
  }
}
