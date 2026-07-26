/**
 * Pick a PERCENTAGE flag, then start/advance/rollback its rollout saga.
 * Only PERCENTAGE flags appear in the selector.
 */
import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { flagsActions } from '@/features/flags/flagsSlice'
import { rolloutsActions } from '@/features/rollouts/rolloutsSlice'
import { canMutateFlags } from '@/features/auth/authSlice'
import { Card } from '@/components/ui/Card'
import { Button } from '@/components/ui/Button'
import { Select } from '@/components/ui/Select'
import { Badge } from '@/components/ui/Badge'
import { Spinner } from '@/components/ui/Spinner'

export function RolloutsPage() {
  const dispatch = useAppDispatch()
  const flags = useAppSelector((s) => s.flags.items)
  const flagsLoading = useAppSelector((s) => s.flags.loading)
  const rollouts = useAppSelector((s) => s.rollouts)
  const role = useAppSelector((s) => s.auth.user?.role ?? null)
  const canMutate = canMutateFlags(role)
  const [flagId, setFlagId] = useState('')

  const percentageFlags = flags.filter((f) => f.flagType === 'PERCENTAGE')

  useEffect(() => {
    dispatch(flagsActions.fetchFlagsRequest({}))
  }, [dispatch])

  useEffect(() => {
    if (!flagId && percentageFlags[0]) {
      setFlagId(percentageFlags[0].id)
    }
  }, [flagId, percentageFlags])

  useEffect(() => {
    if (flagId) {
      dispatch(rolloutsActions.fetchRolloutsRequest(flagId))
    }
  }, [dispatch, flagId])

  return (
    <div className="space-y-6">
      <div className="text-left">
        <h1 className="text-2xl font-semibold text-slate-50">Rollouts</h1>
        <p className="mt-1 text-sm text-slate-400">
          Progressive percentage release sagas with advance and rollback.
        </p>
      </div>

      <Card title="Select percentage flag">
        {flagsLoading && percentageFlags.length === 0 ? (
          <Spinner />
        ) : percentageFlags.length === 0 ? (
          <p className="text-sm text-slate-400">
            No PERCENTAGE flags found.{' '}
            <Link to="/flags" className="text-indigo-300 hover:text-indigo-200">
              Create one
            </Link>
          </p>
        ) : (
          <div className="flex flex-wrap items-end gap-3">
            <div className="min-w-[260px] flex-1">
              <Select
                label="Flag"
                value={flagId}
                onChange={(e) => setFlagId(e.target.value)}
              >
                {percentageFlags.map((f) => (
                  <option key={f.id} value={f.id}>
                    {f.key} ({f.environment}) — {f.percentage ?? 0}%
                  </option>
                ))}
              </Select>
            </div>
            {canMutate && flagId && (
              <Button
                disabled={rollouts.saving}
                onClick={() =>
                  dispatch(
                    rolloutsActions.startRolloutRequest({
                      flagId,
                      steps: [0, 10, 25, 50, 100],
                    }),
                  )
                }
              >
                Start saga 0→10→25→50→100
              </Button>
            )}
          </div>
        )}
      </Card>

      {rollouts.error && (
        <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
          {rollouts.error}
        </div>
      )}

      <Card title="Saga history">
        {rollouts.loading && rollouts.items.length === 0 ? (
          <Spinner />
        ) : rollouts.items.length === 0 ? (
          <p className="text-sm text-slate-400">No sagas for this flag yet.</p>
        ) : (
          <div className="space-y-3">
            {rollouts.items.map((saga) => (
              <div
                key={saga.id}
                className="rounded-xl border border-border bg-panel-2/40 p-4"
              >
                <div className="flex flex-wrap items-center justify-between gap-2">
                  <div className="text-left">
                    <div className="font-medium text-slate-100">{saga.flagKey}</div>
                    <div className="text-xs text-slate-500">
                      {saga.environment} · step {saga.currentStepIndex + 1}/{saga.steps.length} ·{' '}
                      {saga.currentPercentage}%
                    </div>
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

                <div className="mt-3 flex flex-wrap gap-1.5">
                  {saga.steps.map((step, idx) => (
                    <span
                      key={`${saga.id}-${step}-${idx}`}
                      className={`rounded-md px-2 py-1 text-xs font-mono ${
                        idx === saga.currentStepIndex
                          ? 'bg-indigo-500/20 text-indigo-200 ring-1 ring-indigo-400/40'
                          : idx < saga.currentStepIndex
                            ? 'bg-emerald-500/10 text-emerald-300'
                            : 'bg-slate-700/40 text-slate-400'
                      }`}
                    >
                      {step}%
                    </span>
                  ))}
                </div>

                {saga.status === 'RUNNING' && canMutate && (
                  <div className="mt-4 flex flex-wrap gap-2">
                    <Button
                      disabled={rollouts.saving || !saga.hasNextStep}
                      onClick={() => dispatch(rolloutsActions.advanceRolloutRequest(saga.id))}
                    >
                      Advance step
                    </Button>
                    <Button
                      variant="danger"
                      disabled={rollouts.saving}
                      onClick={() =>
                        dispatch(
                          rolloutsActions.rollbackRolloutRequest({
                            id: saga.id,
                            reason: 'Rolled back from rollouts page',
                          }),
                        )
                      }
                    >
                      Rollback
                    </Button>
                    <Link to={`/flags/${saga.flagId}`}>
                      <Button variant="ghost" type="button">
                        View flag
                      </Button>
                    </Link>
                  </div>
                )}

                {saga.failureReason && (
                  <p className="mt-2 text-xs text-rose-300">{saga.failureReason}</p>
                )}
              </div>
            ))}
          </div>
        )}
      </Card>
    </div>
  )
}
