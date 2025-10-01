/**
 * 用户相关类型定义
 */

/**
 * 用户信息响应
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
 * 更新用户信息请求
 */
export interface UpdateUserProfileRequest {
  username?: string
  phone?: string
  avatarUrl?: string
}

/**
 * 会话项响应
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
 * 会话列表响应
 */
export interface SessionListResponse {
  sessions: SessionItemResponse[]
  total: number
  page: number
  pageSize: number
}

/**
 * 删除其他会话响应
 */
export interface DeleteOtherSessionsResponse {
  deletedCount: number
}

