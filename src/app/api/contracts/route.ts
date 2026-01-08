import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { contractService } from '@/services/contract.service';
import { createContractSchema, searchContractSchema } from '@/schemas/contract.schema';
import { ContractStatus } from '@prisma/client';

// GET /api/contracts - List contracts
export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const validatedParams = searchContractSchema.parse(Object.fromEntries(searchParams));

    let agentIdFilter: string | undefined = undefined;
    let ownerIdFilter: string | undefined = undefined;

    if (session.user.role === 'AGENT') {
      agentIdFilter = session.user.id;
    } else if (session.user.role === 'OWNER') {
      ownerIdFilter = session.user.id;
    } else {
        // Tenants might be able to see their own contracts, but not implemented yet.
        return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const { data, pagination } = await contractService.searchContracts({
      ...validatedParams,
      agentId: agentIdFilter,
      ownerId: ownerIdFilter,
    });

    return NextResponse.json({ data, pagination });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('GET /api/contracts error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

// POST /api/contracts - Create contract
export async function POST(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    // Only agents can create contracts
    if (session.user.role !== 'AGENT') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const body = await request.json();
    const validated = createContractSchema.parse(body);

    const contract = await contractService.create({
      ...validated,
      agentId: session.user.id, // Assign current agent as creator
    });

    return NextResponse.json({ data: contract }, { status: 201 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/contracts error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}