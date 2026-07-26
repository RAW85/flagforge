/**
 * Create/list/revoke SDK keys (EDITOR+). After create, show `rawKey` once —
 * it is not reloaded from the API.
 */
import { useEffect, useState } from 'react'
import { Navigate } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { canManageApiKeys } from '@/features/auth/authSlice'
import { apiKeysActions } from '@/features/apiKeys/apiKeysSlice'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Badge } from '@/components/ui/Badge'
import { Spinner } from '@/components/ui/Spinner'
import type { Environment } from '@/types/api'

const schema = z.object({
  name: z.string().min(1, 'Name is required').max(100),
  environmentScope: z.enum(['', 'DEVELOPMENT', 'STAGING', 'PRODUCTION']),
})

type FormValues = z.infer<typeof schema>

export function ApiKeysPage() {
  const dispatch = useAppDispatch()
  const role = useAppSelector((s) => s.auth.user?.role ?? null)
  const { items, loading, saving, error, createdRawKey, createdWarning } = useAppSelector(
    (s) => s.apiKeys,
  )
  const [showCreate, setShowCreate] = useState(false)

  const {
    register,
    handleSubmit,
    reset,
    formState: { errors },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { name: '', environmentScope: '' },
  })

  useEffect(() => {
    if (canManageApiKeys(role)) {
      dispatch(apiKeysActions.fetchApiKeysRequest())
    }
  }, [dispatch, role])

  if (!canManageApiKeys(role)) {
    return <Navigate to="/flags" replace />
  }

  const onCreate = (values: FormValues) => {
    dispatch(
      apiKeysActions.createApiKeyRequest({
        name: values.name,
        environmentScope: (values.environmentScope || null) as Environment | null,
      }),
    )
    setShowCreate(false)
    reset()
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="text-left">
          <h1 className="text-2xl font-semibold text-slate-50">API Keys</h1>
          <p className="mt-1 text-sm text-slate-400">
            SDK credentials for <code className="text-indigo-300">/api/v1/sdk/evaluate</code>.
          </p>
        </div>
        <Button
          onClick={() => {
            setShowCreate((v) => !v)
            dispatch(apiKeysActions.clearCreatedRawKey())
          }}
        >
          {showCreate ? 'Cancel' : 'New API key'}
        </Button>
      </div>

      {createdRawKey && (
        <Card title="Copy your new key now">
          <p className="mb-2 text-sm text-amber-300">{createdWarning}</p>
          <div className="flex flex-wrap items-center gap-2">
            <code className="flex-1 break-all rounded-lg bg-black/40 px-3 py-2 font-mono text-sm text-emerald-300">
              {createdRawKey}
            </code>
            <Button
              variant="secondary"
              type="button"
              onClick={() => navigator.clipboard.writeText(createdRawKey)}
            >
              Copy
            </Button>
            <Button
              variant="ghost"
              type="button"
              onClick={() => dispatch(apiKeysActions.clearCreatedRawKey())}
            >
              Dismiss
            </Button>
          </div>
        </Card>
      )}

      {showCreate && (
        <Card title="Create API key">
          <form className="grid gap-3 md:grid-cols-2" onSubmit={handleSubmit(onCreate)}>
            <Input
              label="Name"
              placeholder="mobile-sdk"
              error={errors.name?.message}
              {...register('name')}
            />
            <Select label="Environment scope (optional)" {...register('environmentScope')}>
              <option value="">All environments</option>
              <option value="DEVELOPMENT">DEVELOPMENT</option>
              <option value="STAGING">STAGING</option>
              <option value="PRODUCTION">PRODUCTION</option>
            </Select>
            <div className="md:col-span-2">
              <Button type="submit" disabled={saving}>
                {saving ? 'Creating…' : 'Create key'}
              </Button>
            </div>
          </form>
        </Card>
      )}

      {error && (
        <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
          {error}
        </div>
      )}

      <Card>
        {loading && items.length === 0 ? (
          <Spinner />
        ) : items.length === 0 ? (
          <p className="text-sm text-slate-400">No API keys yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[640px] text-left text-sm">
              <thead className="text-xs uppercase tracking-wide text-slate-500">
                <tr className="border-b border-border">
                  <th className="px-2 py-2 font-medium">Name</th>
                  <th className="px-2 py-2 font-medium">Key</th>
                  <th className="px-2 py-2 font-medium">Scope</th>
                  <th className="px-2 py-2 font-medium">Status</th>
                  <th className="px-2 py-2 font-medium">Last used</th>
                  <th className="px-2 py-2 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((key) => (
                  <tr key={key.id} className="border-b border-border/70">
                    <td className="px-2 py-3 font-medium text-slate-200">{key.name}</td>
                    <td className="px-2 py-3 font-mono text-xs text-slate-400">{key.displayKey}</td>
                    <td className="px-2 py-3 text-slate-300">
                      {key.environmentScope ?? 'ALL'}
                    </td>
                    <td className="px-2 py-3">
                      <Badge tone={key.active ? 'success' : 'danger'}>
                        {key.active ? 'ACTIVE' : 'REVOKED'}
                      </Badge>
                    </td>
                    <td className="px-2 py-3 text-xs text-slate-500">
                      {key.lastUsedAt ? new Date(key.lastUsedAt).toLocaleString() : '—'}
                    </td>
                    <td className="px-2 py-3">
                      {key.active && (
                        <Button
                          variant="danger"
                          type="button"
                          disabled={saving}
                          onClick={() => {
                            if (confirm(`Revoke key "${key.name}"?`)) {
                              dispatch(apiKeysActions.revokeApiKeyRequest(key.id))
                            }
                          }}
                        >
                          Revoke
                        </Button>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </Card>
    </div>
  )
}
