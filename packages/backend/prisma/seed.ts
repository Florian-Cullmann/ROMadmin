import { PrismaClient } from '@prisma/client';
import { DEFAULT_PLATFORMS } from '@romadmin/shared';

const prisma = new PrismaClient();

async function main() {
  for (const platform of DEFAULT_PLATFORMS) {
    await prisma.platform.upsert({
      where: { slug: platform.slug },
      update: {},
      create: platform,
    });
  }
  console.log(`Seeded ${DEFAULT_PLATFORMS.length} platforms`);
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(() => prisma.$disconnect());
