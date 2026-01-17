import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import prisma from '@/lib/db';
import { authOptions } from '@/lib/auth';

// POST /api/auth/logout
export async function POST(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);

    // Log logout event even if no session (for tracking)
    if (session?.user?.id) {
      const ipAddress = request.headers.get('x-forwarded-for') || 'unknown';
      const userAgent = request.headers.get('user-agent') || 'unknown';

      await prisma.loginHistory.create({
        data: {
          userId: session.user.id,
          email: session.user.email || 'unknown',
          ipAddress,
          userAgent,
          status: 'LOGOUT',
          reason: 'User initiated logout',
        },
      });
    }

    // NextAuth handles session invalidation on client side via signOut()
    // This endpoint is mainly for logging purposes
    return NextResponse.json({
      message: 'Logged out successfully',
    });
  } catch (error) {
    console.error('POST /api/auth/logout error:', error);
    // Still return success even if logging fails
    return NextResponse.json({
      message: 'Logged out successfully',
    });
  }
}
