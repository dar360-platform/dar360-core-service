import { NextRequest, NextResponse } from 'next/server';
import { propertyService } from '@/services/property.service';
import { toFrontendProperty } from '@/lib/frontend-mappers';

// GET /api/properties/share/[token] - View shared property
export async function GET(request: NextRequest, { params }: { params: Promise<{ token: string }> }) {
  try {
    const { token } = await params;
    const share = await propertyService.trackShareClick(token);

    if (!share || !share.property) {
      return NextResponse.json({ error: 'Shared property not found or expired' }, { status: 404 });
    }

    return NextResponse.json({ data: toFrontendProperty(share.property) });
  } catch (error) {
    console.error('GET /api/properties/share/[token] error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
