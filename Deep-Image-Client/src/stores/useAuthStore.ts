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
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // 6. 刷新失败，清理所有 token
      clearTokens()
      accessToken.value = null
      refreshToken.value = null

      // 7. 抛出错误，让调用方处理（如跳转登录页）
      throw new Error(error.response?.data?.message || error.message || 'Token refresh failed')
    }
  }
  async function logout() {
    try {
      await logoutApi()
    } catch {
      /* ignore */
    }
    accessToken.value = null
    refreshToken.value = null
    expiresIn.value = null
    clearTokens()
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
