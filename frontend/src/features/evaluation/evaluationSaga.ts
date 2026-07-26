/** POST /evaluate for the dashboard Evaluate page. */
import { call, put, takeLatest } from 'redux-saga/effects'
import { evaluateApi } from '@/api/evaluateApi'
import { getErrorMessage } from '@/api/client'
import type { EvaluateResponse } from '@/types/api'
import { evaluationActions } from './evaluationSlice'

function* evaluateWorker(action: ReturnType<typeof evaluationActions.evaluateRequest>) {
  try {
    const result: EvaluateResponse = yield call(evaluateApi.evaluate, action.payload)
    yield put(evaluationActions.evaluateSuccess(result))
  } catch (error) {
    yield put(evaluationActions.evaluateFailure(getErrorMessage(error, 'Evaluation failed')))
  }
}

export function* evaluationSaga() {
  yield takeLatest(evaluationActions.evaluateRequest.type, evaluateWorker)
}
