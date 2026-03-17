import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import { useQueryClient } from '@tanstack/react-query';
import {
  Container,
  Stepper,
  Button,
  Group,
  TextInput,
  PasswordInput,
  Select,
  Paper,
  Title,
  Text,
  Stack,
  Alert,
  Center,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconCheck, IconAlertCircle, IconDeviceGamepad2 } from '@tabler/icons-react';
import { completeInstall, checkPath } from '../api/install';

export function Install() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const queryClient = useQueryClient();
  const [active, setActive] = useState(0);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pathValid, setPathValid] = useState<boolean | null>(null);

  const form = useForm({
    initialValues: {
      admin: { username: '', email: '', password: '' },
      romPath: '/roms',
      language: 'en' as 'en' | 'de',
      igdb: { clientId: '', clientSecret: '' },
    },
    validate: {
      admin: {
        username: (v) => (v.length < 3 ? 'Min. 3 characters' : null),
        email: (v) => (/^\S+@\S+$/.test(v) ? null : 'Invalid email'),
        password: (v) => (v.length < 8 ? 'Min. 8 characters' : null),
      },
      romPath: (v) => (v.length < 1 ? 'Required' : null),
      igdb: {
        clientId: (v) => (v.length < 1 ? 'Required' : null),
        clientSecret: (v) => (v.length < 1 ? 'Required' : null),
      },
    },
  });

  const nextStep = () => {
    // Validate current step
    if (active === 0) {
      const errors = {
        'admin.username': form.validateField('admin.username').hasError,
        'admin.email': form.validateField('admin.email').hasError,
        'admin.password': form.validateField('admin.password').hasError,
      };
      if (Object.values(errors).some(Boolean)) return;
    }
    if (active === 1) {
      if (form.validateField('romPath').hasError) return;
    }
    if (active === 3) {
      const errors = {
        'igdb.clientId': form.validateField('igdb.clientId').hasError,
        'igdb.clientSecret': form.validateField('igdb.clientSecret').hasError,
      };
      if (Object.values(errors).some(Boolean)) return;
    }
    setActive((c) => Math.min(c + 1, 4));
  };

  const prevStep = () => setActive((c) => Math.max(c - 1, 0));

  const handleVerifyPath = async () => {
    try {
      await checkPath(form.values.romPath);
      setPathValid(true);
    } catch {
      setPathValid(false);
    }
  };

  const handleSubmit = async () => {
    setLoading(true);
    setError(null);
    try {
      await completeInstall(form.values);
      await queryClient.invalidateQueries({ queryKey: ['install-status'] });
      navigate('/login');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Installation failed');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container size="sm" py={60}>
      <Center mb="xl">
        <Stack align="center" gap="xs">
          <IconDeviceGamepad2 size={48} />
          <Title order={2}>{t('install.title')}</Title>
          <Text c="dimmed">{t('install.subtitle')}</Text>
        </Stack>
      </Center>

      <Paper shadow="md" p="xl" radius="md" withBorder>
        <Stepper active={active} onStepClick={setActive} size="sm" mb="xl">
          <Stepper.Step label={t('install.stepAdmin')}>
            <Stack mt="md">
              <TextInput
                label={t('install.adminUsername')}
                placeholder="admin"
                {...form.getInputProps('admin.username')}
              />
              <TextInput
                label={t('install.adminEmail')}
                placeholder="admin@example.com"
                {...form.getInputProps('admin.email')}
              />
              <PasswordInput
                label={t('install.adminPassword')}
                placeholder="Min. 8 characters"
                {...form.getInputProps('admin.password')}
              />
            </Stack>
          </Stepper.Step>

          <Stepper.Step label={t('install.stepLibrary')}>
            <Stack mt="md">
              <TextInput
                label={t('install.romPath')}
                description={t('install.romPathHint')}
                placeholder="/roms"
                {...form.getInputProps('romPath')}
              />
              <Group>
                <Button variant="light" onClick={handleVerifyPath}>
                  {t('install.verifyPath')}
                </Button>
                {pathValid === true && (
                  <Text c="green" size="sm">
                    <IconCheck size={14} style={{ verticalAlign: 'middle' }} /> {t('install.pathValid')}
                  </Text>
                )}
                {pathValid === false && (
                  <Text c="red" size="sm">
                    <IconAlertCircle size={14} style={{ verticalAlign: 'middle' }} /> Path not found
                  </Text>
                )}
              </Group>
            </Stack>
          </Stepper.Step>

          <Stepper.Step label={t('install.stepLanguage')}>
            <Stack mt="md">
              <Select
                label={t('install.language')}
                data={[
                  { value: 'en', label: 'English' },
                  { value: 'de', label: 'Deutsch' },
                ]}
                {...form.getInputProps('language')}
              />
            </Stack>
          </Stepper.Step>

          <Stepper.Step label={t('install.stepIgdb')}>
            <Stack mt="md">
              <TextInput
                label={t('install.igdbClientId')}
                placeholder="Your Twitch/IGDB Client ID"
                {...form.getInputProps('igdb.clientId')}
              />
              <PasswordInput
                label={t('install.igdbClientSecret')}
                placeholder="Your Twitch/IGDB Client Secret"
                {...form.getInputProps('igdb.clientSecret')}
              />
              <Text size="sm" c="dimmed">
                {t('install.igdbHint')}
              </Text>
            </Stack>
          </Stepper.Step>

          <Stepper.Completed>
            <Stack mt="md" align="center">
              <IconCheck size={48} color="var(--mantine-color-green-6)" />
              <Text ta="center">Ready to install. Click the button below to complete setup.</Text>
            </Stack>
          </Stepper.Completed>
        </Stepper>

        {error && (
          <Alert color="red" icon={<IconAlertCircle />} mb="md">
            {error}
          </Alert>
        )}

        <Group justify="space-between" mt="xl">
          <Button variant="default" onClick={prevStep} disabled={active === 0}>
            {t('common.back')}
          </Button>
          {active < 4 ? (
            <Button onClick={nextStep}>{t('common.next')}</Button>
          ) : (
            <Button onClick={handleSubmit} loading={loading} color="green">
              {t('install.complete')}
            </Button>
          )}
        </Group>
      </Paper>
    </Container>
  );
}
