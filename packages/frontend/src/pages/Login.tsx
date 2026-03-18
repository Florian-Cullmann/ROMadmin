import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from 'react-i18next';
import {
  Container,
  Paper,
  Title,
  TextInput,
  PasswordInput,
  Button,
  Stack,
  Alert,
  Center,
} from '@mantine/core';
import { useForm } from '@mantine/form';
import { IconAlertCircle, IconDeviceGamepad2 } from '@tabler/icons-react';
import { login } from '../api/auth';
import { useAuthStore } from '../stores/auth';
import i18n from '../i18n';

export function Login() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const setAuth = useAuthStore((s) => s.setAuth);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const form = useForm({
    initialValues: { username: '', password: '' },
    validate: {
      username: (v) => (v.length < 1 ? 'Required' : null),
      password: (v) => (v.length < 1 ? 'Required' : null),
    },
  });

  const handleSubmit = async (values: typeof form.values) => {
    setLoading(true);
    setError(null);
    try {
      const res = await login(values.username, values.password);
      setAuth(res.token, res.user);
      i18n.changeLanguage(res.user.language);
      navigate('/');
    } catch {
      setError(t('auth.invalidCredentials'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container size={420} py={80}>
      <Center mb="xl">
        <Stack align="center" gap="xs">
          <IconDeviceGamepad2 size={48} />
          <Title order={2}>{t('auth.loginTitle')}</Title>
        </Stack>
      </Center>

      <Paper shadow="md" p="xl" radius="md" withBorder>
        <form onSubmit={form.onSubmit(handleSubmit)}>
          <Stack>
            {error && (
              <Alert color="red" icon={<IconAlertCircle />}>
                {error}
              </Alert>
            )}
            <TextInput
              label={t('auth.username')}
              placeholder="admin"
              {...form.getInputProps('username')}
            />
            <PasswordInput
              label={t('auth.password')}
              placeholder="Your password"
              {...form.getInputProps('password')}
            />
            <Button type="submit" fullWidth loading={loading}>
              {t('auth.loginButton')}
            </Button>
          </Stack>
        </form>
      </Paper>
    </Container>
  );
}
