import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { propertyService } from '@/services/property.service';
import { updatePropertyStatusSchema } from '@/schemas/property.schema';

// PUT /api/properties/[id]/status - Update property status
export async function PUT(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const body = await request.json();
    const validated = updatePropertyStatusSchema.parse(body);

    const existingProperty = await propertyService.getPropertyById(id);
    if (!existingProperty) {
      return NextResponse.json({ error: 'Property not found' }, { status: 404 });
    }

    // Only the agent who created the property or an admin can update it
    if (existingProperty.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const updatedProperty = await propertyService.updatePropertyStatus(id, validated.status);
    return NextResponse.json({ data: updatedProperty });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('PUT /api/properties/[id]/status error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
