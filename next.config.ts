import type { NextConfig } from "next";

const nextConfig: NextConfig = {
  /* config options here */
  // reactCompiler: true,
  async headers() {
    // Allow both development (localhost:8081) and production frontend origins
    const allowedOrigins = [
      'http://localhost:8081',
      'http://localhost:8080',
      'http://localhost:3001',
      'https://dar360.ae',
      'https://www.dar360.ae',
    ];

    return [
      {
        // matching all API routes
        source: "/api/:path*",
        headers: [
          { key: "Access-Control-Allow-Credentials", value: "true" },
          // Note: When using credentials, origin must be specific, not "*"
          // The actual origin validation happens in middleware or per-request
          { key: "Access-Control-Allow-Origin", value: process.env.NEXT_PUBLIC_FRONTEND_URL || "http://localhost:8081" },
          { key: "Access-Control-Allow-Methods", value: "GET,DELETE,PATCH,POST,PUT,OPTIONS" },
          { key: "Access-Control-Allow-Headers", value: "X-CSRF-Token, X-Requested-With, Accept, Accept-Version, Content-Length, Content-MD5, Content-Type, Date, X-Api-Version, Cookie" },
        ],
      },
    ];
  },
};

export default nextConfig;
