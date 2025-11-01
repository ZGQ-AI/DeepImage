/**
 * User-related type definitions
 */

/**
 * User profile response
 */
export interface UserProfileResponse {
  id: number
  username: string
  email: string
  phone?: string
  avatarUrl?: string
  verified: boolean
  createdAt: string
  updatedAt: string
}

/**
 * Update user profile request
 */
export interface UpdateUserProfileRequest {
  username?: string
  phone?: string
  avatarUrl?: string
}

/**
 * Session item response
 */
export interface SessionItemResponse {
  id: number
  deviceInfo?: string
  ipAddress: string
  userAgent: string
  active: number
  lastRefreshAt: string
  createdAt: string
  isCurrent: boolean
}

/**
 * Session list response
 */
export interface SessionListResponse {
  sessions: SessionItemResponse[]
  total: number
  page: number
  pageSize: number
}

/**
 * Delete other sessions response
 */
export interface DeleteOtherSessionsResponse {
  deletedCount: number
}
