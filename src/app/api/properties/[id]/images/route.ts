import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { propertyService } from '@/services/property.service';
import { uploadPropertyImageSchema } from '@/schemas/property.schema';
import { getSignedUploadUrl } from '@/lib/s3';

// POST /api/properties/[id]/images - Upload images
export async function POST(request: NextRequest, { params }: { params: Promise<{ id: string }> }) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id: propertyId } = await params;

    const existingProperty = await propertyService.getPropertyById(propertyId);
    if (!existingProperty) {
      return NextResponse.json({ error: 'Property not found' }, { status: 404 });
    }

    // Only the agent who created the property or an admin can upload images
    if (existingProperty.agentId !== session.user.id && session.user.role !== 'ADMIN') {
      return NextResponse.json({ error: 'Forbidden' }, { status: 403 });
    }

    const body = await request.json();
    const validated = uploadPropertyImageSchema.parse(body);

    const s3Key = `properties/${propertyId}/${validated.fileName}`;
    const signedUrl = await getSignedUploadUrl(s3Key, validated.contentType);

    // Save image metadata to the database
    const newImage = await propertyService.addPropertyImage(
      propertyId,
      `https://${process.env.AWS_S3_BUCKET}.s3.${process.env.AWS_REGION}.amazonaws.com/${s3Key}`,
      s3Key,
      validated.isPrimary,
      validated.displayOrder,
    );

    return NextResponse.json({
      data: {
        ...newImage,
        signedUrl, // Return signed URL for client to upload to S3 directly
      },
      message: 'Signed URL generated for image upload',
    });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/properties/[id]/images error:', error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}