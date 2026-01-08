import { NextRequest, NextResponse } from 'next/server';
import { contractService } from '@/services/contract.service';
import { verifyOtpSchema } from '@/schemas/contract.schema';

// POST /api/contracts/[id]/verify-otp - Verify & sign
export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const { id } = await params;
    const body = await request.json();
    const validated = verifyOtpSchema.parse(body);

    const ipAddress = (request as any).ip || request.headers.get('x-forwarded-for') || 'unknown';

    const result = await contractService.verifyOtpAndSign(id, validated.otp, ipAddress);

    return NextResponse.json({ message: result.message, contract: result.contract });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/contracts/[id]/verify-otp error:', error);
    return NextResponse.json({ error: error.message || 'Internal server error' }, { status: 500 });
  }
}