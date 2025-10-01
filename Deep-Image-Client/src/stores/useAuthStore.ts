import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, refreshToken as refreshApi, logout as logoutApi } from '../api/auth'
import type { LoginRequest, TokenPairResponse } from '../types/auth'
import {
  getAccessToken,
  getRefreshToken,
  clearTokens,
  setTokens,
  getTokenStorageMode,
} from '../utils/token'
import { decodeJwt } from '../utils/jwt'
import { useUserStore } from './useUserStore'

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(getAccessToken())
  const refreshToken = ref<string | null>(getRefreshToken())
  const expiresIn = ref<number | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)

  function applyTokenPair(tokenPair: TokenPairResponse, mode?: 'local' | 'session') {
    // 如果没有指定 mode，则使用当前的存储模式
    const storageMode = mode || getTokenStorageMode()

    accessToken.value = tokenPair.accessToken
    refreshToken.value = tokenPair.refreshToken
    expiresIn.value = tokenPair.expiresIn ?? null
    setTokens(tokenPair.accessToken, tokenPair.refreshToken, storageMode)

    // 解析 JWT，获取用户信息
    const payload = decodeJwt(tokenPair.accessToken)
    if (payload) {
      const userStore = useUserStore()
      const id = (payload.loginId ?? payload.sub) as any
      const username = (payload.username ?? payload.USERNAME ?? payload.name) as any
      const email = payload.email as any
      const avatarUrl = payload.avatarUrl as any
      
      // 更新用户基本信息（用于头部显示）
      if (id && username) {
        userStore.profile = {
          id,
          username,
          email: email || '',
          avatarUrl: avatarUrl || '',
          verified: false,
          createdAt: '',
          updatedAt: '',
        }
      }
    }
  }

  async function login(payload: LoginRequest & { remember?: boolean }) {
    const { data } = await loginApi(payload)
    if (data.code !== 200) throw new Error(data.message)
    applyTokenPair(data.data, payload.remember ? 'local' : 'session')
    return true
  }

  async function refresh() {
    // 1. 优先从内存/状态中获取 refresh token，如果没有则从存储中读取
    const rt = refreshToken.value || getRefreshToken()

    // 2. 如果没有 refresh token，说明用户未登录或已过期
    if (!rt) {
      throw new Error('No refresh token available')
    }

    try {
      // 3. 调用后端刷新接口
      const { data } = await refreshApi({ refreshToken: rt })

      // 4. 检查响应状态码
      if (data.code !== 200) {
        throw new Error(data.message || 'Failed to refresh token')
      }

      // 5. 应用新的 token pair
      // 注意：刷新时保持原有的存储模式（local 或 session）
      applyTokenPair(data.data)

      return true
    } catch (error: any) {
      // 6. 刷新失败，清理所有 token
      clearTokens()
      accessToken.value = null
      refreshToken.value = null
      
      // 清空用户状态
      const userStore = useUserStore()
      userStore.clearUserState()

      // 7. 抛出错误，让调用方处理（如跳转登录页）
      throw new Error(error.response?.data?.message || error.message || 'Token refresh failed')
    }
  }
  async function logout() {
    try {
      await logoutApi()
    } catch (e) {
      /* ignore */
    }
    accessToken.value = null
    refreshToken.value = null
    expiresIn.value = null
    clearTokens()
    
    // 清空用户状态
    const userStore = useUserStore()
    userStore.clearUserState()
  }

  async function bootstrap() {
    // 如果已有 access token，直接返回
    if (accessToken.value) return true

    // 尝试从存储中获取 refresh token
    const rt = getRefreshToken()
    if (rt) {
      try {
        await refresh()
        return true
        } catch (err) {
          console.warn('Bootstrap refresh failed:', err)
          // 静默失败，不影响用户体验
        }
    }

    return false
  }

  return {
    accessToken,
    refreshToken,
    expiresIn,
    isAuthenticated,
    applyTokenPair,
    login,
    refresh,
    logout,
    bootstrap,
  }
})
