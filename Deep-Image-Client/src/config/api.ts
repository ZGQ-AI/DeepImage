/**
 * API Configuration
 *
 * Centralized API endpoint definitions and utilities
 */

/**
 * Public API endpoints that don't require authentication
 * These endpoints can be accessed without a valid access token
 */
export const PUBLIC_ENDPOINTS = [
  '/auth/login',
  '/auth/register',
  '/auth/google',
  '/auth/forgot-password',
  '/auth/reset-password',
  '/auth/refresh',
  '/auth/callback',
] as const

/**
 * Check if a URL is a public endpoint that doesn't require authentication
 *
 * @param url - The request URL to check
 * @returns true if the endpoint is public, false otherwise
 *
 * @example
 * ```typescript
 * isPublicEndpoint('/auth/login') // true
 * isPublicEndpoint('/user/profile') // false
 * ```
 */
export function isPublicEndpoint(url: string): boolean {
  return PUBLIC_ENDPOINTS.some((endpoint) => url.includes(endpoint))
}

/**
 * API base URL
 * Can be overridden by environment variable VITE_API_BASE_URL
 */
export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * Request timeout in milliseconds
 */
export const REQUEST_TIMEOUT = 50000
