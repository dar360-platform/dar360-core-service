import { NextAuthOptions } from 'next-auth';
import CredentialsProvider from 'next-auth/providers/credentials';
import prisma from './db';
import bcrypt from 'bcryptjs';

// Hardcoded dummy account for testing
const DUMMY_ACCOUNT = {
  email: 'hello@dar360.ae',
  password: '1234',
  user: {
    id: 'dummy-account-id',
    email: 'hello@dar360.ae',
    name: 'Dar360 Demo User',
    role: 'ADMIN',
    reraVerified: true,
  },
};

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
        const ipAddress = (req as any)?.headers?.['x-forwarded-for'] || (req as any)?.ip || 'unknown';
        const userAgent = (req as any)?.headers?.['user-agent'] || 'unknown';

        // Check if it's the dummy account
        if (email === DUMMY_ACCOUNT.email && credentials.password === DUMMY_ACCOUNT.password) {
          console.log('✅ Dummy account login successful');
          return DUMMY_ACCOUNT.user;
        }

        // Real user authentication
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
    async jwt({ token, user, trigger }) {
      // Initial sign in
      if (user) {
        token.id = user.id;
        token.role = user.role;
        token.reraVerified = user.reraVerified;
        token.iat = Math.floor(Date.now() / 1000); // Issued at time
      }

      // Refresh token every 15 minutes
      const now = Math.floor(Date.now() / 1000);
      const tokenAge = now - (token.iat as number || now);

      // If token is older than 15 minutes, refresh it
      if (tokenAge > 15 * 60) {
        console.log('🔄 Refreshing JWT token');
        token.iat = now;

        // For real users (not dummy), refresh data from DB
        if (token.id !== DUMMY_ACCOUNT.user.id) {
          try {
            const user = await prisma.user.findUnique({
              where: { id: token.id as string },
              select: {
                id: true,
                email: true,
                fullName: true,
                role: true,
                reraVerifiedAt: true,
                isActive: true,
              },
            });

            if (user && user.isActive) {
              token.role = user.role;
              token.reraVerified = !!user.reraVerifiedAt;
            } else {
              // User no longer exists or is inactive - invalidate token
              return null as any;
            }
          } catch (error) {
            console.error('Error refreshing token:', error);
          }
        }
      }

      return token;
    },
    async session({ session, token }) {
      if (session.user && token) {
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
    maxAge: 24 * 60 * 60, // 24 hours max session
  },
  jwt: {
    maxAge: 15 * 60, // JWT expires after 15 minutes (will be refreshed)
  },
  secret: process.env.NEXTAUTH_SECRET,
  cookies: {
    sessionToken: {
      name: `next-auth.session-token`,
      options: {
        httpOnly: true,
        sameSite: 'lax',
        path: '/',
        secure: process.env.NODE_ENV === 'production',
      },
    },
  },
};
