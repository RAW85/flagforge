import { describe, it, expect } from 'vitest'
import { act, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { LoginPage } from './LoginPage'
import { renderWithProviders } from '@/test/test-utils'
import { authActions } from '@/features/auth/authSlice'

const unauthenticated = {
  auth: {
    token: null,
    user: null,
    loading: false,
    error: null,
    bootstrapped: true,
  },
} as const

describe('LoginPage', () => {
  it('renders sign-in form', () => {
    renderWithProviders(<LoginPage />, {
      route: '/login',
      preloadedState: unauthenticated,
    })
    expect(screen.getByText('Sign in to FlagForge')).toBeInTheDocument()
    expect(screen.getByLabelText('Email')).toBeInTheDocument()
    expect(screen.getByLabelText('Password')).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /sign in/i })).toBeInTheDocument()
  })

  it('shows auth error after failure action', async () => {
    const { store } = renderWithProviders(<LoginPage />, {
      route: '/login',
      preloadedState: unauthenticated,
    })

    act(() => {
      store.dispatch(authActions.authFailure('Invalid email or password'))
    })

    expect(screen.getByText('Invalid email or password')).toBeInTheDocument()
  })

  it('sets loading state after valid submit', async () => {
    const user = userEvent.setup()
    const { store } = renderWithProviders(<LoginPage />, {
      route: '/login',
      preloadedState: unauthenticated,
    })

    await user.type(screen.getByLabelText('Email'), 'user@example.com')
    await user.type(screen.getByLabelText('Password'), 'password123')
    await user.click(screen.getByRole('button', { name: /sign in/i }))

    await waitFor(() => {
      expect(store.getState().auth.loading).toBe(true)
      expect(screen.getByRole('button', { name: /signing in/i })).toBeDisabled()
    })
  })
})
