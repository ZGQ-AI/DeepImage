/**
 * 用户相关API接口
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
 * 获取当前用户信息
 */
export function getUserProfile() {
  return request.get<ApiResponse<UserProfileResponse>>('/api/user/profile')
}

/**
 * 更新用户信息
 */
export function updateUserProfile(data: UpdateUserProfileRequest) {
  return request.put<ApiResponse<UserProfileResponse>>('/api/user/profile', data)
}

/**
 * 获取用户会话列表
 */
export function getUserSessions() {
  return request.get<ApiResponse<SessionListResponse>>('/api/user/sessions')
}

/**
 * 删除指定会话
 */
export function deleteSession(sessionId: number) {
  return request.delete<ApiResponse<boolean>>(`/api/user/sessions/${sessionId}`)
}

/**
 * 删除其他所有会话
 */
export function deleteOtherSessions() {
  return request.delete<ApiResponse<DeleteOtherSessionsResponse>>('/api/user/sessions/others')
}
