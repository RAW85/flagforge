/**
 * Redux store: RTK slices + redux-saga (thunks disabled).
 * Feature state lives under features/*; side effects in *Saga.ts.
 */
import { configureStore } from '@reduxjs/toolkit'
import createSagaMiddleware from 'redux-saga'
import authReducer from '@/features/auth/authSlice'
import flagsReducer from '@/features/flags/flagsSlice'
import evaluationReducer from '@/features/evaluation/evaluationSlice'
import rolloutsReducer from '@/features/rollouts/rolloutsSlice'
import apiKeysReducer from '@/features/apiKeys/apiKeysSlice'
import usersReducer from '@/features/users/usersSlice'
import { rootSaga } from './rootSaga'

const sagaMiddleware = createSagaMiddleware()

export const store = configureStore({
  reducer: {
    auth: authReducer,
    flags: flagsReducer,
    evaluation: evaluationReducer,
    rollouts: rolloutsReducer,
    apiKeys: apiKeysReducer,
    users: usersReducer,
  },
  middleware: (getDefaultMiddleware) =>
    // serializableCheck off: saga action payloads may hold non-serializable values
    getDefaultMiddleware({ thunk: false, serializableCheck: false }).concat(sagaMiddleware),
})

sagaMiddleware.run(rootSaga)

export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch
