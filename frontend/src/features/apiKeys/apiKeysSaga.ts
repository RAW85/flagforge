/** List / create / revoke SDK API keys. */
import { call, put, takeLatest } from 'redux-saga/effects'
import { apiKeysApi } from '@/api/apiKeysApi'
import { getErrorMessage } from '@/api/client'
import type { ApiKey, CreateApiKeyResponse } from '@/types/api'
import { apiKeysActions } from './apiKeysSlice'

function* fetchWorker() {
  try {
    const items: ApiKey[] = yield call(apiKeysApi.list)
    yield put(apiKeysActions.fetchApiKeysSuccess(items))
  } catch (error) {
    yield put(apiKeysActions.fetchApiKeysFailure(getErrorMessage(error, 'Failed to load API keys')))
  }
}

function* createWorker(action: ReturnType<typeof apiKeysActions.createApiKeyRequest>) {
  try {
    const result: CreateApiKeyResponse = yield call(apiKeysApi.create, action.payload)
    yield put(apiKeysActions.createApiKeySuccess(result))
  } catch (error) {
    yield put(apiKeysActions.createApiKeyFailure(getErrorMessage(error, 'Failed to create API key')))
  }
}

function* revokeWorker(action: ReturnType<typeof apiKeysActions.revokeApiKeyRequest>) {
  try {
    const key: ApiKey = yield call(apiKeysApi.revoke, action.payload)
    yield put(apiKeysActions.revokeApiKeySuccess(key))
  } catch (error) {
    yield put(apiKeysActions.revokeApiKeyFailure(getErrorMessage(error, 'Failed to revoke API key')))
  }
}

export function* apiKeysSaga() {
  yield takeLatest(apiKeysActions.fetchApiKeysRequest.type, fetchWorker)
  yield takeLatest(apiKeysActions.createApiKeyRequest.type, createWorker)
  yield takeLatest(apiKeysActions.revokeApiKeyRequest.type, revokeWorker)
}
