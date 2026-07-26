/** SDK API key management. Create response includes one-time `rawKey`. */
import { api } from './client'
import type { ApiKey, CreateApiKeyResponse, Environment } from '@/types/api'

export const apiKeysApi = {
  list: () => api.get<ApiKey[]>('/api-keys').then((r) => r.data),

  create: (payload: { name: string; environmentScope?: Environment | null }) =>
    api.post<CreateApiKeyResponse>('/api-keys', payload).then((r) => r.data),

  revoke: (id: string) => api.delete<ApiKey>(`/api-keys/${id}`).then((r) => r.data),
}
