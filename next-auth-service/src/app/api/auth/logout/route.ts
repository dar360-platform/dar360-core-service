import { NextResponse } from "next/server";

const AUTH_COOKIES = [
  "next-auth.session-token",
  "__Secure-next-auth.session-token",
  "next-auth.callback-url",
  "next-auth.csrf-token",
];

export async function POST() {
  const response = NextResponse.json({ success: true });
  for (const cookie of AUTH_COOKIES) {
    response.cookies.set(cookie, "", { expires: new Date(0) });
  }
  return response;
}
