import { Dar360Role } from "@prisma/client";
import type { DefaultSession } from "next-auth";

declare module "next-auth" {
  interface Session {
    user: {
      id: string;
      role: Dar360Role;
      fullName: string;
    } & DefaultSession["user"];
  }

  interface User {
    id: string;
    role: Dar360Role;
    fullName: string;
  }
}

declare module "next-auth/jwt" {
  interface JWT {
    role?: Dar360Role;
    fullName?: string;
  }
}
