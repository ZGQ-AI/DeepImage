/**
 * User Store - Manages user information and sessions
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
  // User information
  const profile = ref<UserProfileResponse | null>(null)

  // Session list
  const sessions = ref<SessionItemResponse[]>([])

  // Loading states
  const profileLoading = ref(false)
  const sessionsLoading = ref(false)

  // Computed property: logged-in user information (for GlobalHeader and other components)
  const loginUser = computed(() => ({
    id: profile.value?.id || null,
    userName: profile.value?.username || 'unLogin',
  }))

  /**
   * Fetch user profile
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
        error?.response?.status === 401 // 401 indicates authentication failure

      if (!isAuthError) {
        message.error(error?.message || '获取用户信息失败')
      }
      // Don't throw error if it's an auth error - just fail silently
      // The login modal will be shown by interceptors
      if (isAuthError) {
        return // Silent failure
      }
      throw error
    } finally {
      profileLoading.value = false
    }
  }

  /**
   * Update user profile
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
   * Fetch session list
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
        error?.response?.status === 401 // 401 indicates authentication failure

      if (!isAuthError) {
        message.error(error?.message || '获取会话列表失败')
      }
      // Don't throw error if it's an auth error - just fail silently
      // The login modal will be shown by interceptors
      if (isAuthError) {
        return // Silent failure
      }
      throw error
    } finally {
      sessionsLoading.value = false
    }
  }

  /**
   * Delete a specific session
   */
  async function deleteSession(sessionId: number) {
    try {
      const { data } = await deleteSessionApi(sessionId)
      if (data.code === 200) {
        message.success('设备已移除')
        // Refresh session list
        await fetchSessions()
        return true
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // Don't show error message for authentication errors
      // User will be redirected to login page automatically
      const isAuthError =
        error?.__CANCEL__ ||
        error?.response?.status === 401 // 401 indicates authentication failure

      if (!isAuthError) {
        message.error(error?.message || '移除失败')
      }
      // Don't throw error if it's an auth error - just fail silently
      // The login modal will be shown by interceptors
      if (isAuthError) {
        return false // Silent failure
      }
      throw error
    }
  }

  /**
   * Delete all other sessions
   */
  async function deleteOtherSessions() {
    try {
      const { data } = await deleteOtherSessionsApi()
      if (data.code === 200) {
        const count = data.data.deletedCount
        message.success(`已移除 ${count} 个设备`)
        // Refresh session list
        await fetchSessions()
        return true
      } else {
        throw new Error(data.message)
      }
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // Don't show error message for authentication errors
      // User will be redirected to login page automatically
      const isAuthError =
        error?.__CANCEL__ ||
        error?.response?.status === 401 // 401 indicates authentication failure

      if (!isAuthError) {
        message.error(error?.message || '移除失败')
      }
      // Don't throw error if it's an auth error - just fail silently
      // The login modal will be shown by interceptors
      if (isAuthError) {
        return false // Silent failure
      }
      throw error
    }
  }

  /**
   * Clear user state (used for logout)
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
