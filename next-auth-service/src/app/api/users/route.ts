import { Dar360Role } from "@prisma/client";
import { NextRequest, NextResponse } from "next/server";

import { requireUser } from "@/lib/auth";
import { toErrorResponse } from "@/lib/errors";
import { createUser, listUsers } from "@/services/user.service";

export async function GET(request: NextRequest) {
  try {
    await requireUser([Dar360Role.AGENT]);
    const query = Object.fromEntries(request.nextUrl.searchParams.entries());
    const users = await listUsers(query);
    return NextResponse.json(users);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}

export async function POST(request: NextRequest) {
  try {
    const sessionUser = await requireUser([Dar360Role.AGENT]);
    const json = await request.json();
    const user = await createUser(json, sessionUser.id);
    return NextResponse.json(user, { status: 201 });
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
