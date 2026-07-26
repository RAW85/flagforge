/** ADMIN users API: list, update role, set enabled. */
import { call, put, takeLatest } from 'redux-saga/effects'
import { usersApi } from '@/api/usersApi'
import { getErrorMessage } from '@/api/client'
import type { PlatformUser } from '@/types/api'
import { usersActions } from './usersSlice'

function* fetchWorker() {
  try {
    const items: PlatformUser[] = yield call(usersApi.list)
    yield put(usersActions.fetchUsersSuccess(items))
  } catch (error) {
    yield put(usersActions.fetchUsersFailure(getErrorMessage(error, 'Failed to load users')))
  }
}

function* updateRoleWorker(action: ReturnType<typeof usersActions.updateUserRoleRequest>) {
  try {
    const user: PlatformUser = yield call(
      usersApi.updateRole,
      action.payload.id,
      action.payload.role,
    )
    yield put(usersActions.updateUserRoleSuccess(user))
  } catch (error) {
    yield put(usersActions.updateUserRoleFailure(getErrorMessage(error, 'Failed to update role')))
  }
}

function* setEnabledWorker(action: ReturnType<typeof usersActions.setUserEnabledRequest>) {
  try {
    const user: PlatformUser = yield call(
      usersApi.setEnabled,
      action.payload.id,
      action.payload.enabled,
    )
    yield put(usersActions.setUserEnabledSuccess(user))
  } catch (error) {
    yield put(usersActions.setUserEnabledFailure(getErrorMessage(error, 'Failed to update status')))
  }
}

export function* usersSaga() {
  yield takeLatest(usersActions.fetchUsersRequest.type, fetchWorker)
  yield takeLatest(usersActions.updateUserRoleRequest.type, updateRoleWorker)
  yield takeLatest(usersActions.setUserEnabledRequest.type, setEnabledWorker)
}
