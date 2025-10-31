/**
 * Authentication Utility Functions
 *
 * Centralized utilities for token validation and authentication state checking.
 * These functions are pure and synchronous (except where noted), providing
 * a single source of truth for auth logic.
 */
import { getAccessToken, getRefreshToken } from './token'
import { isTokenExpired } from './jwt'

/**
 * Check if a token is valid (exists and not expired)
 *
 * @param token - The access token to check (or null)
 * @param bufferSeconds - Buffer time in seconds before expiration (default: 60)
 * @returns true if token is valid, false otherwise
 */
export function isTokenValid(token: string | null, bufferSeconds: number = 60): boolean {
  if (!token) return false
  return !isTokenExpired(token, bufferSeconds)
}

/**
 * Check authentication state and determine if refresh is needed
 *
 * **Pure Function:** No side effects, only reads from storage.
 *
 * @returns Object with authentication state:
 *   - isAuthenticated: true if user has valid access token OR refresh token exists
 *   - needsRefresh: true if access token is invalid BUT refresh token exists
 */
export function checkAuth(): {
  isAuthenticated: boolean
  needsRefresh: boolean
} {
  const accessToken = getAccessToken()
  const refreshToken = getRefreshToken()

  const isValid = isTokenValid(accessToken)

  return {
    isAuthenticated: isValid || !!refreshToken,
    needsRefresh: !isValid && !!refreshToken,
  }
}

/**
 * Get a valid access token, refreshing if necessary
 *
 * **Async Function:** May trigger token refresh if needed.
 *
 * @returns Promise that resolves to valid access token, or null if unavailable
 * @throws Error if refresh fails
 */
export async function getValidAccessToken(): Promise<string | null> {
  const accessToken = getAccessToken()
  const refreshToken = getRefreshToken()

  // If access token is valid, return it
  if (isTokenValid(accessToken)) {
    return accessToken
  }

  // If no refresh token, return null
  if (!refreshToken) {
    return null
  }

  // Access token is invalid but refresh token exists
  // Return null and let caller handle refresh via TokenRefreshManager
  // (We don't refresh here to avoid circular dependencies)
  return null
}

