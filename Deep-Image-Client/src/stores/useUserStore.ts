/**
 * 用户Store - 管理用户信息和会话
 */
import { ref, computed, watch } from 'vue'
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
import { useAuthStore } from './useAuthStore'

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
      message.error(error?.message || '获取用户信息失败')
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
      message.error(error?.message || '获取会话列表失败')
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

  // 响应式同步：监听认证状态变化
  const authStore = useAuthStore()
  const isAutoFetching = ref(false) // 防止重复加载的标志

  watch(
    () => authStore.isAuthenticated,
    async (isAuth, wasAuth) => {
      // 从未认证 → 已认证：自动加载用户信息
      if (isAuth && !wasAuth) {
        // 防止重复加载
        if (isAutoFetching.value || profile.value) return
        
        isAutoFetching.value = true
        try {
          await fetchProfile()
        } catch (error) {
          console.warn('Auto-fetch profile failed:', error)
          // 静默失败，不影响认证状态
        } finally {
          isAutoFetching.value = false
        }
      }
      // 从已认证 → 未认证：自动清空用户信息
      else if (!isAuth && wasAuth) {
        clearUserState()
      }
    },
    { immediate: false } // 不立即执行，避免初始化时触发
  )

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

