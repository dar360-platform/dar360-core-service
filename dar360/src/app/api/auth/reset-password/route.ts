import { NextRequest, NextResponse } from 'next/server';
import { userService } from '@/services/user.service';
import { z } from 'zod';

const resetPasswordSchema = z.object({
  token: z.string().min(1),
  newPassword: z.string().min(8, 'Password must be at least 8 characters long'),
});

// POST /api/auth/reset-password
export async function POST(request: NextRequest) {
  try {
    const body = await request.json();
    const { token, newPassword } = resetPasswordSchema.parse(body);

    await userService.resetPassword(token, newPassword);

    return NextResponse.json({ message: 'Password has been reset successfully.' });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    if (error.message === 'Invalid or expired token') {
      return NextResponse.json({ error: 'Invalid or expired token' }, { status: 400 });
    }
    console.error('POST /api/auth/reset-password error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
