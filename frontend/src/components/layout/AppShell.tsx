/**
 * Authenticated chrome: nav (role-filtered) + outlet for page content.
 */
import { NavLink, Outlet } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import {
  authActions,
  canManageApiKeys,
  canManageUsers,
} from '@/features/auth/authSlice'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'

export function AppShell() {
  const dispatch = useAppDispatch()
  const user = useAppSelector((s) => s.auth.user)
  const role = user?.role ?? null

  const nav = [
    { to: '/flags', label: 'Flags', show: true },
    { to: '/evaluate', label: 'Evaluate', show: true },
    { to: '/rollouts', label: 'Rollouts', show: true },
    { to: '/api-keys', label: 'API Keys', show: canManageApiKeys(role) },
    { to: '/users', label: 'Users', show: canManageUsers(role) },
  ].filter((item) => item.show)

  return (
    <div className="min-h-screen">
      <header className="sticky top-0 z-20 border-b border-border/80 bg-surface/80 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3">
          <div className="flex items-center gap-6">
            <div className="flex items-center gap-2">
              <div className="flex h-9 w-9 items-center justify-center rounded-xl bg-indigo-600 text-sm font-bold text-white shadow-lg shadow-indigo-900/40">
                FF
              </div>
              <div className="text-left">
                <div className="text-sm font-semibold tracking-wide text-slate-100">FlagForge</div>
                <div className="text-[11px] text-slate-400">Feature Flag Platform</div>
              </div>
            </div>
            <nav className="hidden items-center gap-1 sm:flex">
              {nav.map((item) => (
                <NavLink
                  key={item.to}
                  to={item.to}
                  className={({ isActive }) =>
                    `rounded-lg px-3 py-1.5 text-sm transition ${
                      isActive
                        ? 'bg-indigo-500/15 text-indigo-300'
                        : 'text-slate-400 hover:bg-white/5 hover:text-slate-200'
                    }`
                  }
                >
                  {item.label}
                </NavLink>
              ))}
            </nav>
          </div>
          <div className="flex items-center gap-3">
            {user && (
              <div className="hidden text-right sm:block">
                <div className="text-sm text-slate-200">{user.username}</div>
                <div className="text-xs text-slate-500">{user.email}</div>
              </div>
            )}
            {user && <Badge tone="brand">{user.role}</Badge>}
            <Button variant="secondary" onClick={() => dispatch(authActions.logout())}>
              Logout
            </Button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-6">
        <Outlet />
      </main>
    </div>
  )
}
