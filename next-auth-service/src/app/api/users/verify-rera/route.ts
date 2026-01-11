import { NextRequest, NextResponse } from "next/server";

import { requireUser } from "@/lib/auth";
import { toErrorResponse } from "@/lib/errors";
import { verifyRera } from "@/services/user.service";

export async function POST(request: NextRequest) {
  try {
    const sessionUser = await requireUser();
    const payload = await request.json();
    const user = await verifyRera(sessionUser.id, payload);
    return NextResponse.json(user);
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
