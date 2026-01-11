import { Dar360Role } from "@prisma/client";
import { NextRequest, NextResponse } from "next/server";

import { requireUser } from "@/lib/auth";
import { toErrorResponse } from "@/lib/errors";
import { deleteUser, getUserById, updateUser } from "@/services/user.service";

export async function GET(_request: NextRequest, { params }: { params: { id: string } }) {
  try {
    await requireUser([Dar360Role.AGENT]);
    const user = await getUserById(params.id);
    return NextResponse.json(user);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}

export async function PUT(request: NextRequest, { params }: { params: { id: string } }) {
  try {
    await requireUser([Dar360Role.AGENT]);
    const payload = await request.json();
    const user = await updateUser(params.id, payload);
    return NextResponse.json(user);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}

export async function DELETE(_request: NextRequest, { params }: { params: { id: string } }) {
  try {
    await requireUser([Dar360Role.AGENT]);
    await deleteUser(params.id);
    return NextResponse.json({ success: true });
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
