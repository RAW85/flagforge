/**
 * Rollout start/advance/rollback. On success, re-fetches the related flag
 * so percentage/enabled in the UI match the backend.
 */
import { call, put, takeLatest } from 'redux-saga/effects'
import { rolloutsApi } from '@/api/rolloutsApi'
import { getErrorMessage } from '@/api/client'
import type { RolloutSaga } from '@/types/api'
import { rolloutsActions } from './rolloutsSlice'
import { flagsActions } from '@/features/flags/flagsSlice'

function* fetchRolloutsWorker(action: ReturnType<typeof rolloutsActions.fetchRolloutsRequest>) {
  try {
    const items: RolloutSaga[] = yield call(rolloutsApi.listByFlag, action.payload)
    yield put(rolloutsActions.fetchRolloutsSuccess(items))
  } catch (error) {
    yield put(rolloutsActions.fetchRolloutsFailure(getErrorMessage(error, 'Failed to load rollouts')))
  }
}

function* startRolloutWorker(action: ReturnType<typeof rolloutsActions.startRolloutRequest>) {
  try {
    const saga: RolloutSaga = yield call(rolloutsApi.start, action.payload)
    yield put(rolloutsActions.startRolloutSuccess(saga))
    yield put(flagsActions.fetchFlagRequest(action.payload.flagId))
  } catch (error) {
    yield put(rolloutsActions.startRolloutFailure(getErrorMessage(error, 'Failed to start rollout')))
  }
}

function* advanceRolloutWorker(action: ReturnType<typeof rolloutsActions.advanceRolloutRequest>) {
  try {
    const saga: RolloutSaga = yield call(rolloutsApi.advance, action.payload)
    yield put(rolloutsActions.advanceRolloutSuccess(saga))
    yield put(flagsActions.fetchFlagRequest(saga.flagId))
  } catch (error) {
    yield put(rolloutsActions.advanceRolloutFailure(getErrorMessage(error, 'Failed to advance rollout')))
  }
}

function* rollbackRolloutWorker(action: ReturnType<typeof rolloutsActions.rollbackRolloutRequest>) {
  try {
    const saga: RolloutSaga = yield call(
      rolloutsApi.rollback,
      action.payload.id,
      action.payload.reason,
    )
    yield put(rolloutsActions.rollbackRolloutSuccess(saga))
    yield put(flagsActions.fetchFlagRequest(saga.flagId))
  } catch (error) {
    yield put(rolloutsActions.rollbackRolloutFailure(getErrorMessage(error, 'Failed to rollback')))
  }
}

export function* rolloutsSaga() {
  yield takeLatest(rolloutsActions.fetchRolloutsRequest.type, fetchRolloutsWorker)
  yield takeLatest(rolloutsActions.startRolloutRequest.type, startRolloutWorker)
  yield takeLatest(rolloutsActions.advanceRolloutRequest.type, advanceRolloutWorker)
  yield takeLatest(rolloutsActions.rollbackRolloutRequest.type, rollbackRolloutWorker)
}
