import { NextRequest, NextResponse } from 'next/server';
import { getServerSession } from 'next-auth';
import { userService } from '@/services/user.service';
import { updateUserSchema } from '@/schemas/user.schema';
import { authOptions } from '@/lib/auth';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const user = await userService.findUserById(id);

    if (!user) {
      return NextResponse.json({ error: 'User not found' }, { status: 404 });
    }

    // Exclude passwordHash
    const { passwordHash, ...userWithoutPassword } = user;

    return NextResponse.json({ data: userWithoutPassword });
  } catch (error) {
    console.error(`GET /api/users/[id] error:`, error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

export async function PUT(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }

    const { id } = await params;
    const body = await request.json();
    
    // Validate body
    const validated = updateUserSchema.parse(body);

    const updatedUser = await userService.updateUser(id, validated);

    return NextResponse.json({ data: updatedUser });
  } catch (error: any) {
    if (error.name === 'ZodError') {
      return NextResponse.json({ error: 'Validation failed', details: error.errors }, { status: 400 });
    }
    // Handle Prisma not found error
    if (error.code === 'P2025') {
        return NextResponse.json({ error: 'User not found' }, { status: 404 });
    }
    console.error(`PUT /api/users/[id] error:`, error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}

export async function DELETE(
  request: NextRequest,
  { params }: { params: Promise<{ id: string }> }
) {
  try {
    const session = await getServerSession(authOptions);
    if (!session) {
      return NextResponse.json({ error: 'Unauthorized' }, { status: 401 });
    }
    // TODO: Add role check (e.g. only ADMIN)

    const { id } = await params;
    await userService.deleteUser(id);

    return NextResponse.json({ success: true }, { status: 200 });
  } catch (error: any) {
    if (error.code === 'P2025') {
         return NextResponse.json({ error: 'User not found' }, { status: 404 });
    }
    console.error(`DELETE /api/users/[id] error:`, error);
    return NextResponse.json({ error: 'Internal server error' }, { status: 500 });
  }
}
