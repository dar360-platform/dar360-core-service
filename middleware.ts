import { NextResponse } from 'next/server';
import type { NextRequest } from 'next/server';

export function middleware(request: NextRequest) {
  // Get the origin from the request
  const origin = request.headers.get('origin');

  console.log('[MIDDLEWARE] Request:', request.method, request.url);
  console.log('[MIDDLEWARE] Origin:', origin);

  // List of allowed origins
  const allowedOrigins = [
    'http://localhost:8080',  // Frontend dev server
    'http://localhost:8081',  // Frontend dev server (alternate port)
    'http://localhost:3000',  // Backend itself
    'https://dar360.ae',      // Production frontend
    'https://www.dar360.ae',  // Production frontend (www)
    process.env.NEXT_PUBLIC_FRONTEND_URL,
  ].filter(Boolean);

  console.log('[MIDDLEWARE] Allowed origins:', allowedOrigins);

  // Handle preflight requests
  if (request.method === 'OPTIONS') {
    console.log('[MIDDLEWARE] Handling OPTIONS preflight request');
    return new NextResponse(null, {
      status: 200,
      headers: {
        'Access-Control-Allow-Origin': origin || '*',
        'Access-Control-Allow-Credentials': 'true',
        'Access-Control-Allow-Methods': 'GET, POST, PUT, PATCH, DELETE, OPTIONS',
        'Access-Control-Allow-Headers': 'Content-Type, Authorization, X-Requested-With',
        'Access-Control-Max-Age': '86400',
      },
    });
  }

  // Clone the response
  const response = NextResponse.next();

  // Check if origin is allowed
  if (origin && allowedOrigins.includes(origin)) {
    console.log('[MIDDLEWARE] Origin allowed, setting CORS headers');
    response.headers.set('Access-Control-Allow-Origin', origin);
    response.headers.set('Access-Control-Allow-Credentials', 'true');
    response.headers.set(
      'Access-Control-Allow-Methods',
      'GET, POST, PUT, PATCH, DELETE, OPTIONS'
    );
    response.headers.set(
      'Access-Control-Allow-Headers',
      'Content-Type, Authorization, X-Requested-With'
    );
    response.headers.set('Access-Control-Max-Age', '86400');
  } else {
    console.log('[MIDDLEWARE] Origin NOT allowed or missing');
  }

  return response;
}

// Apply middleware to API routes
export const config = {
  matcher: '/api/:path*',
};
