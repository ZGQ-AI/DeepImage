import axios from 'axios'
import { getAccessToken } from './utils/token'
import { useAuthStore } from './stores/useAuthStore'
import router from './router'
import { API_BASE_URL, REQUEST_TIMEOUT, isPublicEndpoint } from './config/api'
import { isTokenValid, checkAuth } from './utils/authUtils'
import { tokenRefreshManager } from './utils/tokenRefreshManager'

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  withCredentials: true,
})

axiosInstance.interceptors.request.use(
  async function (config) {
    // Check if this is a public endpoint
    const url = config.url || ''
    if (isPublicEndpoint(url)) {
      // Public endpoints don't need token
      return config
    }

    // Get current access token
    let token = getAccessToken()

    // Check authentication state
    const authState = checkAuth()

    // If token is invalid or expired, try to refresh before sending request
    if (!isTokenValid(token)) {
      // If refresh token exists, refresh the access token first
      if (authState.needsRefresh) {
        try {
          // Wait for refresh to complete (TokenRefreshManager handles concurrency)
          await tokenRefreshManager.refresh()
          // Get the new token from storage
          token = getAccessToken()
          
          if (!token) {
            throw new Error('Token refresh succeeded but no token found')
          }
        } catch (refreshError) {
          // Refresh failed - clear tokens and show login modal
          console.error('[Request Interceptor] Token refresh failed:', refreshError)
          
          setTimeout(() => {
            const authStore = useAuthStore()
            const currentPath = window.location.pathname
            authStore.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)
          }, 0)

          const cancelError = new Error('Token refresh failed, authentication required')
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          ;(cancelError as any).__CANCEL__ = true
          return Promise.reject(cancelError)
        }
      } else if (!authState.isAuthenticated) {
        // No refresh token available, show login modal and cancel request
        console.warn('[Request Interceptor] No valid token and no refresh token, showing login modal')

        setTimeout(() => {
          const authStore = useAuthStore()
          const currentPath = window.location.pathname
          authStore.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)
        }, 0)

        const cancelError = new Error('No authentication token available')
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ;(cancelError as any).__CANCEL__ = true
        return Promise.reject(cancelError)
      }
    }

    // Add token to headers (either valid token or newly refreshed token)
    if (token) {
      config.headers = config.headers || {}
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      ;(config.headers as any).Authorization = `Bearer ${token}`
    }

    return config
  },
  function (error) {
    return Promise.reject(error)
  },
)

// Request queue for concurrent 401 responses
let pendingQueue: Array<(token?: string) => void> = []

axiosInstance.interceptors.response.use(
  function onFulfilled(response) {
    return response
  },
  async function onRejected(error) {
    const { response, config } = error || {}

    // Only handle 401 errors that haven't been retried
    if (response && response.status === 401 && !config._retry) {
      config._retry = true

      // If refresh is already in progress, queue this request
      if (tokenRefreshManager.isRefreshingInProgress()) {
        return new Promise((resolve, reject) => {
          pendingQueue.push((token) => {
            if (token) {
              // Ensure headers object exists
              config.headers = config.headers || {}
              config.headers.Authorization = `Bearer ${token}`
              // Keep _retry flag to prevent infinite retry loops
              axiosInstance(config).then(resolve).catch(reject)
            } else {
              // Mark as cancelled to prevent error messages
              const cancelledError = new Error('Token refresh failed')
              // eslint-disable-next-line @typescript-eslint/no-explicit-any
              ;(cancelledError as any).__CANCEL__ = true
              reject(cancelledError)
            }
          })
        })
      }

      // Start refresh operation using TokenRefreshManager
      try {
        // TokenRefreshManager handles concurrency protection
        const newToken = await tokenRefreshManager.refresh()

        // Refresh succeeded - get new token from storage (in case it was updated)
        const refreshedToken = getAccessToken()
        
        if (refreshedToken) {
          // Process queued requests first
          pendingQueue.forEach((fn) => fn(refreshedToken))
          pendingQueue = []

          // Retry current request with new token
          config.headers = config.headers || {}
          config.headers.Authorization = `Bearer ${refreshedToken}`
          // Keep _retry flag to prevent infinite retry loops
          // If retry fails again, let error propagate
          return axiosInstance(config)
        } else {
          throw new Error('Token refresh succeeded but no token found in storage')
        }
      } catch (refreshError) {
        // Refresh failed - TokenRefreshManager already cleared tokens and user state
        console.error('[Response Interceptor] Token refresh failed:', refreshError)

        // Fail all queued requests silently
        pendingQueue.forEach((fn) => fn(undefined))
        pendingQueue = []

        const authStore = useAuthStore()

        // Show login modal silently (no error message to user)
        const currentPath = router.currentRoute.value.fullPath
        authStore.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)

        // Mark error as cancelled to prevent error messages from being shown
        const cancelledError = new Error('Authentication required')
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ;(cancelledError as any).__CANCEL__ = true
        return Promise.reject(cancelledError)
      }
    }

    return Promise.reject(error)
  },
)

export default axiosInstance
