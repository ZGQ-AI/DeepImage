import axios from 'axios'
import { getAccessToken, getRefreshToken } from './utils/token'
import { useAuthStore } from './stores/useAuthStore'
import router from './router'
import { API_BASE_URL, REQUEST_TIMEOUT, isPublicEndpoint } from './config/api'
import { isTokenExpired } from './utils/jwt'

const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: REQUEST_TIMEOUT,
  withCredentials: true,
})

// 防止请求拦截器中的并发刷新
let isRefreshingInInterceptor = false
let refreshPromiseInInterceptor: Promise<boolean> | null = null

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

    // 主动检查 token 是否过期（提前 60 秒判定为过期）
    if (token && isTokenExpired(token, 60)) {
      console.log('[Request Interceptor] Access token expired or expiring soon, attempting refresh...')

      // 检查是否有 refresh token
      const refreshToken = getRefreshToken()
      if (refreshToken) {
        try {
          // 如果已经有刷新操作在进行，等待它完成
          if (isRefreshingInInterceptor && refreshPromiseInInterceptor) {
            console.log('[Request Interceptor] Another refresh is in progress, waiting...')
            await refreshPromiseInInterceptor
            token = getAccessToken()
            console.log('[Request Interceptor] Using refreshed token from another request')
          } else {
            // 开始新的刷新操作
            isRefreshingInInterceptor = true
            const authStore = useAuthStore()
            
            // 创建刷新 Promise 供其他请求等待
            refreshPromiseInInterceptor = authStore.refresh().finally(() => {
              isRefreshingInInterceptor = false
              refreshPromiseInInterceptor = null
            })
            
            await refreshPromiseInInterceptor
            
            // 刷新成功，重新获取新的 access token
            token = getAccessToken()
            console.log('[Request Interceptor] Token refreshed successfully')
          }
        } catch (error) {
          console.error('[Request Interceptor] Token refresh failed:', error)
          // 刷新失败，清除 token 并显示登录框
          const authStore = useAuthStore()
          await authStore.logout()

          setTimeout(() => {
            const currentPath = window.location.pathname
            authStore.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)
          }, 0)

          const cancelError = new Error('Token refresh failed, authentication required')
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          ;(cancelError as any).__CANCEL__ = true
          return Promise.reject(cancelError)
        }
      } else {
        // 没有 refresh token，显示登录框
        console.warn('[Request Interceptor] No refresh token available')
        
        setTimeout(() => {
          const authStore = useAuthStore()
          const currentPath = window.location.pathname
          authStore.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)
        }, 0)

        const cancelError = new Error('No refresh token available')
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ;(cancelError as any).__CANCEL__ = true
        return Promise.reject(cancelError)
      }
    }

    // 添加 token 到请求头
    if (token) {
      config.headers = config.headers || {}
      // eslint-disable-next-line @typescript-eslint/no-explicit-any
      ;(config.headers as any).Authorization = `Bearer ${token}`
      return config
    }

    // 没有 token，显示登录框
    console.warn('[Request Interceptor] No token found for private endpoint, showing login modal')

    setTimeout(() => {
      const authStore = useAuthStore()
      const currentPath = window.location.pathname
      authStore.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)
    }, 0)

    const cancelError = new Error('No authentication token available')
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
      } catch (refreshError) {
        // 刷新失败，清空队列并弹出登录框
        console.error('[Response Interceptor] Token refresh failed:', refreshError)
        
        pendingQueue.forEach((fn) => fn(undefined))
        pendingQueue = []

        await auth.logout()

        // 弹出登录框而不是跳转
        const currentPath = router.currentRoute.value.fullPath
        auth.showLoginModal(currentPath !== '/auth' ? currentPath : undefined)

        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default axiosInstance
