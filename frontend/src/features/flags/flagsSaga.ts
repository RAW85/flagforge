/** Flag list/detail/create/toggle/delete → flagsApi. */
import { call, put, select, takeLatest } from 'redux-saga/effects'
import { flagsApi } from '@/api/flagsApi'
import { getErrorMessage } from '@/api/client'
import type { CursorPage, FeatureFlag } from '@/types/api'
import { flagsActions, type FlagsState } from './flagsSlice'

function* fetchFlagsWorker(action: ReturnType<typeof flagsActions.fetchFlagsRequest>) {
  try {
    const state: FlagsState = yield select((s: { flags: FlagsState }) => s.flags)
    // append=true loads next page using current cursor
    const append = Boolean(action.payload?.append)
    const page: CursorPage<FeatureFlag> = yield call(flagsApi.list, {
      cursor: append ? state.nextCursor ?? undefined : undefined,
      limit: 20,
      environment: action.payload?.environment ?? state.filters.environment,
      status: action.payload?.status ?? state.filters.status,
    })
    yield put(flagsActions.fetchFlagsSuccess({ page, append }))
  } catch (error) {
    yield put(flagsActions.fetchFlagsFailure(getErrorMessage(error, 'Failed to load flags')))
  }
}

function* fetchFlagWorker(action: ReturnType<typeof flagsActions.fetchFlagRequest>) {
  try {
    const flag: FeatureFlag = yield call(flagsApi.getById, action.payload)
    yield put(flagsActions.fetchFlagSuccess(flag))
  } catch (error) {
    yield put(flagsActions.fetchFlagFailure(getErrorMessage(error, 'Failed to load flag')))
  }
}

function* createFlagWorker(action: ReturnType<typeof flagsActions.createFlagRequest>) {
  try {
    const flag: FeatureFlag = yield call(flagsApi.create, action.payload)
    yield put(flagsActions.createFlagSuccess(flag))
  } catch (error) {
    yield put(flagsActions.createFlagFailure(getErrorMessage(error, 'Failed to create flag')))
  }
}

function* toggleFlagWorker(action: ReturnType<typeof flagsActions.toggleFlagRequest>) {
  try {
    const flag: FeatureFlag = yield call(flagsApi.toggle, action.payload.id, action.payload.enabled)
    yield put(flagsActions.toggleFlagSuccess(flag))
  } catch (error) {
    yield put(flagsActions.toggleFlagFailure(getErrorMessage(error, 'Failed to toggle flag')))
  }
}

function* deleteFlagWorker(action: ReturnType<typeof flagsActions.deleteFlagRequest>) {
  try {
    yield call(flagsApi.remove, action.payload)
    yield put(flagsActions.deleteFlagSuccess(action.payload))
  } catch (error) {
    yield put(flagsActions.deleteFlagFailure(getErrorMessage(error, 'Failed to delete flag')))
  }
}

export function* flagsSaga() {
  yield takeLatest(flagsActions.fetchFlagsRequest.type, fetchFlagsWorker)
  yield takeLatest(flagsActions.fetchFlagRequest.type, fetchFlagWorker)
  yield takeLatest(flagsActions.createFlagRequest.type, createFlagWorker)
  yield takeLatest(flagsActions.toggleFlagRequest.type, toggleFlagWorker)
  yield takeLatest(flagsActions.deleteFlagRequest.type, deleteFlagWorker)
}
