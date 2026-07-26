/**
 * Routes + session bootstrap.
 * Public: /login, /register. Everything else under ProtectedRoute + AppShell.
 */
import { useEffect } from 'react'
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { authActions } from '@/features/auth/authSlice'
import { ProtectedRoute } from '@/components/auth/ProtectedRoute'
import { AppShell } from '@/components/layout/AppShell'
import { LoginPage } from '@/pages/LoginPage'
import { RegisterPage } from '@/pages/RegisterPage'
import { FlagsPage } from '@/pages/FlagsPage'
import { FlagDetailPage } from '@/pages/FlagDetailPage'
import { EvaluatePage } from '@/pages/EvaluatePage'
import { RolloutsPage } from '@/pages/RolloutsPage'
import { ApiKeysPage } from '@/pages/ApiKeysPage'
import { UsersPage } from '@/pages/UsersPage'
import { Spinner } from '@/components/ui/Spinner'

/** Waits for auth bootstrap (token → /me) before rendering routes. */
function Bootstrapper({ children }: { children: React.ReactNode }) {
  const dispatch = useAppDispatch()
  const bootstrapped = useAppSelector((s) => s.auth.bootstrapped)

  useEffect(() => {
    dispatch(authActions.bootstrapRequest())
  }, [dispatch])

  if (!bootstrapped) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Spinner label="Starting FlagForge…" />
      </div>
    )
  }

  return children
}

export default function App() {
  return (
    <BrowserRouter>
      <Bootstrapper>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/register" element={<RegisterPage />} />
          <Route
            element={
              <ProtectedRoute>
                <AppShell />
              </ProtectedRoute>
            }
          >
            <Route path="/" element={<Navigate to="/flags" replace />} />
            <Route path="/flags" element={<FlagsPage />} />
            <Route path="/flags/:id" element={<FlagDetailPage />} />
            <Route path="/evaluate" element={<EvaluatePage />} />
            <Route path="/rollouts" element={<RolloutsPage />} />
            <Route path="/api-keys" element={<ApiKeysPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Route>
          <Route path="*" element={<Navigate to="/flags" replace />} />
        </Routes>
      </Bootstrapper>
    </BrowserRouter>
  )
}
