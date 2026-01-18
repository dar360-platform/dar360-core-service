import { NextRequest, NextResponse } from "next/server";

import { toErrorResponse } from "@/lib/errors";
import { requestPasswordReset } from "@/services/user.service";

export async function POST(request: NextRequest) {
  try {
    const payload = await request.json();
    const result = await requestPasswordReset(payload);
    return NextResponse.json(result, { status: 200 });
  } catch (error) {
    const { status, body } = toErrorResponse(error);
    return NextResponse.json(body, { status });
  }
}
