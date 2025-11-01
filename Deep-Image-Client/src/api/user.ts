/**
 * User-related API interfaces
 */
import request from '../request'
import type { ApiResponse } from '../types/api'
import type {
  UserProfileResponse,
  UpdateUserProfileRequest,
  SessionListResponse,
  DeleteOtherSessionsResponse,
} from '../types/user'

/**
 * Get current user profile
 */
export function getUserProfile() {
  return request.get<ApiResponse<UserProfileResponse>>('/api/user/profile')
}

/**
 * Update user profile
 */
export function updateUserProfile(data: UpdateUserProfileRequest) {
  return request.put<ApiResponse<UserProfileResponse>>('/api/user/profile', data)
}

/**
 * Get user session list
 */
export function getUserSessions() {
  return request.get<ApiResponse<SessionListResponse>>('/api/user/sessions')
}

/**
 * Delete a specific session
 */
export function deleteSession(sessionId: number) {
  return request.delete<ApiResponse<boolean>>(`/api/user/sessions/${sessionId}`)
}

/**
 * Delete all other sessions
 */
export function deleteOtherSessions() {
  return request.delete<ApiResponse<DeleteOtherSessionsResponse>>('/api/user/sessions/others')
}
