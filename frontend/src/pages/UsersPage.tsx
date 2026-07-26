/** ADMIN-only user table: change role / enable / disable. */
import { useEffect } from 'react'
import { Navigate } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { canManageUsers } from '@/features/auth/authSlice'
import { usersActions } from '@/features/users/usersSlice'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Spinner } from '@/components/ui/Spinner'
import type { UserRole } from '@/types/api'

const roles: UserRole[] = ['ADMIN', 'EDITOR', 'VIEWER']

export function UsersPage() {
  const dispatch = useAppDispatch()
  const currentUser = useAppSelector((s) => s.auth.user)
  const role = currentUser?.role ?? null
  const { items, loading, saving, error } = useAppSelector((s) => s.users)

  useEffect(() => {
    if (canManageUsers(role)) {
      dispatch(usersActions.fetchUsersRequest())
    }
  }, [dispatch, role])

  if (!canManageUsers(role)) {
    return <Navigate to="/flags" replace />
  }

  return (
    <div className="space-y-6">
      <div className="text-left">
        <h1 className="text-2xl font-semibold text-slate-50">Users</h1>
        <p className="mt-1 text-sm text-slate-400">
          Manage roles and account status (ADMIN only).
        </p>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
          {error}
        </div>
      )}

      <Card>
        {loading && items.length === 0 ? (
          <Spinner />
        ) : items.length === 0 ? (
          <p className="text-sm text-slate-400">No users found.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-xs uppercase tracking-wide text-slate-500">
                <tr className="border-b border-border">
                  <th className="px-2 py-2 font-medium">User</th>
                  <th className="px-2 py-2 font-medium">Role</th>
                  <th className="px-2 py-2 font-medium">Status</th>
                  <th className="px-2 py-2 font-medium">Created</th>
                  <th className="px-2 py-2 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((user) => {
                  const isSelf = user.id === currentUser?.id
                  return (
                    <tr key={user.id} className="border-b border-border/70">
                      <td className="px-2 py-3">
                        <div className="font-medium text-slate-200">
                          {user.username}
                          {isSelf && (
                            <span className="ml-2 text-xs text-indigo-300">(you)</span>
                          )}
                        </div>
                        <div className="text-xs text-slate-500">{user.email}</div>
                      </td>
                      <td className="px-2 py-3">
                        <select
                          className="rounded-lg border border-border bg-panel px-2 py-1.5 text-sm text-slate-100"
                          value={user.role}
                          disabled={saving}
                          onChange={(e) =>
                            dispatch(
                              usersActions.updateUserRoleRequest({
                                id: user.id,
                                role: e.target.value as UserRole,
                              }),
                            )
                          }
                        >
                          {roles.map((r) => (
                            <option key={r} value={r}>
                              {r}
                            </option>
                          ))}
                        </select>
                      </td>
                      <td className="px-2 py-3">
                        <Badge tone={user.enabled ? 'success' : 'danger'}>
                          {user.enabled ? 'ENABLED' : 'DISABLED'}
                        </Badge>
                      </td>
                      <td className="px-2 py-3 text-xs text-slate-500">
                        {new Date(user.createdAt).toLocaleString()}
                      </td>
                      <td className="px-2 py-3">
                        <Button
                          variant={user.enabled ? 'danger' : 'secondary'}
                          type="button"
                          disabled={saving || isSelf}
                          title={isSelf ? 'You cannot disable your own account' : undefined}
                          onClick={() =>
                            dispatch(
                              usersActions.setUserEnabledRequest({
                                id: user.id,
                                enabled: !user.enabled,
                              }),
                            )
                          }
                        >
                          {user.enabled ? 'Disable' : 'Enable'}
                        </Button>
                      </td>
                    </tr>
                  )
                })}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}
