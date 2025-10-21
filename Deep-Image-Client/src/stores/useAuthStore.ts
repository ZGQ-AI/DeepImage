/**
 * Authentication Store
 *
 * **Architecture: Stateless Token Management**
 * This store does NOT cache tokens in reactive refs. All token access is delegated
 * to storage utilities, ensuring single source of truth and preventing sync issues.
 */
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
  // Optional: Track token expiration time for UI display only
  // This does NOT affect authentication logic
  const expiresIn = ref<number | null>(null)

  /**
   * Computed property that checks authentication status
   *
   * **Storage-Based:** Always reads current token from storage.
   * No cached state - reflects real-time storage state.
   */
  const isAuthenticated = computed(() => !!getAccessToken())

  /**
   * Apply a token pair received from the backend
   *
   * @param tokenPair - The access and refresh tokens from login/refresh response
   * @param mode - Storage mode: 'local' for "remember me", 'session' for current session only
   */
  function applyTokenPair(tokenPair: TokenPairResponse, mode?: 'local' | 'session') {
    // Determine storage mode (use provided or fallback to current mode)
    const storageMode = mode || getTokenStorageMode()

    // Store tokens in browser storage (no memory cache)
    setTokens(tokenPair.accessToken, tokenPair.refreshToken, storageMode)

    // Update expiration time (UI display only)
    expiresIn.value = tokenPair.expiresIn ?? null

    // Extract basic user info from JWT for immediate UI display
    const payload = decodeJwt(tokenPair.accessToken)
    if (payload) {
      const userStore = useUserStore()
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const id = (payload.loginId ?? payload.sub) as any
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const username = (payload.username ?? payload.USERNAME ?? payload.name) as any
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const email = payload.email as any
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      const avatarUrl = payload.avatarUrl as any

      // Update user basic info (for header display)
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

  /**
   * Perform user login
   *
   * @param payload - Login credentials with optional "remember" flag
   * @returns Promise<true> on success
   */
  async function login(payload: LoginRequest & { remember?: boolean }) {
    const { data } = await loginApi(payload)
    if (data.code !== 200) throw new Error(data.message)

    // Store tokens based on "remember me" preference
    applyTokenPair(data.data, payload.remember ? 'local' : 'session')
    return true
  }

  /**
   * Refresh the access token using the refresh token
   *
   * **Storage-Based:** Reads refresh token from storage on each call.
   */
  async function refresh() {
    // Read refresh token from storage
    const rt = getRefreshToken()

    // If no refresh token, user is not authenticated
    if (!rt) {
      throw new Error('No refresh token available')
    }

    try {
      // Call backend refresh API
      const { data } = await refreshApi({ refreshToken: rt })

      // Check response
      if (data.code !== 200) {
        throw new Error(data.message || 'Failed to refresh token')
      }

      // Apply new token pair (preserve current storage mode)
      applyTokenPair(data.data)

      return true
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // Refresh failed - clear all tokens
      clearTokens()

      // Clear user state
      const userStore = useUserStore()
      userStore.clearUserState()

      // Propagate error to caller (will trigger redirect to login)
      throw new Error(error.response?.data?.message || error.message || 'Token refresh failed')
    }
  }

  /**
   * Logout the current user
   */
  async function logout() {
    try {
      // Call backend logout API (best effort)
      await logoutApi()
    } catch {
      // Ignore logout API errors
    }

    // Clear all tokens from storage
    clearTokens()

    // Clear UI state
    expiresIn.value = null

    // Clear user state
    const userStore = useUserStore()
    userStore.clearUserState()
  }

  /**
   * Bootstrap authentication on app initialization
   *
   * **Storage-Based:** Checks storage for refresh token and attempts to restore session.
   *
   * @returns Promise<true> if session restored, false otherwise
   */
  async function bootstrap() {
    // Check if we have an access token in storage
    if (getAccessToken()) {
      return true
    }

    // Try to restore session using refresh token
    const rt = getRefreshToken()
    if (rt) {
      try {
        await refresh()
        return true
      } catch (err) {
        console.warn('Bootstrap refresh failed:', err)
        // Silent failure - user will see login page
      }
    }

    return false
  }

  return {
    // State (UI only, not used for auth logic)
    expiresIn,

    // Computed
    isAuthenticated,

    // Actions
    applyTokenPair,
    login,
    refresh,
    logout,
    bootstrap,
  }
})
