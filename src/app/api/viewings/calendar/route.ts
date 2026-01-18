import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { viewingService } from '@/services/viewing.service';
import { toFrontendViewing } from '@/lib/frontend-mappers';
import { z } from 'zod';

const calendarViewQuerySchema = z.object({
  start: z.coerce.date(),
  end: z.coerce.date(),
});

// GET /api/viewings/calendar?start=...&end=... - Get viewings for calendar
export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const validated = calendarViewQuerySchema.parse(Object.fromEntries(searchParams));

    let agentIdFilter: string | undefined = undefined;
    if (session.user.role === 'AGENT') {
      agentIdFilter = session.user.id;
    }

    const viewings = await viewingService.getCalendarViewings(
      validated.start,
      validated.end,
      agentIdFilter
    );

    return NextResponse.json({ data: viewings.map((viewing) => toFrontendViewing(viewing)) });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('GET /api/viewings/calendar error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
