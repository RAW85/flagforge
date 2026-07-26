/** Dashboard evaluation (JWT). SDK clients use `/sdk/evaluate` with X-API-Key instead. */
import { api } from './client'
import type { EvaluateRequest, EvaluateResponse } from '@/types/api'

export const evaluateApi = {
  evaluate: (payload: EvaluateRequest) =>
    api.post<EvaluateResponse>('/evaluate', payload).then((r) => r.data),
}
