import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { viewingService } from '@/services/viewing.service';
import { updateViewingOutcomeSchema } from '@/schemas/viewing.schema';
import { normalizeViewingOutcomeInput, toFrontendViewing } from '@/lib/frontend-mappers';

// PUT /api/viewings/[id]/outcome - Record outcome
export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const body = await request.json();
    const validated = updateViewingOutcomeSchema.parse(body);

    const existingViewing = await viewingService.getViewingById(id);
    if (!existingViewing) {
      return NextResponse.json({ error: 'Viewing not found' }, { status: 404 });
    }

    // Only agent who scheduled the viewing or an admin can update outcome
    if (existingViewing.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const normalizedOutcome = normalizeViewingOutcomeInput(validated.outcome);
    await viewingService.updateViewingOutcome(
      id,
      normalizedOutcome.outcome,
      validated.notes,
      normalizedOutcome.status,
    );
    const updatedViewing = await viewingService.getViewingById(id);
    if (!updatedViewing) {
      return NextResponse.json({ error: 'Viewing not found' }, { status: 404 });
    }
    return NextResponse.json({ data: toFrontendViewing(updatedViewing) });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('PUT /api/viewings/[id]/outcome error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
