import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { contractService } from '@/services/contract.service';
import { createContractSchema, searchContractSchema } from '@/schemas/contract.schema';
import { normalizeContractInput, toFrontendContract } from '@/lib/frontend-mappers';

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

    if (session.user.role === 'ADMIN') {
      // Admins can see all contracts, so no filter is applied.
    } else if (session.user.role === 'AGENT') {
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

    const mapped = data.map((contract) => toFrontendContract(contract));
    return NextResponse.json({ data: mapped, pagination });
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
    const normalizedBody = normalizeContractInput(body);
    const validated = createContractSchema.parse({
      ...normalizedBody,
      agentId: session.user.id,
    });

    const contract = await contractService.create({
      ...validated,
      agentId: session.user.id, // Assign current agent as creator
    });

    const fullContract = await contractService.getContractById(contract.id);
    const responseContract = fullContract
      ? toFrontendContract(fullContract)
      : toFrontendContract({ ...(contract as any), property: null });

    return NextResponse.json({ data: responseContract }, { status: 201 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/contracts error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
