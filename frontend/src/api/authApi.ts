/** Auth endpoints: register/login (public) and /me (JWT). */
import { api } from './client'
import type { AuthResponse, MeResponse } from '@/types/api'

export const authApi = {
  register: (payload: { username: string; email: string; password: string }) =>
    api.post<AuthResponse>('/auth/register', payload).then((r) => r.data),

  login: (payload: { email: string; password: string }) =>
    api.post<AuthResponse>('/auth/login', payload).then((r) => r.data),

  me: () => api.get<MeResponse>('/auth/me').then((r) => r.data),
}
