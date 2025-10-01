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
