import { describe, it, expect } from 'vitest'
import flagsReducer, { flagsActions } from './flagsSlice'
import type { FeatureFlag } from '@/types/api'

const sampleFlag: FeatureFlag = {
  id: 'f1',
  key: 'dark-mode',
  name: 'Dark Mode',
  description: null,
  enabled: false,
  status: 'DRAFT',
  flagType: 'BOOLEAN',
  environment: 'DEVELOPMENT',
  defaultValue: 'false',
  percentage: null,
  rulesJson: null,
  createdBy: 'u1',
  createdAt: '2026-01-01T00:00:00Z',
  updatedAt: '2026-01-01T00:00:00Z',
  version: 0,
}

describe('flagsSlice', () => {
  it('stores list page on fetchFlagsSuccess', () => {
    const state = flagsReducer(
      undefined,
      flagsActions.fetchFlagsSuccess({
        page: { items: [sampleFlag], nextCursor: 'c1', hasMore: true },
      }),
    )
    expect(state.items).toHaveLength(1)
    expect(state.items[0].key).toBe('dark-mode')
    expect(state.hasMore).toBe(true)
    expect(state.nextCursor).toBe('c1')
    expect(state.loading).toBe(false)
  })

  it('appends when fetchFlagsSuccess append=true', () => {
    const first = flagsReducer(
      undefined,
      flagsActions.fetchFlagsSuccess({
        page: { items: [sampleFlag], nextCursor: null, hasMore: false },
      }),
    )
    const secondFlag = { ...sampleFlag, id: 'f2', key: 'beta' }
    const state = flagsReducer(
      first,
      flagsActions.fetchFlagsSuccess({
        page: { items: [secondFlag], nextCursor: null, hasMore: false },
        append: true,
      }),
    )
    expect(state.items).toHaveLength(2)
    expect(state.items.map((f) => f.key)).toEqual(['dark-mode', 'beta'])
  })

  it('updates flag on toggleFlagSuccess', () => {
    const listed = flagsReducer(
      undefined,
      flagsActions.fetchFlagsSuccess({
        page: { items: [sampleFlag], nextCursor: null, hasMore: false },
      }),
    )
    const state = flagsReducer(
      listed,
      flagsActions.toggleFlagSuccess({
        ...sampleFlag,
        enabled: true,
        status: 'ACTIVE',
      }),
    )
    expect(state.items[0].enabled).toBe(true)
    expect(state.items[0].status).toBe('ACTIVE')
    expect(state.selected?.enabled).toBe(true)
  })

  it('removes flag on deleteFlagSuccess', () => {
    const listed = flagsReducer(
      undefined,
      flagsActions.fetchFlagsSuccess({
        page: { items: [sampleFlag], nextCursor: null, hasMore: false },
      }),
    )
    const state = flagsReducer(listed, flagsActions.deleteFlagSuccess('f1'))
    expect(state.items).toHaveLength(0)
  })

  it('sets error on fetchFlagsFailure', () => {
    const state = flagsReducer(
      undefined,
      flagsActions.fetchFlagsFailure('boom'),
    )
    expect(state.loading).toBe(false)
    expect(state.error).toBe('boom')
  })
})
