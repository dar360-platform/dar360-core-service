import { NextRequest, NextResponse } from "next/server";

import { toErrorResponse } from "@/lib/errors";
import { resetPassword } from "@/services/user.service";

export async function POST(request: NextRequest) {
  try {
    const payload = await request.json();
    const user = await resetPassword(payload);
    return NextResponse.json(user, { status: 200 });
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
