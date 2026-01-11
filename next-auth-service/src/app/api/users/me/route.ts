import { NextRequest, NextResponse } from "next/server";

import { requireUser } from "@/lib/auth";
import { toErrorResponse } from "@/lib/errors";
import { getUserById, updateCurrentUser } from "@/services/user.service";

export async function GET() {
  try {
    const sessionUser = await requireUser();
    const user = await getUserById(sessionUser.id);
    return NextResponse.json(user);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}

export async function PUT(request: NextRequest) {
  try {
    const sessionUser = await requireUser();
    const payload = await request.json();
    const user = await updateCurrentUser(sessionUser.id, payload);
    return NextResponse.json(user);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
