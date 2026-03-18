import { useParams } from 'react-router-dom';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  Title,
  Grid,
  Image,
  Text,
  Stack,
  Group,
  Badge,
  Paper,
  Table,
  Button,
  FileInput,
  ActionIcon,
  Skeleton,
  Modal,
  TextInput,
  Card,
  Loader,
  Center,
  ScrollArea,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import { notifications } from '@mantine/notifications';
import { IconUpload, IconDownload, IconTrash, IconRefresh, IconSearch, IconCheck } from '@tabler/icons-react';
import { getGame, fetchGameMetadata, searchIGDB, applyIGDBResult } from '../api/games';
import type { IGDBSearchResult } from '../api/games';
import { getSaves, uploadSave, deleteSave, downloadSave } from '../api/saves';
import { useAuthStore } from '../stores/auth';
import { useState } from 'react';

function formatFileSize(bytes: string | number): string {
  const b = typeof bytes === 'string' ? parseInt(bytes) : bytes;
  if (b < 1024) return `${b} B`;
  if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
  if (b < 1024 * 1024 * 1024) return `${(b / (1024 * 1024)).toFixed(1)} MB`;
  return `${(b / (1024 * 1024 * 1024)).toFixed(2)} GB`;
}

function MetadataSearchModal({
  opened,
  onClose,
  gameId,
  initialQuery,
}: {
  opened: boolean;
  onClose: () => void;
  gameId: number;
  initialQuery: string;
}) {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [searchQuery, setSearchQuery] = useState(initialQuery);
  const [submittedQuery, setSubmittedQuery] = useState('');

  const { data: results, isLoading, isFetching } = useQuery({
    queryKey: ['igdb-search', gameId, submittedQuery],
    queryFn: () => searchIGDB(gameId, submittedQuery),
    enabled: submittedQuery.length > 0,
  });

  const applyMutation = useMutation({
    mutationFn: (result: IGDBSearchResult) => applyIGDBResult(gameId, result),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['game', String(gameId)] });
      queryClient.invalidateQueries({ queryKey: ['games'] });
      notifications.show({ title: 'IGDB', message: 'Metadata applied', color: 'green' });
      onClose();
    },
    onError: (err) => {
      notifications.show({ title: 'Error', message: err.message, color: 'red' });
    },
  });

  const handleSearch = () => {
    if (searchQuery.trim()) {
      setSubmittedQuery(searchQuery.trim());
    }
  };

  return (
    <Modal opened={opened} onClose={onClose} title="Search IGDB" size="lg">
      <Stack>
        <Group>
          <TextInput
            placeholder="Search for a game..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            leftSection={<IconSearch size={16} />}
            style={{ flex: 1 }}
          />
          <Button onClick={handleSearch} loading={isFetching}>
            {t('common.search')}
          </Button>
        </Group>

        {isLoading && (
          <Center py="xl">
            <Loader />
          </Center>
        )}

        {results && results.length === 0 && (
          <Text c="dimmed" ta="center" py="md">
            No results found. Try a different search term.
          </Text>
        )}

        {results && results.length > 0 && (
          <ScrollArea.Autosize mah={500}>
            <Stack gap="xs">
              {results.map((result) => (
                <Card
                  key={result.igdbId}
                  withBorder
                  padding="sm"
                  radius="md"
                  style={{ cursor: 'pointer' }}
                  onClick={() => applyMutation.mutate(result)}
                >
                  <Group wrap="nowrap" align="flex-start">
                    {result.coverUrl ? (
                      <Image
                        src={result.thumbnailUrl || result.coverUrl}
                        w={60}
                        h={80}
                        fit="cover"
                        radius="sm"
                      />
                    ) : (
                      <div
                        style={{
                          width: 60,
                          height: 80,
                          borderRadius: 4,
                          background: 'var(--mantine-color-dark-6)',
                          flexShrink: 0,
                        }}
                      />
                    )}
                    <Stack gap={4} style={{ minWidth: 0, flex: 1 }}>
                      <Group justify="space-between" wrap="nowrap">
                        <Text fw={600} size="sm" lineClamp={1}>
                          {result.name}
                        </Text>
                        {result.releaseDate && (
                          <Badge size="sm" variant="light" color="gray" style={{ flexShrink: 0 }}>
                            {new Date(result.releaseDate).getFullYear()}
                          </Badge>
                        )}
                      </Group>
                      {result.summary && (
                        <Text size="xs" c="dimmed" lineClamp={2}>
                          {result.summary}
                        </Text>
                      )}
                    </Stack>
                    <ActionIcon
                      variant="light"
                      color="green"
                      size="lg"
                      style={{ flexShrink: 0 }}
                      loading={applyMutation.isPending}
                    >
                      <IconCheck size={18} />
                    </ActionIcon>
                  </Group>
                </Card>
              ))}
            </Stack>
          </ScrollArea.Autosize>
        )}
      </Stack>
    </Modal>
  );
}

export function GameDetail() {
  const { id } = useParams<{ id: string }>();
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const user = useAuthStore((s) => s.user);
  const [saveFile, setSaveFile] = useState<File | null>(null);
  const [searchModalOpened, { open: openSearchModal, close: closeSearchModal }] = useDisclosure(false);

  const { data: game, isLoading } = useQuery({
    queryKey: ['game', id],
    queryFn: () => getGame(parseInt(id!)),
    enabled: !!id,
  });

  const { data: saves } = useQuery({
    queryKey: ['saves', id],
    queryFn: () => getSaves(parseInt(id!)),
    enabled: !!id,
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadSave(parseInt(id!), file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['saves', id] });
      setSaveFile(null);
      notifications.show({ title: 'Success', message: 'Save file uploaded', color: 'green' });
    },
    onError: () => {
      notifications.show({ title: 'Error', message: 'Failed to upload save file', color: 'red' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteSave,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['saves', id] });
      notifications.show({ title: 'Deleted', message: 'Save file deleted', color: 'orange' });
    },
  });

  const metadataMutation = useMutation({
    mutationFn: () => fetchGameMetadata(parseInt(id!)),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['game', id] });
      notifications.show({ title: 'IGDB', message: 'Metadata fetched successfully', color: 'green' });
    },
    onError: () => {
      // Auto-fetch failed — open manual search
      openSearchModal();
    },
  });

  if (isLoading) {
    return (
      <Grid>
        <Grid.Col span={{ base: 12, md: 4 }}>
          <Skeleton height={400} radius="md" />
        </Grid.Col>
        <Grid.Col span={{ base: 12, md: 8 }}>
          <Skeleton height={200} radius="md" />
        </Grid.Col>
      </Grid>
    );
  }

  if (!game) {
    return <Text>Game not found</Text>;
  }

  return (
    <Stack>
      <Grid>
        <Grid.Col span={{ base: 12, md: 3, lg: 2 }}>
          <Image
            src={game.coverUrl || undefined}
            alt={game.displayName}
            radius="md"
            maw={264}
            fallbackSrc="data:image/svg+xml,<svg xmlns='http://www.w3.org/2000/svg' width='264' height='374' fill='%231a1b1e'><rect width='264' height='374' rx='8'/></svg>"
          />
        </Grid.Col>

        <Grid.Col span={{ base: 12, md: 9, lg: 10 }}>
          <Stack>
            <div>
              <Title order={2}>{game.displayName}</Title>
              <Group mt="xs">
                <Badge variant="light" color="violet">
                  {game.platform?.displayName}
                </Badge>
                {game.releaseDate && (
                  <Badge variant="light" color="gray">
                    {new Date(game.releaseDate).toLocaleDateString()}
                  </Badge>
                )}
                <Badge variant="light" color="gray">
                  {formatFileSize(game.fileSize)}
                </Badge>
              </Group>
            </div>

            {game.description && (
              <Paper p="md" withBorder>
                <Text size="sm">{game.description}</Text>
              </Paper>
            )}

            <Text size="sm" c="dimmed">
              {t('game.fileSize')}: {formatFileSize(game.fileSize)} &middot; {game.fileName}
            </Text>

            {user?.role === 'ADMIN' && (
              <Group>
                <Button
                  variant="light"
                  size="sm"
                  leftSection={<IconRefresh size={16} />}
                  onClick={() => metadataMutation.mutate()}
                  loading={metadataMutation.isPending}
                >
                  {t('game.fetchMetadata')}
                </Button>
                <Button
                  variant="subtle"
                  size="sm"
                  leftSection={<IconSearch size={16} />}
                  onClick={openSearchModal}
                >
                  Manual Search
                </Button>
              </Group>
            )}
          </Stack>
        </Grid.Col>
      </Grid>

      {/* Save Files Section */}
      <Paper p="md" withBorder>
        <Title order={4} mb="md">
          {t('game.saves')}
        </Title>

        <Group mb="md">
          <FileInput
            placeholder={t('game.uploadSave')}
            leftSection={<IconUpload size={16} />}
            value={saveFile}
            onChange={setSaveFile}
            style={{ flex: 1 }}
          />
          <Button
            onClick={() => saveFile && uploadMutation.mutate(saveFile)}
            disabled={!saveFile}
            loading={uploadMutation.isPending}
          >
            {t('game.uploadSave')}
          </Button>
        </Group>

        {saves && saves.length > 0 ? (
          <Table>
            <Table.Thead>
              <Table.Tr>
                <Table.Th>File</Table.Th>
                <Table.Th>Size</Table.Th>
                <Table.Th>Device</Table.Th>
                <Table.Th>Date</Table.Th>
                <Table.Th>Actions</Table.Th>
              </Table.Tr>
            </Table.Thead>
            <Table.Tbody>
              {saves.map((save) => (
                <Table.Tr key={save.id}>
                  <Table.Td>
                    <Group gap="xs">
                      <Text size="sm">{save.fileName}</Text>
                      {save.isLatest && (
                        <Badge size="xs" color="green">
                          Latest
                        </Badge>
                      )}
                    </Group>
                  </Table.Td>
                  <Table.Td>
                    <Text size="sm">{formatFileSize(save.fileSize)}</Text>
                  </Table.Td>
                  <Table.Td>
                    <Text size="sm">{save.deviceName || '—'}</Text>
                  </Table.Td>
                  <Table.Td>
                    <Text size="sm">{new Date(save.uploadedAt).toLocaleString()}</Text>
                  </Table.Td>
                  <Table.Td>
                    <Group gap="xs">
                      <ActionIcon
                        variant="light"
                        color="blue"
                        onClick={() => downloadSave(save.id)}
                      >
                        <IconDownload size={16} />
                      </ActionIcon>
                      <ActionIcon
                        variant="light"
                        color="red"
                        onClick={() => deleteMutation.mutate(save.id)}
                        loading={deleteMutation.isPending}
                      >
                        <IconTrash size={16} />
                      </ActionIcon>
                    </Group>
                  </Table.Td>
                </Table.Tr>
              ))}
            </Table.Tbody>
          </Table>
        ) : (
          <Text c="dimmed" ta="center" py="md">
            {t('game.noSaves')}
          </Text>
        )}
      </Paper>

      {/* IGDB Manual Search Modal */}
      <MetadataSearchModal
        opened={searchModalOpened}
        onClose={closeSearchModal}
        gameId={parseInt(id!)}
        initialQuery={game.displayName}
      />
    </Stack>
  );
}
