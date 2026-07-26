/** Progressive rollout sagas (PERCENTAGE flags only). */
import { api } from './client'
import type { RolloutSaga } from '@/types/api'

export const rolloutsApi = {
  start: (payload: { flagId: string; steps?: number[] }) =>
    api.post<RolloutSaga>('/rollouts', payload).then((r) => r.data),

  get: (id: string) => api.get<RolloutSaga>(`/rollouts/${id}`).then((r) => r.data),

  listByFlag: (flagId: string) =>
    api.get<RolloutSaga[]>('/rollouts', { params: { flagId } }).then((r) => r.data),

  advance: (id: string) => api.post<RolloutSaga>(`/rollouts/${id}/advance`).then((r) => r.data),

  rollback: (id: string, reason?: string) =>
    api.post<RolloutSaga>(`/rollouts/${id}/rollback`, { reason }).then((r) => r.data),
}
