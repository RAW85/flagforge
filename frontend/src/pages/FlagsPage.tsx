/**
 * Flag list, filters, create form, cursor "load more".
 * Mutations gated by canMutateFlags / canDeleteFlags.
 */
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { flagsActions } from '@/features/flags/flagsSlice'
import { canDeleteFlags, canMutateFlags } from '@/features/auth/authSlice'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Badge } from '@/components/ui/Badge'
import { Spinner } from '@/components/ui/Spinner'
import type { Environment, FlagStatus, FlagType } from '@/types/api'

const createSchema = z.object({
  key: z
    .string()
    .min(1)
    .regex(/^[a-z0-9]+(?:-[a-z0-9]+)*$/, 'kebab-case only'),
  name: z.string().min(1),
  description: z.string().optional(),
  flagType: z.enum(['BOOLEAN', 'PERCENTAGE', 'MULTIVARIATE']),
  environment: z.enum(['DEVELOPMENT', 'STAGING', 'PRODUCTION']),
  defaultValue: z.string().optional(),
  percentage: z.coerce.number().min(0).max(100).optional(),
})

type CreateForm = z.infer<typeof createSchema>

function statusTone(status: FlagStatus) {
  if (status === 'ACTIVE') return 'success'
  if (status === 'ARCHIVED') return 'neutral'
  return 'warning'
}

export function FlagsPage() {
  const dispatch = useAppDispatch()
  const { items, loading, saving, error, hasMore } = useAppSelector((s) => s.flags)
  const role = useAppSelector((s) => s.auth.user?.role ?? null)
  const canMutate = canMutateFlags(role)
  const canDelete = canDeleteFlags(role)
  const [showCreate, setShowCreate] = useState(false)
  const [envFilter, setEnvFilter] = useState<Environment | ''>('')

  const {
    register,
    handleSubmit,
    watch,
    reset,
    formState: { errors },
  } = useForm<CreateForm>({
    resolver: zodResolver(createSchema),
    defaultValues: {
      flagType: 'BOOLEAN',
      environment: 'DEVELOPMENT',
      defaultValue: 'false',
      percentage: 0,
    },
  })

  const flagType = watch('flagType')

  useEffect(() => {
    dispatch(
      flagsActions.fetchFlagsRequest({
        environment: envFilter || undefined,
      }),
    )
  }, [dispatch, envFilter])

  const onCreate = (values: CreateForm) => {
    dispatch(
      flagsActions.createFlagRequest({
        key: values.key,
        name: values.name,
        description: values.description,
        flagType: values.flagType as FlagType,
        environment: values.environment as Environment,
        defaultValue: values.defaultValue || 'false',
        percentage: values.flagType === 'PERCENTAGE' ? values.percentage : undefined,
      }),
    )
    setShowCreate(false)
    reset()
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="text-left">
          <h1 className="text-2xl font-semibold text-slate-50">Feature Flags</h1>
          <p className="mt-1 text-sm text-slate-400">
            Create, toggle, and manage flags across environments.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Select
            className="w-44"
            value={envFilter}
            onChange={(e) => setEnvFilter(e.target.value as Environment | '')}
          >
            <option value="">All environments</option>
            <option value="DEVELOPMENT">DEVELOPMENT</option>
            <option value="STAGING">STAGING</option>
            <option value="PRODUCTION">PRODUCTION</option>
          </Select>
          {canMutate && (
            <Button onClick={() => setShowCreate((v) => !v)}>
              {showCreate ? 'Cancel' : 'New flag'}
            </Button>
          )}
        </div>
      </div>

      {showCreate && canMutate && (
        <Card title="Create feature flag">
          <form className="grid gap-3 md:grid-cols-2" onSubmit={handleSubmit(onCreate)}>
            <Input label="Key" placeholder="new-checkout-flow" error={errors.key?.message} {...register('key')} />
            <Input label="Name" placeholder="New Checkout Flow" error={errors.name?.message} {...register('name')} />
            <Input label="Description" {...register('description')} />
            <Select label="Environment" {...register('environment')}>
              <option value="DEVELOPMENT">DEVELOPMENT</option>
              <option value="STAGING">STAGING</option>
              <option value="PRODUCTION">PRODUCTION</option>
            </Select>
            <Select label="Type" {...register('flagType')}>
              <option value="BOOLEAN">BOOLEAN</option>
              <option value="PERCENTAGE">PERCENTAGE</option>
              <option value="MULTIVARIATE">MULTIVARIATE</option>
            </Select>
            <Input label="Default value" {...register('defaultValue')} />
            {flagType === 'PERCENTAGE' && (
              <Input
                label="Percentage"
                type="number"
                min={0}
                max={100}
                error={errors.percentage?.message}
                {...register('percentage')}
              />
            )}
            <div className="md:col-span-2">
              <Button type="submit" disabled={saving}>
                {saving ? 'Creating…' : 'Create flag'}
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
          <p className="text-sm text-slate-400">No flags yet. Create your first one.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full min-w-[720px] text-left text-sm">
              <thead className="text-xs uppercase tracking-wide text-slate-500">
                <tr className="border-b border-border">
                  <th className="px-2 py-2 font-medium">Flag</th>
                  <th className="px-2 py-2 font-medium">Env</th>
                  <th className="px-2 py-2 font-medium">Type</th>
                  <th className="px-2 py-2 font-medium">Status</th>
                  <th className="px-2 py-2 font-medium">Enabled</th>
                  <th className="px-2 py-2 font-medium">Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((flag) => (
                  <tr key={flag.id} className="border-b border-border/70 hover:bg-white/[0.02]">
                    <td className="px-2 py-3">
                      <Link
                        to={`/flags/${flag.id}`}
                        className="font-medium text-indigo-300 hover:text-indigo-200"
                      >
                        {flag.key}
                      </Link>
                      <div className="text-xs text-slate-500">{flag.name}</div>
                    </td>
                    <td className="px-2 py-3 text-slate-300">{flag.environment}</td>
                    <td className="px-2 py-3">
                      <Badge tone="info">{flag.flagType}</Badge>
                    </td>
                    <td className="px-2 py-3">
                      <Badge tone={statusTone(flag.status)}>{flag.status}</Badge>
                    </td>
                    <td className="px-2 py-3">
                      <Badge tone={flag.enabled ? 'success' : 'neutral'}>
                        {flag.enabled ? 'ON' : 'OFF'}
                      </Badge>
                    </td>
                    <td className="px-2 py-3">
                      <div className="flex flex-wrap gap-2">
                        <Link to={`/flags/${flag.id}`}>
                          <Button variant="ghost" type="button">
                            Open
                          </Button>
                        </Link>
                        {canMutate && (
                          <Button
                            variant="secondary"
                            type="button"
                            disabled={saving}
                            onClick={() =>
                              dispatch(
                                flagsActions.toggleFlagRequest({
                                  id: flag.id,
                                  enabled: !flag.enabled,
                                }),
                              )
                            }
                          >
                            {flag.enabled ? 'Disable' : 'Enable'}
                          </Button>
                        )}
                        {canDelete && (
                          <Button
                            variant="danger"
                            type="button"
                            disabled={saving}
                            onClick={() => {
                              if (confirm(`Delete flag ${flag.key}?`)) {
                                dispatch(flagsActions.deleteFlagRequest(flag.id))
                              }
                            }}
                          >
                            Delete
                          </Button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
        {hasMore && (
          <div className="mt-4">
            <Button
              variant="secondary"
              disabled={loading}
              onClick={() =>
                dispatch(
                  flagsActions.fetchFlagsRequest({
                    append: true,
                    environment: envFilter || undefined,
                  }),
                )
              }
            >
              Load more
            </Button>
          </div>
        )}
      </Card>
    </div>
  )
}
