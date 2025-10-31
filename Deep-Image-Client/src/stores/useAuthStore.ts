/**
 * Authentication Store
 *
 * **Architecture: Stateless Token Management**
 * This store does NOT cache tokens in reactive refs. All token access is delegated
 * to storage utilities, ensuring single source of truth and preventing sync issues.
 */
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi } from '../api/auth'
import type { LoginRequest, TokenPairResponse } from '../types/auth'
import {
  getAccessToken,
  getRefreshToken,
  clearTokens,
  setTokens,
  getTokenStorageMode,
} from '../utils/token'
import { decodeJwt, clearTokenExpirationCache } from '../utils/jwt'
import { useUserStore } from './useUserStore'
import { tokenRefreshManager } from '../utils/tokenRefreshManager'
import { checkAuth } from '../utils/authUtils'

export const useAuthStore = defineStore('auth', () => {
  // Optional: Track token expiration time for UI display only
  // This does NOT affect authentication logic
  const expiresIn = ref<number | null>(null)

  // Login modal state
  const loginModalVisible = ref(false)
  const loginModalRedirectPath = ref<string>('')

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

    // Clear token expiration cache since we have a new token
    clearTokenExpirationCache()

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
   * **Delegated to TokenRefreshManager:** Uses centralized refresh manager
   * to prevent race conditions and duplicate refresh attempts.
   *
   * @returns Promise<true> on success
   * @throws Error if refresh fails
   */
  async function refresh() {
    try {
      await tokenRefreshManager.refresh()
      return true
    } catch (error) {
      // Error is already handled by TokenRefreshManager (tokens cleared, user state cleared)
      // Just propagate the error
      throw error
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

    // Clear token expiration cache
    clearTokenExpirationCache()

    // Clear UI state
    expiresIn.value = null

    // Clear user state
    const userStore = useUserStore()
    userStore.clearUserState()
  }

  /**
   * Bootstrap authentication on app initialization
   *
   * **Uses authUtils:** Checks authentication state using centralized utilities.
   *
   * @returns Promise<true> if session restored, false otherwise
   */
  async function bootstrap() {
    const authState = checkAuth()

    // If already authenticated, return true
    if (authState.isAuthenticated && !authState.needsRefresh) {
      return true
    }

    // If needs refresh, try to refresh token
    if (authState.needsRefresh) {
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

  /**
   * Show login modal
   *
   * @param redirectPath - Optional path to redirect to after successful login
   */
  function showLoginModal(redirectPath?: string) {
    loginModalVisible.value = true
    loginModalRedirectPath.value = redirectPath || ''
  }

  /**
   * Hide login modal
   */
  function hideLoginModal() {
    loginModalVisible.value = false
    loginModalRedirectPath.value = ''
  }

  return {
    // State (UI only, not used for auth logic)
    expiresIn,
    loginModalVisible,
    loginModalRedirectPath,

    // Computed
    isAuthenticated,

    // Actions
    applyTokenPair,
    login,
    refresh,
    logout,
    bootstrap,
    showLoginModal,
    hideLoginModal,
  }
})
