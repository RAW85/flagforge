/**
 * Auth state + RBAC helpers.
 * Pattern: *Request actions set loading; sagas complete with Success/Failure.
 * `bootstrapped` means session restore finished (with or without a user).
 */
import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { AuthResponse, MeResponse, UserRole } from '@/types/api'

export interface AuthState {
  token: string | null
  user: MeResponse | null
  loading: boolean
  error: string | null
  /** True after bootstrap (token validated or cleared). Gates the app shell. */
  bootstrapped: boolean
}

const initialState: AuthState = {
  token: localStorage.getItem('flagforge_token'),
  user: null,
  loading: false,
  error: null,
  bootstrapped: false,
}

const authSlice = createSlice({
  name: 'auth',
  initialState,
  reducers: {
    loginRequest(state, _action: PayloadAction<{ email: string; password: string }>) {
      state.loading = true
      state.error = null
    },
    registerRequest(
      state,
      _action: PayloadAction<{ username: string; email: string; password: string }>,
    ) {
      state.loading = true
      state.error = null
    },
    authSuccess(state, action: PayloadAction<AuthResponse>) {
      state.loading = false
      state.error = null
      state.token = action.payload.accessToken
      state.user = {
        id: action.payload.userId,
        username: action.payload.username,
        email: action.payload.email,
        role: action.payload.role,
      }
      state.bootstrapped = true
    },
    bootstrapRequest(state) {
      state.loading = Boolean(state.token)
      state.error = null
    },
    bootstrapSuccess(state, action: PayloadAction<MeResponse>) {
      state.loading = false
      state.user = action.payload
      state.bootstrapped = true
    },
    bootstrapFailure(state) {
      state.loading = false
      state.token = null
      state.user = null
      state.bootstrapped = true
    },
    authFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    logout(state) {
      state.token = null
      state.user = null
      state.error = null
      state.loading = false
      state.bootstrapped = true
    },
    clearAuthError(state) {
      state.error = null
    },
  },
})

export const authActions = authSlice.actions
export default authSlice.reducer

export const selectIsAuthenticated = (state: { auth: AuthState }) =>
  Boolean(state.auth.token && state.auth.user)

export const selectUserRole = (state: { auth: AuthState }): UserRole | null =>
  state.auth.user?.role ?? null

/** EDITOR+ — create/update/toggle flags and rollouts. */
export const canMutateFlags = (role: UserRole | null) =>
  role === 'ADMIN' || role === 'EDITOR'

/** ADMIN only — delete flags. */
export const canDeleteFlags = (role: UserRole | null) => role === 'ADMIN'

/** EDITOR+ — manage SDK API keys. */
export const canManageApiKeys = (role: UserRole | null) =>
  role === 'ADMIN' || role === 'EDITOR'

/** ADMIN only — user admin UI. */
export const canManageUsers = (role: UserRole | null) => role === 'ADMIN'
