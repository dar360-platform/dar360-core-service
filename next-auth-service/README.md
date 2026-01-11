## Dar360 Auth Service

Next.js (App Router) service providing authentication, user management APIs, RERA verification, and owner invitation flows for the Dar360 platform. It uses NextAuth credentials, Prisma + PostgreSQL, and Zod-validated REST endpoints.

### Prerequisites
- Node.js 18+
- Corepack (ships with recent Node releases)
- Postgres database (local or managed). Update `.env` with `DATABASE_URL` and `NEXTAUTH_SECRET`.

### Setup
```bash
cd next-auth-service
corepack pnpm install
cp .env.example .env  # if you keep an example file
# ensure DATABASE_URL and NEXTAUTH_SECRET are set
pnpm prisma migrate dev
```

### Development Commands
- `pnpm dev` – start Next.js dev server on `http://localhost:3000`
- `pnpm build` / `pnpm start` – production build and run
- `pnpm lint` – run ESLint against the codebase
- `pnpm test` – execute Vitest unit/integration suite
- `pnpm test:watch` – run tests in watch mode
- `pnpm prisma studio` – inspect data in Prisma Studio (optional)

### API Highlights
- `POST /api/auth/register` – public registration
- `POST /api/auth/[...nextauth]` – NextAuth credentials login
- `POST /api/auth/logout` – clears session cookies
- `GET/POST /api/users` – admin list/create users
- `GET/PUT/DELETE /api/users/:id` – admin user management
- `GET/PUT /api/users/me` – current user profile
- `POST /api/users/verify-rera` – RERA verification for current user
- `GET /api/users/owners` – invited owners for an agent
- `POST /api/users/owners/invite` – invite new owner accounts

All non-auth endpoints require a valid NextAuth session; admin routes additionally require the `AGENT` role. Middleware enforces rate limiting and checks JWT presence.

### Project Structure (highlights)
```
src/
  app/api/          # App Router API endpoints
  lib/              # Auth, Prisma client, common helpers
  schemas/          # Zod validation schemas
  services/         # Prisma-backed user domain logic
middleware.ts       # API auth + rate limiting
prisma/schema.prisma
```

For a deeper functional overview and validation/test details, see `AUTH_FEATURES.md`.
