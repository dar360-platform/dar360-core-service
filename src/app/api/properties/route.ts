import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { propertyService } from '@/services/property.service';
import { createPropertySchema, searchPropertySchema } from '@/schemas/property.schema';
import { normalizePropertyInput, toFrontendProperty } from '@/lib/frontend-mappers';

// GET /api/properties - List properties
export async function GET(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);

    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const rawParams = Object.fromEntries(searchParams);
    if (rawParams.area && !rawParams.areaName) {
      rawParams.areaName = rawParams.area;
    }

    const validatedParams = searchPropertySchema.parse(rawParams);

    const { data, pagination } = await propertyService.search({
      agentId: session.user.role === 'AGENT' ? session.user.id : undefined, // Agents can only see their properties
      ownerId: session.user.role === 'OWNER' ? session.user.id : undefined, // Owners can only see their properties
      ...validatedParams,
    });

    const mapped = data.map((property) => toFrontendProperty(property));
    return NextResponse.json({ data: mapped, pagination });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('GET /api/properties error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

// POST /api/properties - Create property
export async function POST(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);

    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    // Only agents can create properties
    if (session.user.role !== 'AGENT') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const body = await request.json();
    const normalizedInput = normalizePropertyInput(body);
    const validated = createPropertySchema.parse(normalizedInput);

    const property = await propertyService.create({
      ...validated,
      agentId: session.user.id, // Assign current agent as creator
    });

    const fullProperty = await propertyService.getPropertyById(property.id);
    const responseProperty = fullProperty
      ? toFrontendProperty(fullProperty)
      : toFrontendProperty({ ...(property as any), images: [], owner: null, agent: null, _count: { viewings: 0 } });

    return NextResponse.json({ data: responseProperty }, { status: 201 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/properties error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
