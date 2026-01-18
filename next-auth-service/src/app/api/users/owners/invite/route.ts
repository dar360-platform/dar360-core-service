import { Dar360Role } from "@prisma/client";
import { NextRequest, NextResponse } from "next/server";

import { requireUser } from "@/lib/auth";
import { toErrorResponse } from "@/lib/errors";
import { inviteOwner } from "@/services/user.service";

export async function POST(request: NextRequest) {
  try {
    const sessionUser = await requireUser([Dar360Role.AGENT]);
    const payload = await request.json();
    const owner = await inviteOwner(sessionUser.id, payload);
    return NextResponse.json(owner, { status: 201 });
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
