import { describe, it, expect } from 'vitest'
import { screen } from '@testing-library/react'
import { Route, Routes } from 'react-router-dom'
import { ProtectedRoute } from './ProtectedRoute'
import { renderWithProviders } from '@/test/test-utils'

describe('ProtectedRoute', () => {
  it('redirects unauthenticated users to login', () => {
    renderWithProviders(
      <Routes>
        <Route
          path="/flags"
          element={
            <ProtectedRoute>
              <div>Secret</div>
            </ProtectedRoute>
          }
        />
        <Route path="/login" element={<div>Login page</div>} />
      </Routes>,
      {
        route: '/flags',
        preloadedState: {
          auth: {
            token: null,
            user: null,
            loading: false,
            error: null,
            bootstrapped: true,
          },
        },
      },
    )
    expect(screen.getByText('Login page')).toBeInTheDocument()
    expect(screen.queryByText('Secret')).not.toBeInTheDocument()
  })

  it('renders children when authenticated', () => {
    renderWithProviders(
      <Routes>
        <Route
          path="/flags"
          element={
            <ProtectedRoute>
              <div>Secret</div>
            </ProtectedRoute>
          }
        />
      </Routes>,
      {
        route: '/flags',
        preloadedState: {
          auth: {
            token: 'jwt',
            user: {
              id: '1',
              username: 'admin',
              email: 'admin@test.com',
              role: 'ADMIN',
            },
            loading: false,
            error: null,
            bootstrapped: true,
          },
        },
      },
    )
    expect(screen.getByText('Secret')).toBeInTheDocument()
  })
})
