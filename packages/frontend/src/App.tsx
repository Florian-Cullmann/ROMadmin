import { Routes, Route, Navigate } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { LoadingOverlay } from '@mantine/core';
import { getInstallStatus } from './api/install';
import { useAuthStore } from './stores/auth';
import { AppLayout } from './components/Layout/AppLayout';
import { ProtectedRoute } from './components/Layout/ProtectedRoute';
import { Install } from './pages/Install';
import { Login } from './pages/Login';
import { Dashboard } from './pages/Dashboard';
import { Platform } from './pages/Platform';
import { GameDetail } from './pages/GameDetail';
import { Settings } from './pages/Settings';

export function App() {
  const { data: installStatus, isLoading } = useQuery({
    queryKey: ['install-status'],
    queryFn: getInstallStatus,
  });

  const isAuthenticated = useAuthStore((s) => !!s.accessToken);

  if (isLoading) {
    return <LoadingOverlay visible />;
  }

  // Not installed yet — show installer
  if (installStatus && !installStatus.installed) {
    return (
      <Routes>
        <Route path="/install" element={<Install />} />
        <Route path="*" element={<Navigate to="/install" replace />} />
      </Routes>
    );
  }

  return (
    <Routes>
      <Route path="/login" element={isAuthenticated ? <Navigate to="/" replace /> : <Login />} />
      <Route path="/install" element={<Navigate to="/" replace />} />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <AppLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<Dashboard />} />
        <Route path="platforms/:id" element={<Platform />} />
        <Route path="games/:id" element={<GameDetail />} />
        <Route path="settings" element={<Settings />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
