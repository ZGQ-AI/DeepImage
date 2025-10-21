import axios from 'axios'
import { getAccessToken } from './utils/token'
import { useAuthStore } from './stores/useAuthStore'
import router from './router'

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 50000,
  withCredentials: true,
})

axiosInstance.interceptors.request.use(
  function (config) {
    const token = getAccessToken()
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
  { synchronous: true, runWhen: () => true },
)

let isRefreshing = false
let isRedirectingToLogin = false // 防止多次跳转登录页
let pendingQueue: Array<(token?: string) => void> = []

// 重置登录跳转标志（供路由守卫使用）
export function resetLoginRedirectFlag() {
  isRedirectingToLogin = false
}

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

      // 如果已经在跳转登录页，直接拒绝请求（静默失败）
      if (isRedirectingToLogin) {
        return Promise.reject(new Error('Redirecting to login'))
      }

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
        // 调用 refresh 方法
        await auth.refresh()

        // 刷新成功，执行队列中的所有请求
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

        // 设置跳转标志，防止多次跳转
        isRedirectingToLogin = true

        await auth.logout()

        try {
          await router.replace({ name: 'auth', query: { redirect: router.currentRoute.value.fullPath } })
        } catch {
          // 忽略路由跳转错误
        }

        // 静默失败，不抛出错误（避免多个错误提示）
        return Promise.reject(new Error('Authentication required'))
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default axiosInstance
