/**
 * Requires JWT session. Redirects to /login when unauthenticated.
 * Waits for bootstrap so a refresh does not flash the login page.
 */
import { Navigate, useLocation } from 'react-router-dom'
import { useAppSelector } from '@/store/hooks'
import { Spinner } from '@/components/ui/Spinner'

export function ProtectedRoute({ children }: { children: React.ReactNode }) {
  const location = useLocation()
  const { token, user, bootstrapped, loading } = useAppSelector((s) => s.auth)

  if (!bootstrapped || (token && loading && !user)) {
    return (
      <div className="flex min-h-screen items-center justify-center">
        <Spinner label="Checking session…" />
      </div>
    )
  }

  if (!token || !user) {
    return <Navigate to="/login" replace state={{ from: location.pathname }} />
  }

  return children
}
