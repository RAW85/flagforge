import { describe, it, expect, beforeEach } from 'vitest'
import { getErrorMessage, tokenStorage } from './client'
import type { AxiosError } from 'axios'
import type { ApiError } from '@/types/api'

describe('tokenStorage', () => {
  beforeEach(() => {
    localStorage.clear()
  })

  it('stores and clears token', () => {
    expect(tokenStorage.get()).toBeNull()
    tokenStorage.set('abc')
    expect(tokenStorage.get()).toBe('abc')
    tokenStorage.clear()
    expect(tokenStorage.get()).toBeNull()
  })
})

describe('getErrorMessage', () => {
  it('reads message from API error body', () => {
    const error = {
      response: { data: { message: 'Validation failed', status: 400 } },
      message: 'Request failed',
    } as AxiosError<ApiError>
    expect(getErrorMessage(error)).toBe('Validation failed')
  })

  it('falls back to axios message', () => {
    const error = { message: 'Network Error' } as AxiosError<ApiError>
    expect(getErrorMessage(error)).toBe('Network Error')
  })

  it('uses default fallback', () => {
    expect(getErrorMessage({})).toBe('Something went wrong')
  })
})
