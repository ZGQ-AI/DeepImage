import axios from 'axios'
import { getAccessToken } from './utils/token'
import { useAuthStore } from './stores/useAuthStore'
import router from './router'
import { API_BASE_URL, REQUEST_TIMEOUT, isPublicEndpoint } from './config/api'

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  withCredentials: true,
})

axiosInstance.interceptors.request.use(
  function (config) {
    // Get fresh token from storage on every request (no caching)
    const token = getAccessToken()
    if (token) {
      config.headers = config.headers || {}
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      ;(config.headers as any).Authorization = `Bearer ${token}`
      return config
    }

    // No token available - check if this is a public endpoint
    const url = config.url || ''
    if (isPublicEndpoint(url)) {
      // Public endpoints don't need token
      return config
    }

    // Private endpoint without token - redirect to login
    console.warn('[Request Interceptor] No token found for private endpoint, redirecting to login')

    // Schedule redirect (async to avoid blocking)
    setTimeout(() => {
      const currentPath = window.location.pathname
      if (currentPath !== '/auth') {
        router.replace({
          name: 'auth',
          query: { redirect: currentPath },
        })
      }
    }, 0)

    // Cancel the request by throwing an error in the interceptor
    // This prevents axios from trying to send the request
    const cancelError = new Error('No authentication token available')
    // Mark it as a cancellation to distinguish from network errors
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    ;(cancelError as any).__CANCEL__ = true
    return Promise.reject(cancelError)
  },
  function (error) {
    return Promise.reject(error)
  },
)

let isRefreshing = false
let pendingQueue: Array<(token?: string) => void> = []

axiosInstance.interceptors.response.use(
  function onFulfilled(response) {
    return response
  },
  async function onRejected(error) {
    const { response, config } = error || {}

    // 只处理 401 且未重试过的请求
    if (response && response.status === 401 && !config._retry) {
      config._retry = true
      const auth = useAuthStore()

      // 如果正在刷新，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push((token) => {
            if (token) {
              config.headers.Authorization = `Bearer ${token}`
              axiosInstance(config).then(resolve).catch(reject)
            } else {
              reject(new Error('Token refresh failed'))
            }
          })
        })
      }

      // 开始刷新流程
      isRefreshing = true

      try {
        // Attempt to refresh tokens (reads refresh token from storage)
        await auth.refresh()

        // Refresh succeeded - get new token from storage and retry queued requests
        const newToken = getAccessToken()
        pendingQueue.forEach((fn) => fn(newToken ?? undefined))
        pendingQueue = []

        // 重试当前请求
        if (newToken) {
          config.headers.Authorization = `Bearer ${newToken}`
          return axiosInstance(config)
        }
      } catch {
        // 刷新失败，清空队列并跳转登录页
        pendingQueue.forEach((fn) => fn(undefined))
        pendingQueue = []

        await auth.logout()

        try {
          router.replace({ name: 'auth', query: { redirect: router.currentRoute.value.fullPath } })
        } catch {
          // 忽略路由跳转错误
        }

        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default axiosInstance
