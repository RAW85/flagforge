/** Admin user management (ADMIN role required on backend). */
import { api } from './client'
import type { PlatformUser, UserRole } from '@/types/api'

export const usersApi = {
  list: () => api.get<PlatformUser[]>('/users').then((r) => r.data),

  updateRole: (id: string, role: UserRole) =>
    api.put<PlatformUser>(`/users/${id}/role`, { role }).then((r) => r.data),

  setEnabled: (id: string, enabled: boolean) =>
    api.put<PlatformUser>(`/users/${id}/enabled`, { enabled }).then((r) => r.data),
}
