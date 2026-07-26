/**
 * Single evaluation result + short client-side history (last 10).
 * Does not call the SDK path — uses JWT /evaluate via evaluateApi.
 */
import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { EvaluateRequest, EvaluateResponse } from '@/types/api'

export interface EvaluationState {
  result: EvaluateResponse | null
  loading: boolean
  error: string | null
  /** Newest first; capped at 10 in evaluateSuccess. */
  history: EvaluateResponse[]
}

const initialState: EvaluationState = {
  result: null,
  loading: false,
  error: null,
  history: [],
}

const evaluationSlice = createSlice({
  name: 'evaluation',
  initialState,
  reducers: {
    evaluateRequest(state, _action: PayloadAction<EvaluateRequest>) {
      state.loading = true
      state.error = null
    },
    evaluateSuccess(state, action: PayloadAction<EvaluateResponse>) {
      state.loading = false
      state.result = action.payload
      state.history = [action.payload, ...state.history].slice(0, 10)
    },
    evaluateFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    clearEvaluation(state) {
      state.result = null
      state.error = null
    },
  },
})

export const evaluationActions = evaluationSlice.actions
export default evaluationSlice.reducer
