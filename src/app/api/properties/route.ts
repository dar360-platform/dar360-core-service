import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { propertyService } from '@/services/property.service';
import { createPropertySchema, searchPropertySchema } from '@/schemas/property.schema';

// GET /api/properties - List properties
export async function GET(request: NextRequest) {
  try {
    let session = await getServerSession(authOptions);

    // Bypassing Authentication for Development
    if (process.env.NODE_ENV === 'development' && !session) {
      session = {
        user: {
          id: 'clerk_user_id_placeholder', // mock user id
          role: 'ADMIN', // Using ADMIN to see all properties in dev
          name: 'Dev Admin',
          email: 'dev@admin.com',
          reraVerified: true,
        },
        expires: '2099-01-01T00:00:00.000Z',
      };
    }

    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { searchParams } = new URL(request.url);
    const validatedParams = searchPropertySchema.parse(Object.fromEntries(searchParams));

    const { data, pagination } = await propertyService.search({
      agentId: session.user.role === 'AGENT' ? session.user.id : undefined, // Agents can only see their properties
      ownerId: session.user.role === 'OWNER' ? session.user.id : undefined, // Owners can only see their properties
      ...validatedParams,
    });

    return NextResponse.json({ data, pagination });
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
    let session = await getServerSession(authOptions);

    // Bypassing Authentication for Development
    if (process.env.NODE_ENV === 'development' && !session) {
      session = {
        user: {
          id: 'clerk_user_id_placeholder', // mock user id
          role: 'AGENT',
          name: 'Dev Agent',
          email: 'dev@agent.com',
          reraVerified: true,
        },
        expires: '2099-01-01T00:00:00.000Z',
      };
    }
    
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    // Only agents can create properties
    if (session.user.role !== 'AGENT') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const body = await request.json();
    const validated = createPropertySchema.parse(body);

    const property = await propertyService.create({
      ...validated,
      agentId: session.user.id, // Assign current agent as creator
    });

    return NextResponse.json({ data: property }, { status: 201 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/properties error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
