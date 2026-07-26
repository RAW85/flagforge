/**
 * Progressive rollouts for a selected PERCENTAGE flag.
 * After advance/rollback, sagas also refresh the flag so % stays in sync.
 */
import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { RolloutSaga } from '@/types/api'

export interface RolloutsState {
  items: RolloutSaga[]
  selected: RolloutSaga | null
  loading: boolean
  saving: boolean
  error: string | null
}

const initialState: RolloutsState = {
  items: [],
  selected: null,
  loading: false,
  saving: false,
  error: null,
}

const rolloutsSlice = createSlice({
  name: 'rollouts',
  initialState,
  reducers: {
    fetchRolloutsRequest(state, _action: PayloadAction<string>) {
      state.loading = true
      state.error = null
    },
    fetchRolloutsSuccess(state, action: PayloadAction<RolloutSaga[]>) {
      state.loading = false
      state.items = action.payload
      state.selected = action.payload[0] ?? null
    },
    fetchRolloutsFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    startRolloutRequest(
      state,
      _action: PayloadAction<{ flagId: string; steps?: number[] }>,
    ) {
      state.saving = true
      state.error = null
    },
    startRolloutSuccess(state, action: PayloadAction<RolloutSaga>) {
      state.saving = false
      state.selected = action.payload
      state.items = [action.payload, ...state.items]
    },
    startRolloutFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    advanceRolloutRequest(state, _action: PayloadAction<string>) {
      state.saving = true
      state.error = null
    },
    advanceRolloutSuccess(state, action: PayloadAction<RolloutSaga>) {
      state.saving = false
      state.selected = action.payload
      state.items = state.items.map((s) => (s.id === action.payload.id ? action.payload : s))
    },
    advanceRolloutFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    rollbackRolloutRequest(
      state,
      _action: PayloadAction<{ id: string; reason?: string }>,
    ) {
      state.saving = true
      state.error = null
    },
    rollbackRolloutSuccess(state, action: PayloadAction<RolloutSaga>) {
      state.saving = false
      state.selected = action.payload
      state.items = state.items.map((s) => (s.id === action.payload.id ? action.payload : s))
    },
    rollbackRolloutFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    clearRolloutsError(state) {
      state.error = null
    },
  },
})

export const rolloutsActions = rolloutsSlice.actions
export default rolloutsSlice.reducer
