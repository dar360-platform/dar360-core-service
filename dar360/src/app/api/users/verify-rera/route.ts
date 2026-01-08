import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import prisma from '@/lib/db';
import { authOptions } from '@/lib/auth';
import { z } from 'zod';

const reraSchema = z.object({
  reraLicenseNumber: z.string().min(1).max(50),
  agencyName: z.string().min(1).max(255),
});

// POST /api/users/verify-rera
export async function POST(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const user = await prisma.user.findUnique({
      where: { id: session.user.id },
    });

    if (!user || user.role !== 'AGENT') {
      return NextResponse.json({ error: 'Only agents can verify RERA' }, { status: 403 });
    }

    if (user.reraVerifiedAt) {
      return NextResponse.json({ error: 'Already RERA verified' }, { status: 400 });
    }

    const body = await request.json();
    const { reraLicenseNumber, agencyName } = reraSchema.parse(body);

    // Check if RERA number already used
    const existing = await prisma.user.findFirst({
      where: { reraLicenseNumber },
    });

    if (existing) {
      return NextResponse.json({ error: 'RERA license already registered' }, { status: 409 });
    }

    // TODO: Integrate with DLD API for real verification
    // For MVP, auto-approve
    const updated = await prisma.user.update({
      where: { id: user.id },
      data: {
        reraLicenseNumber,
        agencyName,
        reraVerifiedAt: new Date(),
      },
      select: {
        id: true,
        email: true,
        fullName: true,
        reraLicenseNumber: true,
        reraVerifiedAt: true,
        agencyName: true,
      },
    });

    return NextResponse.json({
      data: updated,
      message: 'RERA verification successful',
    });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/users/verify-rera error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
