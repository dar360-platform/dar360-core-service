// prisma/seed.ts
import { PrismaClient, Dar360Role } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

const SEED_AGENT_ID = 'clerk_user_id_placeholder';
const SEED_AGENT_EMAIL = 'agent@dar360.dev';
const SEED_AGENT_PASSWORD = 'password123';

const SEED_OWNER_ID = 'seed_owner_f8f8f8f8f8f8f8f8f8f8f8f';
const SEED_OWNER_EMAIL = 'owner@dar360.dev';
const SEED_OWNER_PASSWORD = 'password123';

async function main() {
  console.log('Starting the seed process...');

  // Check if the seed agent already exists
  const existingAgent = await prisma.user.findUnique({
    where: { id: SEED_AGENT_ID },
  });

  if (existingAgent) {
    console.log('Seed agent already exists. Nothing to do.');
  } else {
    console.log('Creating a new seed agent...');
    const passwordHash = await bcrypt.hash(SEED_AGENT_PASSWORD, 12);
    
    await prisma.user.create({
      data: {
        id: SEED_AGENT_ID,
        email: SEED_AGENT_EMAIL,
        passwordHash,
        fullName: 'Seed Agent',
        phone: '+971500000000',
        role: Dar360Role.AGENT,
        agencyName: 'Seed Agency',
        reraLicenseNumber: '12345',
        reraVerifiedAt: new Date(),
        isActive: true,
      },
    });
    console.log('Seed agent created successfully!');
    console.log(`  - ID: ${SEED_AGENT_ID}`);
    console.log(`  - Email: ${SEED_AGENT_EMAIL}`);
    console.log(`  - Password: ${SEED_AGENT_PASSWORD}`);
  }

  // Check if the seed owner already exists
  const existingOwner = await prisma.user.findUnique({
    where: { id: SEED_OWNER_ID },
  });

  if (existingOwner) {
    console.log('Seed owner already exists. Nothing to do.');
  } else {
    console.log('Creating a new seed owner...');
    const passwordHash = await bcrypt.hash(SEED_OWNER_PASSWORD, 12);
    
    await prisma.user.create({
      data: {
        id: SEED_OWNER_ID,
        email: SEED_OWNER_EMAIL,
        passwordHash,
        fullName: 'Seed Owner',
        phone: '+971501112222',
        role: Dar360Role.OWNER,
        isActive: true,
      },
    });
    console.log('Seed owner created successfully!');
    console.log(`  - ID: ${SEED_OWNER_ID}`);
    console.log(`  - Email: ${SEED_OWNER_EMAIL}`);
    console.log(`  - Password: ${SEED_OWNER_PASSWORD}`);
  }

  console.log('Seed process finished.');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
