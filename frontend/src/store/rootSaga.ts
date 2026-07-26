/** Forks all feature sagas. Add new feature sagas here. */
import { all, fork } from 'redux-saga/effects'
import { authSaga } from '@/features/auth/authSaga'
import { flagsSaga } from '@/features/flags/flagsSaga'
import { evaluationSaga } from '@/features/evaluation/evaluationSaga'
import { rolloutsSaga } from '@/features/rollouts/rolloutsSaga'
import { apiKeysSaga } from '@/features/apiKeys/apiKeysSaga'
import { usersSaga } from '@/features/users/usersSaga'

export function* rootSaga() {
  yield all([
    fork(authSaga),
    fork(flagsSaga),
    fork(evaluationSaga),
    fork(rolloutsSaga),
    fork(apiKeysSaga),
    fork(usersSaga),
  ])
}
