/**
 * 用户Store - 管理用户信息和会话
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { message } from 'ant-design-vue'
import {
  getUserProfile,
  updateUserProfile,
  getUserSessions,
  deleteSession as deleteSessionApi,
  deleteOtherSessions as deleteOtherSessionsApi,
} from '../api/user'
import type {
  UserProfileResponse,
  UpdateUserProfileRequest,
  SessionItemResponse,
} from '../types/user'

export const useUserStore = defineStore('user', () => {
  // 用户信息
  const profile = ref<UserProfileResponse | null>(null)

  // 会话列表
  const sessions = ref<SessionItemResponse[]>([])

  // 加载状态
  const profileLoading = ref(false)
  const sessionsLoading = ref(false)

  // 计算属性：登录用户信息（用于GlobalHeader等组件）
  const loginUser = computed(() => ({
    id: profile.value?.id || null,
    userName: profile.value?.username || 'unLogin',
  }))

  /**
   * 获取用户信息
   */
  async function fetchProfile() {
    profileLoading.value = true
    try {
      const { data } = await getUserProfile()
      if (data.code === 200) {
        profile.value = data.data
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // Don't show error message for authentication errors or cancelled requests
      // User will be redirected to login page automatically
      const isAuthError =
        error?.__CANCEL__ || // Request cancelled by interceptor
        error?.message?.includes('No authentication token') ||
        error?.message?.includes('Token refresh failed') ||
        error?.response?.status === 401

      if (!isAuthError) {
        message.error(error?.message || '获取用户信息失败')
      }
      throw error
    } finally {
      profileLoading.value = false
    }
  }

  /**
   * 更新用户信息
   */
  async function updateProfile(request: UpdateUserProfileRequest) {
    profileLoading.value = true
    try {
      const { data } = await updateUserProfile(request)
      if (data.code === 200) {
        profile.value = data.data
        message.success('更新成功')
        return true
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '更新失败')
      throw error
    } finally {
      profileLoading.value = false
    }
  }

  /**
   * 获取会话列表
   */
  async function fetchSessions() {
    sessionsLoading.value = true
    try {
      const { data } = await getUserSessions()
      if (data.code === 200) {
        sessions.value = data.data.sessions
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // Don't show error message for authentication errors or cancelled requests
      const isAuthError =
        error?.__CANCEL__ ||
        error?.message?.includes('No authentication token') ||
        error?.message?.includes('Token refresh failed') ||
        error?.response?.status === 401

      if (!isAuthError) {
        message.error(error?.message || '获取会话列表失败')
      }
      throw error
    } finally {
      sessionsLoading.value = false
    }
  }

  /**
   * 删除指定会话
   */
  async function deleteSession(sessionId: number) {
    try {
      const { data } = await deleteSessionApi(sessionId)
      if (data.code === 200) {
        message.success('设备已移除')
        // 刷新会话列表
        await fetchSessions()
        return true
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '移除失败')
      throw error
    }
  }

  /**
   * 删除其他所有会话
   */
  async function deleteOtherSessions() {
    try {
      const { data } = await deleteOtherSessionsApi()
      if (data.code === 200) {
        const count = data.data.deletedCount
        message.success(`已移除 ${count} 个设备`)
        // 刷新会话列表
        await fetchSessions()
        return true
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      message.error(error?.message || '移除失败')
      throw error
    }
  }

  /**
   * 清空用户状态（用于退出登录）
   */
  function clearUserState() {
    profile.value = null
    sessions.value = []
  }

  return {
    profile,
    sessions,
    profileLoading,
    sessionsLoading,
    loginUser,
    fetchProfile,
    updateProfile,
    fetchSessions,
    deleteSession,
    deleteOtherSessions,
    clearUserState,
  }
})
