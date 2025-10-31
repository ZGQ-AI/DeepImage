/**
 * Token Refresh Manager
 *
 * Centralized token refresh manager that prevents race conditions
 * and duplicate refresh attempts across the application.
 *
 * **Singleton Pattern:** Only one instance exists, ensuring all
 * refresh operations are coordinated.
 */
import { refreshToken as refreshApi } from '../api/auth'
import { getRefreshToken, clearTokens, setTokens, getTokenStorageMode } from './token'
import { useAuthStore } from '../stores/useAuthStore'
import { useUserStore } from '../stores/useUserStore'
import { clearTokenExpirationCache } from './jwt'
import type { TokenPairResponse } from '../types/auth'

class TokenRefreshManager {
  private isRefreshing = false
  private refreshPromise: Promise<string | null> | null = null

  /**
   * Refresh the access token using the refresh token
   *
   * **Concurrency Protection:** If a refresh is already in progress,
   * returns the existing Promise instead of starting a new refresh.
   *
   * @returns Promise that resolves to the new access token, or null if refresh failed
   * @throws Error if refresh fails
   */
  async refresh(): Promise<string | null> {
    // If already refreshing, return the existing Promise
    if (this.isRefreshing && this.refreshPromise) {
      return this.refreshPromise
    }

    // Start new refresh operation
    this.isRefreshing = true
    this.refreshPromise = this.doRefresh()

    try {
      const token = await this.refreshPromise
      return token
    } finally {
      this.isRefreshing = false
      this.refreshPromise = null
    }
  }

  /**
   * Check if a refresh operation is currently in progress
   *
   * @returns true if refresh is in progress, false otherwise
   */
  isRefreshingInProgress(): boolean {
    return this.isRefreshing
  }

  /**
   * Clear refresh state (used during logout)
   */
  clear(): void {
    this.isRefreshing = false
    this.refreshPromise = null
  }

  /**
   * Internal method that performs the actual refresh operation
   */
  private async doRefresh(): Promise<string | null> {
    // Read refresh token from storage
    const refreshToken = getRefreshToken()

    // If no refresh token, user is not authenticated
    if (!refreshToken) {
      throw new Error('No refresh token available')
    }

    try {
      // Call backend refresh API
      const { data } = await refreshApi({ refreshToken })

      // Check response
      if (data.code !== 200) {
        throw new Error(data.message || 'Failed to refresh token')
      }

      // Apply new token pair (preserve current storage mode)
      const tokenPair: TokenPairResponse = data.data
      const storageMode = getTokenStorageMode()
      setTokens(tokenPair.accessToken, tokenPair.refreshToken, storageMode)

      // Clear token expiration cache since we have a new token
      clearTokenExpirationCache()

      // Note: expiresIn is managed by useAuthStore.applyTokenPair()
      // We don't update it here to avoid circular dependencies
      // The store will update it when applyTokenPair is called elsewhere

      return tokenPair.accessToken
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
    } catch (error: any) {
      // Refresh failed - clear all tokens
      clearTokens()

      // Clear token expiration cache
      clearTokenExpirationCache()

      // Clear user state
      const userStore = useUserStore()
      userStore.clearUserState()

      // Propagate error to caller
      throw new Error(
        error.response?.data?.message || error.message || 'Token refresh failed',
      )
    }
  }
}

// Export singleton instance
export const tokenRefreshManager = new TokenRefreshManager()

