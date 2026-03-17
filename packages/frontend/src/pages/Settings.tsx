import { useState } from 'react';
import { useTranslation } from 'react-i18next';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
  Title,
  Tabs,
  Stack,
  Button,
  Paper,
  Text,
  Table,
  Badge,
  Group,
  Progress,
  TextInput,
  PasswordInput,
  Select,
  Modal,
  ActionIcon,
  CopyButton,
  Tooltip,
  Code,
  Alert,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { useDisclosure } from '@mantine/hooks';
import { notifications } from '@mantine/notifications';
import {
  IconScan,
  IconUsers,
  IconSettings,
  IconBrandTwitch,
  IconTrash,
  IconKey,
  IconCopy,
  IconCheck,
  IconPlus,
  IconDeviceFloppy,
  IconDeviceMobile,
  IconUpload,
  IconDownload,
} from '@tabler/icons-react';
import { startScan, getScanStatus } from '../api/scanner';
import { getPlatforms } from '../api/platforms';
import { getUsers, createUser, deleteUser, generateApiKey } from '../api/users';
import { getSettings, updateSettings } from '../api/settings';
import { getApkInfo, uploadApk, deleteApk, getApkDownloadUrl } from '../api/apk';
import { useAuthStore } from '../stores/auth';

export function Settings() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const user = useAuthStore((s) => s.user);

  if (user?.role !== 'ADMIN') {
    return <Text>Access denied</Text>;
  }

  return (
    <Stack>
      <Title order={2}>{t('settings.title')}</Title>
      <Tabs defaultValue="scanner">
        <Tabs.List>
          <Tabs.Tab value="scanner" leftSection={<IconScan size={16} />}>
            {t('settings.scanner')}
          </Tabs.Tab>
          <Tabs.Tab value="platforms" leftSection={<IconSettings size={16} />}>
            {t('settings.platforms')}
          </Tabs.Tab>
          <Tabs.Tab value="users" leftSection={<IconUsers size={16} />}>
            {t('settings.users')}
          </Tabs.Tab>
          <Tabs.Tab value="igdb" leftSection={<IconBrandTwitch size={16} />}>
            {t('settings.igdb')}
          </Tabs.Tab>
          <Tabs.Tab value="general" leftSection={<IconDeviceFloppy size={16} />}>
            {t('settings.general')}
          </Tabs.Tab>
          <Tabs.Tab value="android" leftSection={<IconDeviceMobile size={16} />}>
            Android App
          </Tabs.Tab>
        </Tabs.List>

        <Tabs.Panel value="scanner" pt="md"><ScannerTab /></Tabs.Panel>
        <Tabs.Panel value="platforms" pt="md"><PlatformsTab /></Tabs.Panel>
        <Tabs.Panel value="users" pt="md"><UsersTab /></Tabs.Panel>
        <Tabs.Panel value="igdb" pt="md"><IGDBTab /></Tabs.Panel>
        <Tabs.Panel value="general" pt="md"><GeneralTab /></Tabs.Panel>
        <Tabs.Panel value="android" pt="md"><AndroidAppTab /></Tabs.Panel>
      </Tabs>
    </Stack>
  );
}

function ScannerTab() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { data: scanStatus, refetch: refetchScanStatus } = useQuery({
    queryKey: ['scan-status'],
    queryFn: getScanStatus,
    refetchInterval: (query) => {
      return query.state.data?.state === 'running' ? 1000 : false;
    },
  });

  const scanMutation = useMutation({
    mutationFn: startScan,
    onSuccess: () => {
      refetchScanStatus();
      notifications.show({ title: 'Scanner', message: 'Scan started — metadata will be fetched automatically', color: 'blue' });
    },
    onError: (err) => {
      notifications.show({ title: 'Error', message: err.message, color: 'red' });
    },
    onSettled: () => {
      queryClient.invalidateQueries({ queryKey: ['platforms'] });
      queryClient.invalidateQueries({ queryKey: ['games'] });
    },
  });

  const scanProgress =
    scanStatus?.totalFiles && scanStatus.totalFiles > 0
      ? (scanStatus.processedFiles / scanStatus.totalFiles) * 100
      : 0;

  return (
    <Paper p="md" withBorder>
      <Stack>
        <Group>
          <Button
            onClick={() => scanMutation.mutate()}
            loading={scanStatus?.state === 'running'}
            leftSection={<IconScan size={16} />}
          >
            {scanStatus?.state === 'running' ? t('settings.scanning') : t('settings.rescan')}
          </Button>
        </Group>

        {scanStatus?.state === 'running' && (
          <Stack gap="xs">
            <Progress value={scanProgress} animated />
            <Text size="sm" c="dimmed">
              {scanStatus.processedFiles} / {scanStatus.totalFiles} files processed
            </Text>
          </Stack>
        )}

        {scanStatus?.state === 'completed' && (
          <Alert color="green" variant="light">
            {t('settings.scanComplete')}: {scanStatus.newGames} new, {scanStatus.updatedGames} updated
            {scanStatus.completedAt && (
              <Text size="xs" c="dimmed" mt={4}>
                Completed at {new Date(scanStatus.completedAt).toLocaleString()}
              </Text>
            )}
          </Alert>
        )}

        {scanStatus?.state === 'error' && (
          <Alert color="red" variant="light">
            Error: {scanStatus.error}
          </Alert>
        )}
      </Stack>
    </Paper>
  );
}

function PlatformsTab() {
  const { data: platforms } = useQuery({
    queryKey: ['platforms'],
    queryFn: getPlatforms,
  });

  return (
    <Paper p="md" withBorder>
      <Table striped highlightOnHover>
        <Table.Thead>
          <Table.Tr>
            <Table.Th>Platform</Table.Th>
            <Table.Th>Folder</Table.Th>
            <Table.Th>Extensions</Table.Th>
            <Table.Th>IGDB ID</Table.Th>
            <Table.Th>Games</Table.Th>
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {platforms?.map((p) => (
            <Table.Tr key={p.id}>
              <Table.Td fw={500}>{p.displayName}</Table.Td>
              <Table.Td><Badge variant="light" size="sm">{p.folderName}</Badge></Table.Td>
              <Table.Td><Text size="sm" c="dimmed">{p.fileExtensions}</Text></Table.Td>
              <Table.Td><Text size="sm" c="dimmed">{p.igdbPlatformId ?? '—'}</Text></Table.Td>
              <Table.Td><Badge color="violet" variant="light">{p._count?.games ?? 0}</Badge></Table.Td>
            </Table.Tr>
          ))}
        </Table.Tbody>
      </Table>
    </Paper>
  );
}

function UsersTab() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();
  const [createModalOpened, { open: openCreateModal, close: closeCreateModal }] = useDisclosure(false);
  const [generatedKey, setGeneratedKey] = useState<string | null>(null);

  const { data: users } = useQuery({
    queryKey: ['users'],
    queryFn: getUsers,
  });

  const createMutation = useMutation({
    mutationFn: createUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      closeCreateModal();
      notifications.show({ title: 'Success', message: 'User created', color: 'green' });
    },
    onError: (err) => {
      notifications.show({ title: 'Error', message: err.message, color: 'red' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteUser,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users'] });
      notifications.show({ title: 'Deleted', message: 'User deleted', color: 'orange' });
    },
  });

  const apiKeyMutation = useMutation({
    mutationFn: generateApiKey,
    onSuccess: (data) => {
      setGeneratedKey(data.apiKey);
    },
  });

  const form = useForm({
    initialValues: { username: '', email: '', password: '', role: 'USER', language: 'en' },
    validate: {
      username: (v) => (v.length < 3 ? 'Min. 3 characters' : null),
      email: (v) => (/^\S+@\S+$/.test(v) ? null : 'Invalid email'),
      password: (v) => (v.length < 8 ? 'Min. 8 characters' : null),
    },
  });

  return (
    <>
      <Paper p="md" withBorder>
        <Group justify="space-between" mb="md">
          <Text fw={500}>Users</Text>
          <Button leftSection={<IconPlus size={16} />} size="sm" onClick={openCreateModal}>
            {t('settings.createUser')}
          </Button>
        </Group>

        <Table striped highlightOnHover>
          <Table.Thead>
            <Table.Tr>
              <Table.Th>Username</Table.Th>
              <Table.Th>Email</Table.Th>
              <Table.Th>Role</Table.Th>
              <Table.Th>Language</Table.Th>
              <Table.Th>Actions</Table.Th>
            </Table.Tr>
          </Table.Thead>
          <Table.Tbody>
            {users?.map((u) => (
              <Table.Tr key={u.id}>
                <Table.Td fw={500}>{u.username}</Table.Td>
                <Table.Td>{u.email}</Table.Td>
                <Table.Td>
                  <Badge color={u.role === 'ADMIN' ? 'red' : 'blue'} variant="light" size="sm">
                    {u.role}
                  </Badge>
                </Table.Td>
                <Table.Td>{u.language.toUpperCase()}</Table.Td>
                <Table.Td>
                  <Group gap="xs">
                    <Tooltip label={t('settings.generateApiKey')}>
                      <ActionIcon
                        variant="light"
                        color="violet"
                        onClick={() => apiKeyMutation.mutate(u.id)}
                        loading={apiKeyMutation.isPending}
                      >
                        <IconKey size={16} />
                      </ActionIcon>
                    </Tooltip>
                    <Tooltip label={t('common.delete')}>
                      <ActionIcon
                        variant="light"
                        color="red"
                        onClick={() => deleteMutation.mutate(u.id)}
                      >
                        <IconTrash size={16} />
                      </ActionIcon>
                    </Tooltip>
                  </Group>
                </Table.Td>
              </Table.Tr>
            ))}
          </Table.Tbody>
        </Table>
      </Paper>

      {/* Generated API Key Modal */}
      <Modal opened={!!generatedKey} onClose={() => setGeneratedKey(null)} title="API Key Generated">
        <Stack>
          <Alert color="yellow" variant="light">
            {t('settings.apiKeyGenerated')}
          </Alert>
          <Group>
            <Code block style={{ flex: 1, wordBreak: 'break-all' }}>{generatedKey}</Code>
            <CopyButton value={generatedKey || ''}>
              {({ copied, copy }) => (
                <Tooltip label={copied ? 'Copied' : 'Copy'}>
                  <ActionIcon color={copied ? 'teal' : 'gray'} variant="light" onClick={copy}>
                    {copied ? <IconCheck size={16} /> : <IconCopy size={16} />}
                  </ActionIcon>
                </Tooltip>
              )}
            </CopyButton>
          </Group>
        </Stack>
      </Modal>

      {/* Create User Modal */}
      <Modal opened={createModalOpened} onClose={closeCreateModal} title={t('settings.createUser')}>
        <form onSubmit={form.onSubmit((values) => createMutation.mutate(values))}>
          <Stack>
            <TextInput label="Username" {...form.getInputProps('username')} />
            <TextInput label="Email" {...form.getInputProps('email')} />
            <PasswordInput label="Password" {...form.getInputProps('password')} />
            <Select
              label="Role"
              data={[
                { value: 'USER', label: 'User' },
                { value: 'ADMIN', label: 'Admin' },
              ]}
              {...form.getInputProps('role')}
            />
            <Select
              label="Language"
              data={[
                { value: 'en', label: 'English' },
                { value: 'de', label: 'Deutsch' },
              ]}
              {...form.getInputProps('language')}
            />
            <Button type="submit" loading={createMutation.isPending}>
              {t('settings.createUser')}
            </Button>
          </Stack>
        </form>
      </Modal>
    </>
  );
}

function IGDBTab() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { data: settings } = useQuery({
    queryKey: ['settings'],
    queryFn: getSettings,
  });

  const form = useForm({
    initialValues: {
      igdb_client_id: '',
      igdb_client_secret: '',
    },
  });

  // Populate form when settings load
  if (settings && !form.isTouched()) {
    const clientId = settings['igdb_client_id'] || '';
    const clientSecret = settings['igdb_client_secret'] || '';
    if (clientId !== form.values.igdb_client_id || clientSecret !== form.values.igdb_client_secret) {
      form.setValues({ igdb_client_id: clientId, igdb_client_secret: clientSecret });
    }
  }

  const updateMutation = useMutation({
    mutationFn: updateSettings,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['settings'] });
      notifications.show({ title: 'Saved', message: 'IGDB settings updated', color: 'green' });
    },
  });

  const metadataMutation = useMutation({
    mutationFn: () => fetch('/api/scanner/fetch-metadata', {
      method: 'POST',
      headers: { 'Authorization': `Bearer ${useAuthStore.getState().accessToken}` },
    }),
    onSuccess: () => {
      notifications.show({ title: 'IGDB', message: 'Metadata fetch started in background', color: 'blue' });
    },
  });

  return (
    <Paper p="md" withBorder>
      <form onSubmit={form.onSubmit((values) => {
        const data: Record<string, string> = {};
        if (values.igdb_client_id) data['igdb_client_id'] = values.igdb_client_id;
        if (values.igdb_client_secret && values.igdb_client_secret !== '••••••••') {
          data['igdb_client_secret'] = values.igdb_client_secret;
        }
        updateMutation.mutate(data);
      })}>
        <Stack>
          <TextInput
            label={t('install.igdbClientId')}
            placeholder="Client ID"
            {...form.getInputProps('igdb_client_id')}
          />
          <PasswordInput
            label={t('install.igdbClientSecret')}
            placeholder="Client Secret"
            {...form.getInputProps('igdb_client_secret')}
          />
          <Text size="sm" c="dimmed">{t('install.igdbHint')}</Text>
          <Group>
            <Button type="submit" loading={updateMutation.isPending} leftSection={<IconDeviceFloppy size={16} />}>
              {t('common.save')}
            </Button>
            <Button
              variant="light"
              onClick={() => metadataMutation.mutate()}
              loading={metadataMutation.isPending}
              leftSection={<IconBrandTwitch size={16} />}
            >
              Fetch All Metadata
            </Button>
          </Group>
        </Stack>
      </form>
    </Paper>
  );
}

function AndroidAppTab() {
  const queryClient = useQueryClient();
  const [apkFile, setApkFile] = useState<File | null>(null);

  const { data: apkInfo, isLoading } = useQuery({
    queryKey: ['apk-info'],
    queryFn: getApkInfo,
  });

  const uploadMutation = useMutation({
    mutationFn: (file: File) => uploadApk(file),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['apk-info'] });
      setApkFile(null);
      notifications.show({ title: 'Success', message: 'APK uploaded', color: 'green' });
    },
    onError: (err) => {
      notifications.show({ title: 'Error', message: err.message, color: 'red' });
    },
  });

  const deleteMutation = useMutation({
    mutationFn: deleteApk,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['apk-info'] });
      notifications.show({ title: 'Deleted', message: 'APK removed', color: 'orange' });
    },
  });

  const formatSize = (bytes: string) => {
    const b = parseInt(bytes);
    if (b < 1024 * 1024) return `${(b / 1024).toFixed(1)} KB`;
    return `${(b / (1024 * 1024)).toFixed(1)} MB`;
  };

  return (
    <Paper p="md" withBorder>
      <Stack>
        <Text fw={500} size="lg">Android App</Text>
        <Text size="sm" c="dimmed">
          Upload the RomAdmin APK here so you can download it directly on your handheld devices.
          Open this page on your device's browser and tap the download link.
        </Text>

        {apkInfo?.exists ? (
          <Alert color="green" variant="light" title="APK Available">
            <Stack gap="xs">
              <Text size="sm">
                Size: {formatSize(apkInfo.size!)} &middot; Updated: {new Date(apkInfo.updatedAt!).toLocaleString()}
              </Text>
              <Group>
                <Button
                  component="a"
                  href={getApkDownloadUrl()}
                  download
                  leftSection={<IconDownload size={16} />}
                  variant="light"
                >
                  Download APK
                </Button>
                <Button
                  variant="light"
                  color="red"
                  leftSection={<IconTrash size={16} />}
                  onClick={() => deleteMutation.mutate()}
                  loading={deleteMutation.isPending}
                >
                  Delete
                </Button>
              </Group>
            </Stack>
          </Alert>
        ) : (
          <Alert color="gray" variant="light" title="No APK uploaded">
            <Text size="sm">Upload an APK file to make it available for download on your devices.</Text>
          </Alert>
        )}

        <Group align="flex-end">
          <TextInput
            label="Upload new APK"
            placeholder="Choose .apk file"
            value={apkFile?.name ?? ''}
            readOnly
            onClick={() => {
              const input = document.createElement('input');
              input.type = 'file';
              input.accept = '.apk';
              input.onchange = (e) => {
                const file = (e.target as HTMLInputElement).files?.[0];
                if (file) setApkFile(file);
              };
              input.click();
            }}
            style={{ flex: 1, cursor: 'pointer' }}
          />
          <Button
            onClick={() => apkFile && uploadMutation.mutate(apkFile)}
            disabled={!apkFile}
            loading={uploadMutation.isPending}
            leftSection={<IconUpload size={16} />}
          >
            Upload
          </Button>
        </Group>

        {apkInfo?.exists && (
          <Alert color="blue" variant="light" title="Download on your device">
            <Text size="sm">
              On your handheld, open a browser and go to this URL:
            </Text>
            <Code block mt="xs">
              {`${window.location.origin}/api/downloads/apk`}
            </Code>
          </Alert>
        )}
      </Stack>
    </Paper>
  );
}

function GeneralTab() {
  const { t } = useTranslation();
  const queryClient = useQueryClient();

  const { data: settings } = useQuery({
    queryKey: ['settings'],
    queryFn: getSettings,
  });

  const form = useForm({
    initialValues: {
      rom_root_path: '',
      language: 'en',
    },
  });

  if (settings && !form.isTouched()) {
    const romPath = settings['rom_root_path'] || '';
    const language = settings['language'] || 'en';
    if (romPath !== form.values.rom_root_path || language !== form.values.language) {
      form.setValues({ rom_root_path: romPath, language });
    }
  }

  const updateMutation = useMutation({
    mutationFn: updateSettings,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['settings'] });
      notifications.show({ title: 'Saved', message: 'Settings updated', color: 'green' });
    },
  });

  return (
    <Paper p="md" withBorder>
      <form onSubmit={form.onSubmit((values) => updateMutation.mutate(values))}>
        <Stack>
          <TextInput
            label={t('install.romPath')}
            description={t('install.romPathHint')}
            {...form.getInputProps('rom_root_path')}
          />
          <Select
            label={t('install.language')}
            data={[
              { value: 'en', label: 'English' },
              { value: 'de', label: 'Deutsch' },
            ]}
            {...form.getInputProps('language')}
          />
          <Button type="submit" loading={updateMutation.isPending} leftSection={<IconDeviceFloppy size={16} />}>
            {t('common.save')}
          </Button>
        </Stack>
      </form>
    </Paper>
  );
}
