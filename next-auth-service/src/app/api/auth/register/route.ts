import { NextRequest, NextResponse } from "next/server";

import { toErrorResponse } from "@/lib/errors";
import { registerUser } from "@/services/user.service";

export async function POST(request: NextRequest) {
  try {
    const json = await request.json();
    const user = await registerUser(json);
    return NextResponse.json(user, { status: 201 });
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
