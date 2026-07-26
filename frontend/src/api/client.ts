/**
 * Shared Axios client for dashboard JWT calls.
 * Vite proxies `/api/*` → backend (prefix `/api/` only — not SPA route `/api-keys`).
 * Bearer token is injected from localStorage.
 */
import axios, { type AxiosError } from 'axios'
import type { ApiError } from '@/types/api'

const TOKEN_KEY = 'flagforge_token'

/** JWT persistence (same key used by auth slice bootstrap). */
export const tokenStorage = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token: string) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
}

export const api = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  const token = tokenStorage.get()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/** Prefer backend `ApiError.message`, else Axios message, else fallback. */
export function getErrorMessage(error: unknown, fallback = 'Something went wrong'): string {
  const axiosError = error as AxiosError<ApiError>
  if (axiosError.response?.data?.message) {
    return axiosError.response.data.message
  }
  if (axiosError.message) {
    return axiosError.message
  }
  return fallback
}
