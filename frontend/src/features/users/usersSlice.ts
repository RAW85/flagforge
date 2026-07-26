/** Admin user list, role changes, and enable/disable. */
import { createSlice, type PayloadAction } from '@reduxjs/toolkit'
import type { PlatformUser, UserRole } from '@/types/api'

export interface UsersState {
  items: PlatformUser[]
  loading: boolean
  saving: boolean
  error: string | null
}

const initialState: UsersState = {
  items: [],
  loading: false,
  saving: false,
  error: null,
}

const usersSlice = createSlice({
  name: 'users',
  initialState,
  reducers: {
    fetchUsersRequest(state) {
      state.loading = true
      state.error = null
    },
    fetchUsersSuccess(state, action: PayloadAction<PlatformUser[]>) {
      state.loading = false
      state.items = action.payload
    },
    fetchUsersFailure(state, action: PayloadAction<string>) {
      state.loading = false
      state.error = action.payload
    },
    updateUserRoleRequest(
      state,
      _action: PayloadAction<{ id: string; role: UserRole }>,
    ) {
      state.saving = true
      state.error = null
    },
    updateUserRoleSuccess(state, action: PayloadAction<PlatformUser>) {
      state.saving = false
      state.items = state.items.map((u) => (u.id === action.payload.id ? action.payload : u))
    },
    updateUserRoleFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    setUserEnabledRequest(
      state,
      _action: PayloadAction<{ id: string; enabled: boolean }>,
    ) {
      state.saving = true
      state.error = null
    },
    setUserEnabledSuccess(state, action: PayloadAction<PlatformUser>) {
      state.saving = false
      state.items = state.items.map((u) => (u.id === action.payload.id ? action.payload : u))
    },
    setUserEnabledFailure(state, action: PayloadAction<string>) {
      state.saving = false
      state.error = action.payload
    },
    clearUsersError(state) {
      state.error = null
    },
  },
})

export const usersActions = usersSlice.actions
export default usersSlice.reducer
