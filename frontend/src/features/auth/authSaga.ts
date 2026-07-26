/**
 * Auth side effects: login/register persist JWT; bootstrap calls /me;
 * logout clears token storage.
 */
import { call, put, takeLatest } from 'redux-saga/effects'
import { authApi } from '@/api/authApi'
import { getErrorMessage, tokenStorage } from '@/api/client'
import type { AuthResponse, MeResponse } from '@/types/api'
import { authActions } from './authSlice'

function* loginWorker(action: ReturnType<typeof authActions.loginRequest>) {
  try {
    const data: AuthResponse = yield call(authApi.login, action.payload)
    tokenStorage.set(data.accessToken)
    yield put(authActions.authSuccess(data))
  } catch (error) {
    yield put(authActions.authFailure(getErrorMessage(error, 'Login failed')))
  }
}

function* registerWorker(action: ReturnType<typeof authActions.registerRequest>) {
  try {
    const data: AuthResponse = yield call(authApi.register, action.payload)
    tokenStorage.set(data.accessToken)
    yield put(authActions.authSuccess(data))
  } catch (error) {
    yield put(authActions.authFailure(getErrorMessage(error, 'Registration failed')))
  }
}

/** Restore session from stored token via GET /auth/me. */
function* bootstrapWorker() {
  const token = tokenStorage.get()
  if (!token) {
    yield put(authActions.bootstrapFailure())
    return
  }
  try {
    const me: MeResponse = yield call(authApi.me)
    yield put(authActions.bootstrapSuccess(me))
  } catch {
    tokenStorage.clear()
    yield put(authActions.bootstrapFailure())
  }
}

function* logoutWorker() {
  tokenStorage.clear()
}

export function* authSaga() {
  yield takeLatest(authActions.loginRequest.type, loginWorker)
  yield takeLatest(authActions.registerRequest.type, registerWorker)
  yield takeLatest(authActions.bootstrapRequest.type, bootstrapWorker)
  yield takeLatest(authActions.logout.type, logoutWorker)
}
