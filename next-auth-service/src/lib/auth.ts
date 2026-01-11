import { Dar360Role } from "@prisma/client";
import bcrypt from "bcryptjs";
import type { NextAuthOptions, Session } from "next-auth";
import { getServerSession } from "next-auth";
import CredentialsProvider from "next-auth/providers/credentials";
import { z } from "zod";

import prisma from "@/lib/db";
import { HttpError } from "@/lib/errors";
import { loginSchema } from "@/schemas/user.schema";

const credentialsValidator = loginSchema.extend({
  email: z.string().email(),
});

export const authOptions: NextAuthOptions = {
  session: {
    strategy: "jwt",
  },
  providers: [
    CredentialsProvider({
      name: "Credentials",
      credentials: {
        email: { label: "Email", type: "email" },
        password: { label: "Password", type: "password" },
      },
      async authorize(rawCredentials) {
        const parsed = credentialsValidator.safeParse(rawCredentials);
        if (!parsed.success) {
          return null;
        }
        const { email, password } = parsed.data;
        const user = await prisma.user.findUnique({
          where: { email: email.toLowerCase() },
        });
        if (!user || !user.isActive) {
          return null;
        }
        const passwordMatch = await bcrypt.compare(password, user.passwordHash);
        if (!passwordMatch) {
          return null;
        }
        return {
          id: user.id,
          email: user.email,
          fullName: user.fullName,
          role: user.role,
        };
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.role = (user as { role: Dar360Role }).role;
        token.fullName = (user as { fullName: string }).fullName;
      }
      return token;
    },
    async session({ session, token }) {
      if (session.user) {
        session.user.id = token.sub as string;
        session.user.role = token.role as Dar360Role;
        session.user.fullName = token.fullName as string;
      }
      return session;
    },
  },
  pages: {
    signIn: "/auth/login",
  },
  secret: process.env.NEXTAUTH_SECRET,
};

export type AuthUser = Session["user"];

export const getAuthSession = () => getServerSession(authOptions);

export async function requireUser(roles?: Dar360Role[]) {
  const session = await getAuthSession();
  if (!session?.user) {
    throw new HttpError(401, "Unauthorized");
  }
  if (roles && !roles.includes(session.user.role)) {
    throw new HttpError(403, "Forbidden");
  }
  return session.user;
}
