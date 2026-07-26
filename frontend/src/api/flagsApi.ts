/** Feature flag REST client (cursor list, CRUD, toggle). */
import { api } from './client'
import type {
  CreateFeatureFlagRequest,
  CursorPage,
  Environment,
  FeatureFlag,
  FlagStatus,
} from '@/types/api'

export const flagsApi = {
  list: (params?: {
    cursor?: string
    limit?: number
    environment?: Environment
    status?: FlagStatus
  }) => api.get<CursorPage<FeatureFlag>>('/flags', { params }).then((r) => r.data),

  getById: (id: string) => api.get<FeatureFlag>(`/flags/${id}`).then((r) => r.data),

  create: (payload: CreateFeatureFlagRequest) =>
    api.post<FeatureFlag>('/flags', payload).then((r) => r.data),

  update: (
    id: string,
    payload: Partial<{
      name: string
      description: string
      flagType: string
      status: FlagStatus
      defaultValue: string
      percentage: number
      rulesJson: string
    }>,
  ) => api.put<FeatureFlag>(`/flags/${id}`, payload).then((r) => r.data),

  toggle: (id: string, enabled: boolean) =>
    api.post<FeatureFlag>(`/flags/${id}/toggle`, { enabled }).then((r) => r.data),

  remove: (id: string) => api.delete(`/flags/${id}`).then((r) => r.data),
}
