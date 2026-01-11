import { Dar360Role } from "@prisma/client";
import { NextResponse } from "next/server";

import { requireUser } from "@/lib/auth";
import { toErrorResponse } from "@/lib/errors";
import { getInvitedOwners } from "@/services/user.service";

export async function GET() {
  try {
    const sessionUser = await requireUser([Dar360Role.AGENT]);
    const owners = await getInvitedOwners(sessionUser.id);
    return NextResponse.json(owners);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
