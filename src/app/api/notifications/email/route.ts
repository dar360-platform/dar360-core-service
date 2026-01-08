import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { authOptions } from '@/lib/auth';
import { notificationService } from '@/services/notification.service';
import { sendEmailSchema } from '@/schemas/notification.schema';

// POST /api/notifications/email - Send Email (internal)
export async function POST(request: NextRequest) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    // Further authorization can be added here if only specific roles or internal services can send email
    // For now, any authenticated user can technically trigger this if they know the endpoint.

    const body = await request.json();
    const validated = sendEmailSchema.parse(body);

    await notificationService.sendEmail(
      validated.to,
      validated.subject,
      validated.html,
      validated.templateCode || 'GENERIC_EMAIL',
      validated.text,
    );

    return NextResponse.json({ message: 'Email sent successfully' }, { status: 200 });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    console.error('POST /api/notifications/email error:', error);
    return NextResponse.json({ error: error.message || 'Internal server error' }, { status: 500 });
  }
}