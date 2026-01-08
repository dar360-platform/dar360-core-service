import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { contractService } from '@/services/contract.service';
import { sendOtpSchema } from '@/schemas/contract.schema'; // Although empty, good practice to import if exists

// POST /api/contracts/[id]/send-otp - Send OTP
export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;

    const existingContract = await contractService.getContractById(id);
    if (!existingContract) {
      return NextResponse.json({ error: 'Contract not found' }, { status: 404 });
    }

    // Only the agent who created the contract or an admin can send OTP
    if (existingContract.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    // No body validation needed for sendOtpSchema as it's empty
    // const body = await request.json();
    // sendOtpSchema.parse(body);

    const result = await contractService.sendOtp(id);

    return NextResponse.json({ message: result.message });
  } catch (error: any) {
    console.error('POST /api/contracts/[id]/send-otp error:', error);
    return NextResponse.json({ error: error.message || 'Internal server error' }, { status: 500 });
  }
}