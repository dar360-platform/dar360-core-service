import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { contractService } from '@/services/contract.service';
import { updateContractSchema } from '@/schemas/contract.schema';
import { normalizeContractInput, toFrontendContract } from '@/lib/frontend-mappers';

// GET /api/contracts/[id] - Get contract by ID
export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const contract = await contractService.getContractById(id);

    if (!contract) {
      return NextResponse.json({ error: 'Contract not found' }, { status: 404 });
    }

    // Agent or Owner of the contract, or Tenant if authenticated, can view.
    // For now, only agent/owner, public for verify-otp for tenant.
    if (contract.agentId !== session.user.id && contract.ownerId !== session.user.id && session.user.role !== 'ADMIN') {
        return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    return NextResponse.json({ data: toFrontendContract(contract) });
  } catch (error) {
    console.error('GET /api/contracts/[id] error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

// PUT /api/contracts/[id] - Update contract by ID
export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const body = await request.json();
    const normalizedBody = normalizeContractInput(body);
    const validated = updateContractSchema.parse(normalizedBody);

    const existingContract = await contractService.getContractById(id);
    if (!existingContract) {
      return NextResponse.json({ error: 'Contract not found' }, { status: 404 });
    }

    // Only the agent who created the contract or an admin can update it
    if (existingContract.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const updatedContract = await contractService.updateContract(id, validated);
    return NextResponse.json({ data: toFrontendContract(updatedContract) });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('PUT /api/contracts/[id] error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
