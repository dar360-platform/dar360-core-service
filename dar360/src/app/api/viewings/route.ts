import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { viewingService } from '@/services/viewing.service';
import { createViewingSchema, searchViewingSchema } from '@/schemas/viewing.schema';

// GET /api/viewings - List viewings
export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const validatedParams = searchViewingSchema.parse(Object.fromEntries(searchParams));

    // Only agents can see all viewings or their own
    let agentIdFilter: string | undefined = undefined;
    if (session.user.role === 'AGENT') {
      agentIdFilter = session.user.id;
    } else if (session.user.role === 'OWNER') {
        // Owners can only see viewings related to their properties, not implemented yet
        // For now, restrict owners from viewing all viewings.
        return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const { data, pagination } = await viewingService.searchViewings({
      ...validatedParams,
      agentId: agentIdFilter,
    });

    return NextResponse.json({ data, pagination });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('GET /api/viewings error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

// POST /api/viewings - Schedule viewing
export async function POST(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    // Only agents can schedule viewings
    if (session.user.role !== 'AGENT') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const body = await request.json();
    const validated = createViewingSchema.parse(body);

    const viewing = await viewingService.createViewing({
      ...validated,
      agentId: session.user.id, // Assign current agent as scheduler
    });

    return NextResponse.json({ data: viewing }, { status: 201 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/viewings error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}