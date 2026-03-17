import { useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  Title,
  SimpleGrid,
  Card,
  Text,
  Badge,
  Group,
  Stack,
  Skeleton,
  ThemeIcon,
  Paper,
  RingProgress,
  Center,
} from '@mantine/core';
import {
  IconDeviceGamepad,
  IconDeviceGamepad2,
  IconDeviceNintendo,
} from '@tabler/icons-react';
import { getPlatforms } from '../api/platforms';

const PLATFORM_COLORS: Record<string, string> = {
  'game-boy': '#9bbc0f',
  'game-boy-color': '#be4bdb',
  'game-boy-advance': '#5c7cfa',
  nes: '#e64980',
  snes: '#845ef7',
  n64: '#339af0',
  nds: '#22b8cf',
  '3ds': '#20c997',
  gamecube: '#be4bdb',
  wii: '#228be6',
  switch: '#fa5252',
  genesis: '#495057',
  playstation: '#3b5bdb',
  ps2: '#4263eb',
  psp: '#495057',
};

export function Dashboard() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const { data: platforms, isLoading } = useQuery({
    queryKey: ['platforms'],
    queryFn: getPlatforms,
  });

  const totalGames = platforms?.reduce((sum, p) => sum + (p._count?.games ?? 0), 0) ?? 0;
  const platformsWithGames = platforms?.filter((p) => (p._count?.games ?? 0) > 0) ?? [];

  return (
    <Stack>
      {/* Stats Header */}
      <Group gap="lg">
        <Paper p="md" withBorder style={{ flex: 1 }}>
          <Group>
            <ThemeIcon size="xl" radius="md" variant="light" color="violet">
              <IconDeviceGamepad2 size={24} />
            </ThemeIcon>
            <div>
              <Text size="xs" c="dimmed" tt="uppercase" fw={700}>
                Total Games
              </Text>
              <Text size="xl" fw={700}>
                {totalGames}
              </Text>
            </div>
          </Group>
        </Paper>
        <Paper p="md" withBorder style={{ flex: 1 }}>
          <Group>
            <ThemeIcon size="xl" radius="md" variant="light" color="indigo">
              <IconDeviceNintendo size={24} />
            </ThemeIcon>
            <div>
              <Text size="xs" c="dimmed" tt="uppercase" fw={700}>
                Platforms
              </Text>
              <Text size="xl" fw={700}>
                {platformsWithGames.length}
              </Text>
            </div>
          </Group>
        </Paper>
      </Group>

      <Title order={3} mt="sm">
        {t('dashboard.title')}
      </Title>

      {isLoading ? (
        <SimpleGrid cols={{ base: 1, xs: 2, sm: 3, md: 4 }}>
          {Array.from({ length: 8 }).map((_, i) => (
            <Skeleton key={i} height={120} radius="md" />
          ))}
        </SimpleGrid>
      ) : platformsWithGames.length === 0 ? (
        <Paper p="xl" withBorder>
          <Center>
            <Stack align="center" gap="xs">
              <IconDeviceGamepad size={48} opacity={0.3} />
              <Text c="dimmed" ta="center">
                {t('dashboard.noPlatforms')}
              </Text>
            </Stack>
          </Center>
        </Paper>
      ) : (
        <SimpleGrid cols={{ base: 1, xs: 2, sm: 3, md: 4 }}>
          {platformsWithGames.map((platform) => {
            const gameCount = platform._count?.games ?? 0;
            const color = PLATFORM_COLORS[platform.slug] || '#845ef7';

            return (
              <Card
                key={platform.id}
                shadow="sm"
                padding="lg"
                radius="md"
                withBorder
                style={{ cursor: 'pointer', borderLeft: `4px solid ${color}` }}
                onClick={() => navigate(`/platforms/${platform.id}`)}
              >
                <Group justify="space-between" wrap="nowrap">
                  <div style={{ minWidth: 0 }}>
                    <Text fw={600} truncate>
                      {platform.displayName}
                    </Text>
                    <Text size="sm" c="dimmed" mt={4}>
                      {t('platform.games', { count: gameCount })}
                    </Text>
                  </div>
                  <Badge
                    size="xl"
                    radius="md"
                    variant="light"
                    color="violet"
                    style={{ minWidth: 48, textAlign: 'center' }}
                  >
                    {gameCount}
                  </Badge>
                </Group>
              </Card>
            );
          })}
        </SimpleGrid>
      )}
    </Stack>
  );
}
