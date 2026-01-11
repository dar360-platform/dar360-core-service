# Dar360 Auth & User Service Overview

This document captures the functional scope, architecture, and validation steps for the `next-auth-service` project. Share it with other squads (frontend, backend, QA) to align on integrations and expected behaviours.

## 1. Architecture Summary
- **Framework**: Next.js 16 (App Router) with TypeScript.
- **Authentication**: NextAuth credentials provider (JWT strategy).
- **Database**: PostgreSQL via Prisma ORM (`prisma/schema.prisma`).
- **Validation**: Zod schemas in `src/schemas/user.schema.ts`.
- **Business Layer**: `src/services/user.service.ts` encapsulates all user operations.
- **Middleware**: `src/middleware.ts` applies rate limiting and requires NextAuth sessions for `/api/**` (excluding `api/auth/*` routes).

### Data Model
`User` records capture role-based access (`Dar360Role` enum), optional RERA details, and invitation attribution (`invitedById` self-relationship). See `prisma/schema.prisma` for the full schema.

## 2. Environment & Configuration
1. Ensure `.env` contains:
   ```bash
   DATABASE_URL="postgresql://user:password@host:5432/db"
   NEXTAUTH_SECRET="complex-secret-string"
   RATE_LIMIT_MAX=120        # optional override (per minute)
   ```
2. Install dependencies and generate Prisma client:
   ```bash
   corepack pnpm install
   pnpm prisma migrate dev
   ```
3. Start the dev server: `pnpm dev` → http://localhost:3000.

## 3. Authentication Flow
1. **Registration** (`POST /api/auth/register`)
   - Public endpoint.
   - Validates payload via `registerSchema`.
   - Creates active user or activates pending invited owner.
2. **Login** (`POST /api/auth/[...nextauth]`)
   - NextAuth credentials checks email/password (bcrypt).
   - Returns session + JWT with `id`, `role`, and `fullName`.
3. **Logout** (`POST /api/auth/logout`)
   - Clears session cookies client-side.
4. **Session Enforcement**
   - Middleware validates JWT on all `/api/**` requests.
   - Helper `requireUser()` ensures user presence and (optional) role guard.

## 4. API Surface

| Endpoint | Method(s) | Role Requirement | Description |
| --- | --- | --- | --- |
| `/api/auth/register` | POST | Public | Create/activate user accounts |
| `/api/auth/[...nextauth]` | GET, POST | Public | NextAuth handler (login) |
| `/api/auth/logout` | POST | Authenticated | Clear session cookies |
| `/api/users` | GET, POST | `AGENT` | List users / create new user |
| `/api/users/:id` | GET, PUT, DELETE | `AGENT` | CRUD for specific user; delete performs soft-deactivate |
| `/api/users/me` | GET, PUT | Any authenticated user | View/update own profile |
| `/api/users/verify-rera` | POST | Any authenticated user | Save license number & timestamp |
| `/api/users/owners` | GET | `AGENT` | List owners invited by current agent |
| `/api/users/owners/invite` | POST | `AGENT` | Invite new owner (inactive until registration) |

Responses omit `passwordHash`. Errors surface as `{ error: string }` with relevant HTTP status codes.

## 5. Workflows
### Owner Invitation & Activation
1. Agent calls `POST /api/users/owners/invite`.
2. System creates inactive `OWNER` user with invite metadata and random password.
3. Owner completes registration via `POST /api/auth/register` (detects existing invitation, sets real password, activates account).
4. Agent can see invitees via `GET /api/users/owners`.

### RERA Verification
1. User submits license number at `/api/users/verify-rera`.
2. Service stores license value and current timestamp (`reraVerifiedAt`).
3. Extend logic later to integrate with external verification provider if needed.

## 6. Testing & Verification
### Manual Smoke Tests
1. **Registration** – POST new user, expect 201 + JSON response.
2. **Login** – POST credentials to `/api/auth/[...nextauth]`, capture session cookie.
3. **Protected Requests** – Call `/api/users/me` with session cookie; expect 200.
4. **Role Gates** – Attempt `/api/users` with non-agent user; expect 403.
5. **RERA Workflow** – POST to `/api/users/verify-rera`, then re-fetch `/api/users/me` to verify fields.
6. **Owner Invitation** – Agent invites owner, invited email appears in `/api/users/owners`, registering as owner reactivates and marks `invitedById`.

### Automated
- Run `pnpm lint` and `pnpm test` (Vitest) to check service logic, auth flows, and all API handlers.
- `pnpm test:watch` is available during development.
- Optionally extend integration coverage using `supertest` or `node:test` for end-to-end checks.

## 7. Project Structure Snapshot
```
src/
  app/api/...      # API route handlers
  lib/             # auth + prisma helpers
  schemas/         # Zod schemas
  services/        # Prisma-backed business logic
middleware.ts      # NextAuth + rate limit guard
prisma/schema.prisma
types/next-auth.d.ts
AUTH_FEATURES.md   # this document
README.md
```

## 8. Future Enhancements
- Replace in-memory rate limiter with distributed cache (Redis) for multi-instance deployments.
- Add email service integration for owner invites/registration confirmations.
- Expand test suite with formal integration tests and contract tests for shared API spec.
- Document API via OpenAPI or Postman collection for downstream consumers.

## 9. Contact & Ownership
- **Primary owner**: Developer 1 (Auth & Users squad).
- **Integration points**: Frontend (dar360-view) and other backend services expecting user data.
- For issues or escalations, coordinate with Dev 2 (listings) and Dev 3 (dashboard) to ensure session handling aligns across the platform.
