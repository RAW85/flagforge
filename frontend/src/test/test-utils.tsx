import type { ReactElement, ReactNode } from 'react'
import { render, type RenderOptions } from '@testing-library/react'
import { Provider } from 'react-redux'
import { MemoryRouter } from 'react-router-dom'
import { configureStore } from '@reduxjs/toolkit'
import authReducer from '@/features/auth/authSlice'
import flagsReducer from '@/features/flags/flagsSlice'
import evaluationReducer from '@/features/evaluation/evaluationSlice'
import rolloutsReducer from '@/features/rollouts/rolloutsSlice'
import apiKeysReducer from '@/features/apiKeys/apiKeysSlice'
import usersReducer from '@/features/users/usersSlice'
import type { RootState } from '@/store'

export function createTestStore(preloadedState?: Partial<RootState>) {
  return configureStore({
    reducer: {
      auth: authReducer,
      flags: flagsReducer,
      evaluation: evaluationReducer,
      rollouts: rolloutsReducer,
      apiKeys: apiKeysReducer,
      users: usersReducer,
    },
    preloadedState: preloadedState as RootState | undefined,
    middleware: (getDefaultMiddleware) =>
      getDefaultMiddleware({ thunk: false, serializableCheck: false }),
  })
}

export function renderWithProviders(
  ui: ReactElement,
  {
    preloadedState,
    route = '/',
    ...options
  }: {
    preloadedState?: Partial<RootState>
    route?: string
  } & Omit<RenderOptions, 'wrapper'> = {},
) {
  const store = createTestStore(preloadedState)

  function Wrapper({ children }: { children: ReactNode }) {
    return (
      <Provider store={store}>
        <MemoryRouter initialEntries={[route]}>{children}</MemoryRouter>
      </Provider>
    )
  }

  return {
    store,
    ...render(ui, { wrapper: Wrapper, ...options }),
  }
}
