import { NextResponse } from "next/server";
import type { NextRequest } from "next/server";
import { getToken } from "next-auth/jwt";

const RATE_LIMIT_WINDOW_MS = 60_000;
const RATE_LIMIT_MAX = Number(process.env.RATE_LIMIT_MAX ?? 120);
const rateLimitStore = new Map<string, { count: number; expiresAt: number }>();

function applyRateLimiting(request: NextRequest) {
  const ip =
    request.ip ??
    request.headers.get("x-forwarded-for")?.split(",")[0]?.trim() ??
    "unknown";
  const now = Date.now();
  const existing = rateLimitStore.get(ip);

  if (!existing || existing.expiresAt < now) {
    rateLimitStore.set(ip, { count: 1, expiresAt: now + RATE_LIMIT_WINDOW_MS });
    return true;
  }

  if (existing.count >= RATE_LIMIT_MAX) {
    return false;
  }

  existing.count += 1;
  rateLimitStore.set(ip, existing);
  return true;
}

export async function middleware(request: NextRequest) {
  if (!applyRateLimiting(request)) {
    return NextResponse.json({ error: "Too many requests" }, { status: 429 });
  }

  const { pathname } = request.nextUrl;
  if (pathname.startsWith("/api/auth/")) {
    return NextResponse.next();
  }

  if (pathname.startsWith("/api/")) {
    const token = await getToken({
      req: request,
      secret: process.env.NEXTAUTH_SECRET,
    });
    if (!token) {
      return NextResponse.json({ error: "Unauthorized" }, { status: 401 });
    }
  }

  return NextResponse.next();
}

export const config = {
  matcher: ["/api/:path*"],
};
