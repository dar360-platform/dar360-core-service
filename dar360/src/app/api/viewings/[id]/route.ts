import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { viewingService } from '@/services/viewing.service';
import { updateViewingSchema } from '@/schemas/viewing.schema';

// GET /api/viewings/[id] - Get viewing by ID
export async function GET(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const viewing = await viewingService.getViewingById(id);

    if (!viewing) {
      return NextResponse.json({ error: 'Viewing not found' }, { status: 404 });
    }

    // Only agent who scheduled the viewing or an admin can view/update/delete
    if (viewing.agentId !== session.user.id && session.user.role !== 'ADMIN') {
        return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    return NextResponse.json({ data: viewing });
  } catch (error) {
    console.error('GET /api/viewings/[id] error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

// PUT /api/viewings/[id] - Update viewing by ID
export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const body = await request.json();
    const validated = updateViewingSchema.parse(body);

    const existingViewing = await viewingService.getViewingById(id);
    if (!existingViewing) {
      return NextResponse.json({ error: 'Viewing not found' }, { status: 404 });
    }

    // Only agent who scheduled the viewing or an admin can update
    if (existingViewing.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const updatedViewing = await viewingService.updateViewing(id, validated);
    return NextResponse.json({ data: updatedViewing });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('PUT /api/viewings/[id] error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

// DELETE /api/viewings/[id] - Cancel viewing by ID
export async function DELETE(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;

    const existingViewing = await viewingService.getViewingById(id);
    if (!existingViewing) {
      return NextResponse.json({ error: 'Viewing not found' }, { status: 404 });
    }

    // Only agent who scheduled the viewing or an admin can delete
    if (existingViewing.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    await viewingService.deleteViewing(id);
    return NextResponse.json({ message: 'Viewing cancelled successfully' }, { status: 200 });
  } catch (error) {
    console.error('DELETE /api/viewings/[id] error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}