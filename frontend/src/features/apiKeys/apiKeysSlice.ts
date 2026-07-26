/**
 * SDK API keys list. `createdRawKey` holds the one-time secret after create
 * until the user dismisses it (clearCreatedKey).
 */
import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { ApiKey, CreateApiKeyResponse, Environment } from '@/types/api'

export interface ApiKeysState {
  items: ApiKey[]
  loading: boolean
  saving: boolean
  error: string | null
  /** One-time raw secret from create — show then clear. */
  createdRawKey: string | null
  createdWarning: string | null
}

const initialState: ApiKeysState = {
  items: [],
  loading: false,
  saving: false,
  error: null,
  createdRawKey: null,
  createdWarning: null,
}

const apiKeysSlice = createSlice({
  name: 'apiKeys',
  initialState,
  reducers: {
    fetchApiKeysRequest(state) {
      state.loading = true
      state.error = null
    },
    fetchApiKeysSuccess(state, action: PayloadAction<ApiKey[]>) {
      state.loading = false
      state.items = action.payload
    },
    fetchApiKeysFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    createApiKeyRequest(
      state,
      _action: PayloadAction<{ name: string; environmentScope?: Environment | null }>,
    ) {
      state.saving = true
      state.error = null
      state.createdRawKey = null
      state.createdWarning = null
    },
    createApiKeySuccess(state, action: PayloadAction<CreateApiKeyResponse>) {
      state.saving = false
      state.items = [action.payload.apiKey, ...state.items]
      state.createdRawKey = action.payload.rawKey
      state.createdWarning = action.payload.warning
    },
    createApiKeyFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    revokeApiKeyRequest(state, _action: PayloadAction<string>) {
      state.saving = true
      state.error = null
    },
    revokeApiKeySuccess(state, action: PayloadAction<ApiKey>) {
      state.saving = false
      state.items = state.items.map((k) => (k.id === action.payload.id ? action.payload : k))
    },
    revokeApiKeyFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    clearCreatedRawKey(state) {
      state.createdRawKey = null
      state.createdWarning = null
    },
    clearApiKeysError(state) {
      state.error = null
    },
  },
})

export const apiKeysActions = apiKeysSlice.actions
export default apiKeysSlice.reducer
