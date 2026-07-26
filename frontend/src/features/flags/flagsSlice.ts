/**
 * Feature flags list + detail selection.
 * Supports cursor pagination (`nextCursor`/`hasMore`) and env/status filters.
 */
import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type {
  CreateFeatureFlagRequest,
  CursorPage,
  Environment,
  FeatureFlag,
  FlagStatus,
} from '@/types/api'

export interface FlagsState {
  items: FeatureFlag[]
  selected: FeatureFlag | null
  nextCursor: string | null
  hasMore: boolean
  loading: boolean
  saving: boolean
  error: string | null
  filters: {
    environment?: Environment
    status?: FlagStatus
  }
}

const initialState: FlagsState = {
  items: [],
  selected: null,
  nextCursor: null,
  hasMore: false,
  loading: false,
  saving: false,
  error: null,
  filters: {},
}

const flagsSlice = createSlice({
  name: 'flags',
  initialState,
  reducers: {
    fetchFlagsRequest(
      state,
      action: PayloadAction<{ append?: boolean; environment?: Environment; status?: FlagStatus } | undefined>,
    ) {
      state.loading = true
      state.error = null
      if (action.payload?.environment !== undefined || action.payload?.status !== undefined) {
        state.filters = {
          environment: action.payload.environment,
          status: action.payload.status,
        }
      }
      if (!action.payload?.append) {
        state.items = []
        state.nextCursor = null
      }
    },
    fetchFlagsSuccess(
      state,
      action: PayloadAction<{ page: CursorPage<FeatureFlag>; append?: boolean }>,
    ) {
      state.loading = false
      state.items = action.payload.append
        ? [...state.items, ...action.payload.page.items]
        : action.payload.page.items
      state.nextCursor = action.payload.page.nextCursor
      state.hasMore = action.payload.page.hasMore
    },
    fetchFlagsFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    fetchFlagRequest(state, _action: PayloadAction<string>) {
      state.loading = true
      state.error = null
    },
    fetchFlagSuccess(state, action: PayloadAction<FeatureFlag>) {
      state.loading = false
      state.selected = action.payload
      const idx = state.items.findIndex((f) => f.id === action.payload.id)
      if (idx >= 0) state.items[idx] = action.payload
    },
    fetchFlagFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    createFlagRequest(state, _action: PayloadAction<CreateFeatureFlagRequest>) {
      state.saving = true
      state.error = null
    },
    createFlagSuccess(state, action: PayloadAction<FeatureFlag>) {
      state.saving = false
      state.items = [action.payload, ...state.items]
      state.selected = action.payload
    },
    createFlagFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    toggleFlagRequest(state, _action: PayloadAction<{ id: string; enabled: boolean }>) {
      state.saving = true
      state.error = null
    },
    toggleFlagSuccess(state, action: PayloadAction<FeatureFlag>) {
      state.saving = false
      state.selected = action.payload
      const idx = state.items.findIndex((f) => f.id === action.payload.id)
      if (idx >= 0) state.items[idx] = action.payload
    },
    toggleFlagFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    deleteFlagRequest(state, _action: PayloadAction<string>) {
      state.saving = true
      state.error = null
    },
    deleteFlagSuccess(state, action: PayloadAction<string>) {
      state.saving = false
      state.items = state.items.filter((f) => f.id !== action.payload)
      if (state.selected?.id === action.payload) state.selected = null
    },
    deleteFlagFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    clearFlagsError(state) {
      state.error = null
    },
    clearSelectedFlag(state) {
      state.selected = null
    },
  },
})

export const flagsActions = flagsSlice.actions
export default flagsSlice.reducer
