/** Single flag detail: toggle + linked rollout sagas for PERCENTAGE flags. */
import { useEffect } from 'react'
import { Link, useParams } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { flagsActions } from '@/features/flags/flagsSlice'
import { rolloutsActions } from '@/features/rollouts/rolloutsSlice'
import { canMutateFlags } from '@/features/auth/authSlice'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import { Spinner } from '@/components/ui/Spinner'

export function FlagDetailPage() {
  const { id = '' } = useParams()
  const dispatch = useAppDispatch()
  const { selected, loading, saving, error } = useAppSelector((s) => s.flags)
  const rollouts = useAppSelector((s) => s.rollouts)
  const role = useAppSelector((s) => s.auth.user?.role ?? null)
  const canMutate = canMutateFlags(role)

  useEffect(() => {
    if (id) {
      dispatch(flagsActions.fetchFlagRequest(id))
      dispatch(rolloutsActions.fetchRolloutsRequest(id))
    }
  }, [dispatch, id])

  if (loading && !selected) {
    return <Spinner />
  }

  if (!selected) {
    return (
      <Card>
        <p className="text-sm text-slate-400">Flag not found.</p>
        <Link to="/flags" className="mt-3 inline-block text-sm text-indigo-300">
          Back to flags
        </Link>
      </Card>
    )
  }

  const running = rollouts.items.find((s) => s.status === 'RUNNING')

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="text-left">
          <Link to="/flags" className="text-xs text-slate-500 hover:text-slate-300">
            ← Flags
          </Link>
          <h1 className="mt-1 text-2xl font-semibold text-slate-50">{selected.key}</h1>
          <p className="mt-1 text-sm text-slate-400">{selected.name}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          <Badge tone={selected.enabled ? 'success' : 'neutral'}>
            {selected.enabled ? 'ENABLED' : 'DISABLED'}
          </Badge>
          <Badge tone="info">{selected.flagType}</Badge>
          <Badge tone="brand">{selected.environment}</Badge>
          <Badge>{selected.status}</Badge>
        </div>
      </div>

      {error && (
        <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
          {error}
        </div>
      )}

      <div className="grid gap-4 lg:grid-cols-2">
        <Card title="Details">
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">Default value</dt>
              <dd className="font-mono text-slate-200">{selected.defaultValue}</dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">Percentage</dt>
              <dd className="font-mono text-slate-200">
                {selected.percentage ?? '—'}
              </dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">Created</dt>
              <dd className="text-slate-300">
                {new Date(selected.createdAt).toLocaleString()}
              </dd>
            </div>
            <div className="flex justify-between gap-4">
              <dt className="text-slate-500">Updated</dt>
              <dd className="text-slate-300">
                {new Date(selected.updatedAt).toLocaleString()}
              </dd>
            </div>
            {selected.description && (
              <div>
                <dt className="mb-1 text-slate-500">Description</dt>
                <dd className="text-slate-300">{selected.description}</dd>
              </div>
            )}
            {selected.rulesJson && (
              <div>
                <dt className="mb-1 text-slate-500">Rules JSON</dt>
                <dd className="overflow-x-auto rounded-lg bg-black/30 p-3 font-mono text-xs text-slate-300">
                  {selected.rulesJson}
                </dd>
              </div>
            )}
          </dl>
          {canMutate && (
            <div className="mt-5 flex flex-wrap gap-2">
              <Button
                disabled={saving}
                onClick={() =>
                  dispatch(
                    flagsActions.toggleFlagRequest({
                      id: selected.id,
                      enabled: !selected.enabled,
                    }),
                  )
                }
              >
                {selected.enabled ? 'Disable flag' : 'Enable flag'}
              </Button>
              <Link to={`/evaluate?flagKey=${encodeURIComponent(selected.key)}&environment=${selected.environment}`}>
                <Button variant="secondary" type="button">
                  Evaluate
                </Button>
              </Link>
            </div>
          )}
        </Card>

        <Card
          title="Rollout saga"
          action={
            selected.flagType === 'PERCENTAGE' && canMutate && !running ? (
              <Button
                disabled={rollouts.saving}
                onClick={() =>
                  dispatch(
                    rolloutsActions.startRolloutRequest({
                      flagId: selected.id,
                      steps: [0, 10, 25, 50, 100],
                    }),
                  )
                }
              >
                Start rollout
              </Button>
            ) : null
          }
        >
          {selected.flagType !== 'PERCENTAGE' ? (
            <p className="text-sm text-slate-400">
              Progressive rollouts are available for PERCENTAGE flags only.
            </p>
          ) : rollouts.loading && rollouts.items.length === 0 ? (
            <Spinner />
          ) : rollouts.items.length === 0 ? (
            <p className="text-sm text-slate-400">No rollouts yet for this flag.</p>
          ) : (
            <div className="space-y-3">
              {rollouts.items.map((saga) => (
                <div
                  key={saga.id}
                  className="rounded-xl border border-border bg-panel-2/50 p-3 text-sm"
                >
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="font-medium text-slate-200">
                      Step {saga.currentStepIndex + 1}/{saga.steps.length} · {saga.currentPercentage}%
                    </div>
                    <Badge
                      tone={
                        saga.status === 'RUNNING'
                          ? 'warning'
                          : saga.status === 'COMPLETED'
                            ? 'success'
                            : saga.status === 'ROLLED_BACK'
                              ? 'danger'
                              : 'neutral'
                      }
                    >
                      {saga.status}
                    </Badge>
                  </div>
                  <div className="mt-2 text-xs text-slate-500">
                    Steps: {saga.steps.join(' → ')}
                  </div>
                  {saga.status === 'RUNNING' && canMutate && (
                    <div className="mt-3 flex flex-wrap gap-2">
                      <Button
                        disabled={rollouts.saving || !saga.hasNextStep}
                        onClick={() => dispatch(rolloutsActions.advanceRolloutRequest(saga.id))}
                      >
                        Advance
                      </Button>
                      <Button
                        variant="danger"
                        disabled={rollouts.saving}
                        onClick={() =>
                          dispatch(
                            rolloutsActions.rollbackRolloutRequest({
                              id: saga.id,
                              reason: 'Rolled back from dashboard',
                            }),
                          )
                        }
                      >
                        Rollback
                      </Button>
                    </div>
                  )}
                  {saga.failureReason && (
                    <p className="mt-2 text-xs text-rose-300">{saga.failureReason}</p>
                  )}
                </div>
              ))}
            </div>
          )}
          {rollouts.error && (
            <p className="mt-3 text-sm text-rose-300">{rollouts.error}</p>
          )}
        </Card>
      </div>
    </div>
  )
}
