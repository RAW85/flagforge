/**
 * Interactive flag evaluation (JWT). Query params `flagKey`/`environment`
 * prefill the form. Prefer response `enabled` for on/off; `bucket` is 0–99 sticky.
 */
import { useEffect } from 'react'
import { useSearchParams } from 'react-router-dom'
import { useForm } from 'react-hook-form'
import { z } from 'zod'
import { zodResolver } from '@hookform/resolvers/zod'
import { useAppDispatch, useAppSelector } from '@/store/hooks'
import { evaluationActions } from '@/features/evaluation/evaluationSlice'
import { Card } from '@/components/ui/Card'
import { Input } from '@/components/ui/Input'
import { Select } from '@/components/ui/Select'
import { Button } from '@/components/ui/Button'
import { Badge } from '@/components/ui/Badge'
import type { Environment } from '@/types/api'

const schema = z.object({
  flagKey: z.string().min(1),
  environment: z.enum(['DEVELOPMENT', 'STAGING', 'PRODUCTION']),
  subjectId: z.string().min(1),
  record: z.boolean().optional(),
})

type FormValues = z.infer<typeof schema>

export function EvaluatePage() {
  const dispatch = useAppDispatch()
  const [params] = useSearchParams()
  const { result, loading, error, history } = useAppSelector((s) => s.evaluation)

  const {
    register,
    handleSubmit,
    reset,
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      flagKey: params.get('flagKey') ?? '',
      environment: (params.get('environment') as Environment) || 'DEVELOPMENT',
      subjectId: '',
      record: false,
    },
  })

  useEffect(() => {
    reset({
      flagKey: params.get('flagKey') ?? '',
      environment: (params.get('environment') as Environment) || 'DEVELOPMENT',
      subjectId: '',
      record: false,
    })
  }, [params, reset])

  return (
    <div className="space-y-6">
      <div className="text-left">
        <h1 className="text-2xl font-semibold text-slate-50">Evaluate</h1>
        <p className="mt-1 text-sm text-slate-400">
          Run cache-backed flag evaluation for a subject (sticky bucketing).
        </p>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        <Card title="Evaluation request">
          <form
            className="space-y-3"
            onSubmit={handleSubmit((values) =>
              dispatch(
                evaluationActions.evaluateRequest({
                  flagKey: values.flagKey,
                  environment: values.environment,
                  subjectId: values.subjectId,
                  record: values.record,
                }),
              ),
            )}
          >
            <Input label="Flag key" placeholder="dark-mode" {...register('flagKey')} />
            <Select label="Environment" {...register('environment')}>
              <option value="DEVELOPMENT">DEVELOPMENT</option>
              <option value="STAGING">STAGING</option>
              <option value="PRODUCTION">PRODUCTION</option>
            </Select>
            <Input label="Subject ID" placeholder="user-123" {...register('subjectId')} />
            <label className="flex items-center gap-2 text-sm text-slate-300">
              <input type="checkbox" className="rounded border-border" {...register('record')} />
              Record evaluation history
            </label>
            {error && (
              <div className="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-300">
                {error}
              </div>
            )}
            <Button type="submit" disabled={loading}>
              {loading ? 'Evaluating…' : 'Evaluate'}
            </Button>
          </form>
        </Card>

        <Card title="Result">
          {!result ? (
            <p className="text-sm text-slate-400">Run an evaluation to see the result.</p>
          ) : (
            <div className="space-y-3 text-sm">
              <div className="flex flex-wrap gap-2">
                <Badge tone={result.enabled ? 'success' : 'neutral'}>
                  {result.enabled ? 'enabled' : 'disabled'}
                </Badge>
                <Badge tone="info">{result.reason}</Badge>
                <Badge tone="brand">{result.flagType}</Badge>
              </div>
              <div className="rounded-xl bg-black/30 p-4 font-mono text-slate-100">
                value: <span className="text-indigo-300">{result.value}</span>
              </div>
              <dl className="space-y-2">
                <div className="flex justify-between">
                  <dt className="text-slate-500">Flag</dt>
                  <dd>{result.flagKey}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-slate-500">Subject</dt>
                  <dd className="font-mono">{result.subjectId}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-slate-500">Bucket</dt>
                  <dd className="font-mono">{result.bucket ?? '—'}</dd>
                </div>
                <div className="flex justify-between">
                  <dt className="text-slate-500">Environment</dt>
                  <dd>{result.environment}</dd>
                </div>
              </dl>
            </div>
          )}
        </Card>
      </div>

      {history.length > 0 && (
        <Card title="Recent evaluations">
          <div className="space-y-2">
            {history.map((item, idx) => (
              <div
                key={`${item.flagKey}-${item.subjectId}-${idx}`}
                className="flex flex-wrap items-center justify-between gap-2 rounded-lg border border-border/70 px-3 py-2 text-sm"
              >
                <div>
                  <span className="font-medium text-slate-200">{item.flagKey}</span>
                  <span className="mx-2 text-slate-600">·</span>
                  <span className="font-mono text-xs text-slate-400">{item.subjectId}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Badge tone={item.enabled ? 'success' : 'neutral'}>{item.value}</Badge>
                  <span className="text-xs text-slate-500">{item.reason}</span>
                </div>
              </div>
            ))}
          </div>
        </Card>
      )}
    </div>
  )
}
