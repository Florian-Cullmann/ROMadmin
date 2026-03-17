import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  Title,
  SimpleGrid,
  Card,
  Image,
  Text,
  Group,
  Stack,
  TextInput,
  Select,
  Skeleton,
  Pagination,
  Center,
} from '@mantine/core';
import { useDebouncedValue } from '@mantine/hooks';
import { IconSearch } from '@tabler/icons-react';
import { getPlatform } from '../api/platforms';
import { getGames } from '../api/games';

const PLACEHOLDER_COVER = 'data:image/svg+xml,' + encodeURIComponent(
  '<svg xmlns="http://www.w3.org/2000/svg" width="264" height="352" fill="%231a1b1e"><rect width="264" height="352" rx="8"/><text x="132" y="184" text-anchor="middle" fill="%23909296" font-size="14" font-family="sans-serif">No Cover</text></svg>'
);

export function Platform() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [search, setSearch] = useState('');
  const [debouncedSearch] = useDebouncedValue(search, 300);
  const [sort, setSort] = useState('displayName');
  const [page, setPage] = useState(1);

  const { data: platform } = useQuery({
    queryKey: ['platform', id],
    queryFn: () => getPlatform(parseInt(id!)),
    enabled: !!id,
  });

  const { data: gamesData, isLoading } = useQuery({
    queryKey: ['games', id, debouncedSearch, sort, page],
    queryFn: () =>
      getGames({
        platformId: parseInt(id!),
        search: debouncedSearch || undefined,
        sort,
        page,
        limit: 24,
      }),
    enabled: !!id,
  });

  return (
    <Stack>
      <Title order={2}>{platform?.displayName ?? t('common.loading')}</Title>

      <Group>
        <TextInput
          placeholder={t('common.search')}
          leftSection={<IconSearch size={16} />}
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(1); }}
          style={{ flex: 1 }}
        />
        <Select
          value={sort}
          onChange={(v) => v && setSort(v)}
          data={[
            { value: 'displayName', label: 'Name' },
            { value: 'releaseDate', label: t('game.releaseDate') },
            { value: 'fileSize', label: t('game.fileSize') },
          ]}
          w={160}
        />
      </Group>

      {isLoading ? (
        <SimpleGrid cols={{ base: 2, xs: 3, sm: 4, md: 6 }}>
          {Array.from({ length: 12 }).map((_, i) => (
            <Skeleton key={i} style={{ aspectRatio: '3 / 4' }} radius="md" />
          ))}
        </SimpleGrid>
      ) : gamesData?.data.length === 0 ? (
        <Text c="dimmed" ta="center" py="xl">
          {t('platform.noGames')}
        </Text>
      ) : (
        <>
          <SimpleGrid cols={{ base: 2, xs: 3, sm: 4, md: 6 }}>
            {gamesData?.data.map((game) => (
              <Card
                key={game.id}
                shadow="sm"
                padding={0}
                radius="md"
                withBorder
                style={{ cursor: 'pointer' }}
                onClick={() => navigate(`/games/${game.id}`)}
              >
                <Card.Section style={{ aspectRatio: '3 / 4', overflow: 'hidden', background: 'var(--mantine-color-dark-8)' }}>
                  <Image
                    src={game.coverUrl || PLACEHOLDER_COVER}
                    alt={game.displayName}
                    w="100%"
                    h="100%"
                    fit="contain"
                  />
                </Card.Section>
                <Stack gap={2} p="xs">
                  <Text size="sm" fw={500} lineClamp={2}>
                    {game.displayName}
                  </Text>
                  {game.releaseDate && (
                    <Text size="xs" c="dimmed">
                      {new Date(game.releaseDate).getFullYear()}
                    </Text>
                  )}
                </Stack>
              </Card>
            ))}
          </SimpleGrid>

          {gamesData && gamesData.totalPages > 1 && (
            <Center>
              <Pagination value={page} onChange={setPage} total={gamesData.totalPages} />
            </Center>
          )}
        </>
      )}
    </Stack>
  );
}
