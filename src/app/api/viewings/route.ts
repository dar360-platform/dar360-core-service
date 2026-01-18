import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { viewingService } from '@/services/viewing.service';
import { createViewingSchema, searchViewingSchema } from '@/schemas/viewing.schema';
import { combineDateAndTime, toFrontendViewing } from '@/lib/frontend-mappers';

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

    const mapped = data.map((viewing) => toFrontendViewing(viewing));
    return NextResponse.json({ data: mapped, pagination });
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
    const normalizedBody = {
      ...body,
      ...(body.scheduledAt
        ? {}
        : body.date && body.time
          ? { scheduledAt: combineDateAndTime(body.date, body.time) }
          : {}),
    };
    delete (normalizedBody as any).date;
    delete (normalizedBody as any).time;

    const validated = createViewingSchema.parse(normalizedBody);

    const viewing = await viewingService.createViewing({
      ...validated,
      agentId: session.user.id, // Assign current agent as scheduler
    });

    const fullViewing = await viewingService.getViewingById(viewing.id);
    const responseViewing = fullViewing
      ? toFrontendViewing(fullViewing)
      : toFrontendViewing({ ...(viewing as any), property: null });

    return NextResponse.json({ data: responseViewing }, { status: 201 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/viewings error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
