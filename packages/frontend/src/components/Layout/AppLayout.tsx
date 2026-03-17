import { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import {
  AppShell,
  Burger,
  Group,
  Title,
  NavLink,
  ScrollArea,
  ActionIcon,
  Menu,
  Text,
  useMantineColorScheme,
  Divider,
  Badge,
} from '@mantine/core';
import { useDisclosure } from '@mantine/hooks';
import {
  IconHome,
  IconSettings,
  IconLogout,
  IconSun,
  IconMoon,
  IconUser,
  IconDeviceGamepad2,
} from '@tabler/icons-react';
import { getPlatforms } from '../../api/platforms';
import { useAuthStore } from '../../stores/auth';
import { logout as apiLogout } from '../../api/auth';

export function AppLayout() {
  const [opened, { toggle }] = useDisclosure();
  const { t } = useTranslation();
  const navigate = useNavigate();
  const location = useLocation();
  const { colorScheme, toggleColorScheme } = useMantineColorScheme();
  const { user, logout } = useAuthStore();

  const { data: platforms } = useQuery({
    queryKey: ['platforms'],
    queryFn: getPlatforms,
  });

  const handleLogout = async () => {
    try {
      await apiLogout();
    } catch {
      // Ignore errors on logout
    }
    logout();
    navigate('/login');
  };

  return (
    <AppShell
      header={{ height: 60 }}
      navbar={{ width: 260, breakpoint: 'sm', collapsed: { mobile: !opened } }}
      padding="md"
    >
      <AppShell.Header>
        <Group h="100%" px="md" justify="space-between">
          <Group>
            <Burger opened={opened} onClick={toggle} hiddenFrom="sm" size="sm" />
            <IconDeviceGamepad2 size={28} />
            <Title order={3}>RomAdmin</Title>
          </Group>
          <Group>
            <ActionIcon
              variant="default"
              size="lg"
              onClick={toggleColorScheme}
              aria-label="Toggle color scheme"
            >
              {colorScheme === 'dark' ? <IconSun size={18} /> : <IconMoon size={18} />}
            </ActionIcon>
            <Menu shadow="md" width={200}>
              <Menu.Target>
                <ActionIcon variant="default" size="lg">
                  <IconUser size={18} />
                </ActionIcon>
              </Menu.Target>
              <Menu.Dropdown>
                <Menu.Label>{user?.username}</Menu.Label>
                <Menu.Item leftSection={<IconLogout size={14} />} onClick={handleLogout}>
                  {t('auth.logout')}
                </Menu.Item>
              </Menu.Dropdown>
            </Menu>
          </Group>
        </Group>
      </AppShell.Header>

      <AppShell.Navbar p="xs">
        <AppShell.Section>
          <NavLink
            label={t('nav.dashboard')}
            leftSection={<IconHome size={18} />}
            active={location.pathname === '/'}
            onClick={() => { navigate('/'); toggle(); }}
          />
        </AppShell.Section>

        <Divider my="xs" label="Platforms" labelPosition="left" />

        <AppShell.Section grow component={ScrollArea}>
          {platforms?.map((platform) => (
            <NavLink
              key={platform.id}
              label={platform.displayName}
              rightSection={
                platform._count?.games ? (
                  <Badge size="sm" variant="light" color="violet">
                    {platform._count.games}
                  </Badge>
                ) : null
              }
              active={location.pathname === `/platforms/${platform.id}`}
              onClick={() => { navigate(`/platforms/${platform.id}`); toggle(); }}
            />
          ))}
        </AppShell.Section>

        <AppShell.Section>
          <Divider my="xs" />
          {user?.role === 'ADMIN' && (
            <NavLink
              label={t('nav.settings')}
              leftSection={<IconSettings size={18} />}
              active={location.pathname === '/settings'}
              onClick={() => { navigate('/settings'); toggle(); }}
            />
          )}
        </AppShell.Section>
      </AppShell.Navbar>

      <AppShell.Main>
        <Outlet />
      </AppShell.Main>
    </AppShell>
  );
}
