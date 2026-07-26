import { describe, it, expect, beforeEach } from 'vitest'
import authReducer, {
  authActions,
  canDeleteFlags,
  canManageApiKeys,
  canManageUsers,
  canMutateFlags,
  selectIsAuthenticated,
} from './authSlice'
import type { AuthState } from './authSlice'

function emptyState(overrides: Partial<AuthState> = {}): AuthState {
  return {
    token: null,
    user: null,
    loading: false,
    error: null,
    bootstrapped: false,
    ...overrides,
  }
}

describe('authSlice', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('sets loading on loginRequest', () => {
    const state = authReducer(
      emptyState({ error: 'old' }),
      authActions.loginRequest({ email: 'a@b.com', password: 'x' }),
    )
    expect(state.loading).toBe(true)
    expect(state.error).toBeNull()
  })

  it('stores token and user on authSuccess', () => {
    const state = authReducer(
      emptyState({ loading: true }),
      authActions.authSuccess({
        userId: 'u1',
        username: 'admin',
        email: 'admin@flagforge.local',
        role: 'ADMIN',
        accessToken: 'jwt-token',
        tokenType: 'Bearer',
        expiresInMs: 1000,
      }),
    )
    expect(state.loading).toBe(false)
    expect(state.token).toBe('jwt-token')
    expect(state.user).toEqual({
      id: 'u1',
      username: 'admin',
      email: 'admin@flagforge.local',
      role: 'ADMIN',
    })
    expect(state.bootstrapped).toBe(true)
  })

  it('clears session on logout', () => {
    const state = authReducer(
      emptyState({
        token: 'jwt',
        user: { id: '1', username: 'a', email: 'a@b.c', role: 'VIEWER' },
      }),
      authActions.logout(),
    )
    expect(state.token).toBeNull()
    expect(state.user).toBeNull()
    expect(state.bootstrapped).toBe(true)
  })

  it('records authFailure message', () => {
    const state = authReducer(
      emptyState({ loading: true }),
      authActions.authFailure('Invalid credentials'),
    )
    expect(state.loading).toBe(false)
    expect(state.error).toBe('Invalid credentials')
  })

  it('bootstrapFailure clears token', () => {
    const state = authReducer(
      emptyState({ token: 'stale', loading: true }),
      authActions.bootstrapFailure(),
    )
    expect(state.token).toBeNull()
    expect(state.user).toBeNull()
    expect(state.bootstrapped).toBe(true)
  })
})

describe('auth role helpers', () => {
  it('selectIsAuthenticated requires token and user', () => {
    expect(
      selectIsAuthenticated({
        auth: emptyState({ token: 't', user: null }),
      }),
    ).toBe(false)
    expect(
      selectIsAuthenticated({
        auth: emptyState({
          token: 't',
          user: { id: '1', username: 'a', email: 'a@b.c', role: 'VIEWER' },
        }),
      }),
    ).toBe(true)
  })

  it('role permission helpers', () => {
    expect(canMutateFlags('VIEWER')).toBe(false)
    expect(canMutateFlags('EDITOR')).toBe(true)
    expect(canDeleteFlags('EDITOR')).toBe(false)
    expect(canDeleteFlags('ADMIN')).toBe(true)
    expect(canManageApiKeys('VIEWER')).toBe(false)
    expect(canManageApiKeys('EDITOR')).toBe(true)
    expect(canManageUsers('EDITOR')).toBe(false)
    expect(canManageUsers('ADMIN')).toBe(true)
  })
})
