import { NextAuthOptions } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';
import prisma from './db';
import bcrypt from 'bcryptjs';

export const authOptions: NextAuthOptions = {
  providers: [
    CredentialsProvider({
      name: 'credentials',
      credentials: {
        email: { label: 'Email', type: 'email' },
        password: { label: 'Password', type: 'password' },
      },
      async authorize(credentials, req) {
        if (!credentials?.email || !credentials?.password) {
          throw new Error('Invalid credentials');
        }

        const email = credentials.email.toLowerCase();
        // Try to get IP and User Agent from request if available (requires passing req to authorize)
        // Note: NextAuth `req` in authorize might be limited depending on setup.
        const ipAddress = (req as any)?.headers?.['x-forwarded-for'] || (req as any)?.ip || 'unknown';
        const userAgent = (req as any)?.headers?.['user-agent'] || 'unknown';

        const user = await prisma.user.findUnique({
          where: { email },
        });

        if (!user || !user.isActive) {
          await prisma.loginHistory.create({
            data: {
              email,
              ipAddress,
              userAgent,
              status: 'FAILURE',
              reason: user ? 'Account inactive' : 'User not found',
            },
          });
          throw new Error('Invalid credentials');
        }

        const isValid = await bcrypt.compare(credentials.password, user.passwordHash);
        if (!isValid) {
          await prisma.loginHistory.create({
            data: {
              userId: user.id,
              email,
              ipAddress,
              userAgent,
              status: 'FAILURE',
              reason: 'Invalid password',
            },
          });
          throw new Error('Invalid credentials');
        }

        // Log successful login
        await prisma.loginHistory.create({
          data: {
            userId: user.id,
            email,
            ipAddress,
            userAgent,
            status: 'SUCCESS',
          },
        });

        return {
          id: user.id,
          email: user.email,
          name: user.fullName,
          role: user.role,
          reraVerified: !!user.reraVerifiedAt,
        };
      },
    }),
  ],
  callbacks: {
    async jwt({ token, user }) {
      if (user) {
        token.id = user.id;
        token.role = user.role;
        token.reraVerified = user.reraVerified;
      }
      return token;
    },
    async session({ session, token }) {
      if (session.user) {
        session.user.id = token.id as string;
        session.user.role = token.role as string;
        session.user.reraVerified = token.reraVerified as boolean;
      }
      return session;
    },
  },
  pages: {
    signIn: '/login',
  },
  session: {
    strategy: 'jwt',
    maxAge: 7 * 24 * 60 * 60, // 7 days
  },
};
