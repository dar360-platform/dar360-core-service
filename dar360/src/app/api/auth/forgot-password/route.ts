import { NextRequest, NextResponse } from 'next/server';
import { userService } from '@/services/user.service';
import { z } from 'zod';

const forgotPasswordSchema = z.object({
  email: z.string().email(),
});

// POST /api/auth/forgot-password
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { email } = forgotPasswordSchema.parse(body);

    await userService.forgotPassword(email);

    // Always return OK to prevent email enumeration
    return NextResponse.json({ message: 'If an account with that email exists, we sent you a reset link.' });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Invalid email address' }, { status: 400 });
    }
    console.error('POST /api/auth/forgot-password error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
