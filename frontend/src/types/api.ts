/**
 * Shared API contracts (mirror backend DTOs).
 * Keep in sync with Spring presentation DTOs when fields change.
 */

export type UserRole = 'ADMIN' | 'EDITOR' | 'VIEWER'

export type FlagStatus = 'DRAFT' | 'ACTIVE' | 'ARCHIVED'
export type FlagType = 'BOOLEAN' | 'PERCENTAGE' | 'MULTIVARIATE'
export type Environment = 'DEVELOPMENT' | 'STAGING' | 'PRODUCTION'
export type SagaStatus = 'RUNNING' | 'COMPLETED' | 'ROLLED_BACK' | 'FAILED'
/** Why the engine returned this evaluation result. */
export type EvaluationReason =
  | 'FLAG_DISABLED'
  | 'BOOLEAN_ENABLED'
  | 'PERCENTAGE_IN'
  | 'PERCENTAGE_OUT'
  | 'VARIANT_MATCH'
  | 'DEFAULT_VALUE'

export interface AuthResponse {
  userId: string
  username: string
  email: string
  role: UserRole
  accessToken: string
  tokenType: string
  expiresInMs: number
}

export interface MeResponse {
  id: string
  username: string
  email: string
  role: UserRole
}

export interface FeatureFlag {
  id: string
  key: string
  name: string
  description: string | null
  enabled: boolean
  status: FlagStatus
  flagType: FlagType
  environment: Environment
  defaultValue: string
  percentage: number | null
  rulesJson: string | null
  createdBy: string
  createdAt: string
  updatedAt: string
  version: number
}

export interface CursorPage<T> {
  items: T[]
  nextCursor: string | null
  hasMore: boolean
}

export interface CreateFeatureFlagRequest {
  key: string
  name: string
  description?: string
  flagType: FlagType
  environment: Environment
  defaultValue?: string
  percentage?: number
  rulesJson?: string
}

export interface EvaluateRequest {
  flagKey: string
  environment: Environment
  /** Sticky bucketing key (user/account id). */
  subjectId: string
  contextJson?: string
  /** When true, backend persists an evaluation history row. */
  record?: boolean
}

export interface EvaluateResponse {
  flagId: string
  flagKey: string
  environment: Environment
  flagType: FlagType
  subjectId: string
  /** Resolved payload ("true"/"false", variant name, etc.). */
  value: string
  /** Use this for on/off gates (in-rollout / feature on). */
  enabled: boolean
  reason: EvaluationReason
  /** Sticky 0–99 bucket for the subject. */
  bucket: number | null
}

export interface RolloutSaga {
  id: string
  flagId: string
  flagKey: string
  environment: Environment
  status: SagaStatus
  currentStepIndex: number
  currentPercentage: number
  steps: number[]
  hasNextStep: boolean
  startedBy: string
  createdAt: string
  updatedAt: string
  completedAt: string | null
  failureReason: string | null
}

export interface ApiError {
  timestamp?: string
  status: number
  error?: string
  message: string
  path?: string
  fieldErrors?: { field: string; message: string }[]
}

export interface ApiKey {
  id: string
  name: string
  keyPrefix: string
  displayKey: string
  ownerId: string
  environmentScope: Environment | null
  active: boolean
  lastUsedAt: string | null
  revokedAt: string | null
  createdAt: string
}

/** Create response — `rawKey` is shown once; never stored again on the client. */
export interface CreateApiKeyResponse {
  apiKey: ApiKey
  rawKey: string
  warning: string
}

export interface PlatformUser {
  id: string
  username: string
  email: string
  role: UserRole
  enabled: boolean
  createdAt: string
  updatedAt: string
}
