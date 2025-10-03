import axios, { type InternalAxiosRequestConfig } from 'axios'
import { getAccessToken } from './utils/token'
import { useAuthStore } from './stores/useAuthStore'
import router from './router'
import { isTokenExpiringSoon } from './utils/jwt'

// 扩展 Axios 配置类型，添加自定义字段
interface CustomAxiosRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean
  _skipRefresh?: boolean
}

const axiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080',
  timeout: 50000,
  withCredentials: true,
})

let isRefreshing = false
let pendingQueue: Array<(token?: string) => void> = []

axiosInstance.interceptors.request.use(
  async function (config: CustomAxiosRequestConfig) {
    const token = getAccessToken()
    
    if (token) {
      // 检查 token 是否即将过期（提前 5 分钟）
      if (isTokenExpiringSoon(token, 5) && !isRefreshing && !config._skipRefresh) {
        console.log('Token 即将过期，主动刷新...')
        isRefreshing = true
        
        try {
          const auth = useAuthStore()
          await auth.refresh()
          
          // 刷新成功，使用新 token
          const newToken = getAccessToken()
          if (newToken) {
            config.headers = config.headers || {}
            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            ;(config.headers as any).Authorization = `Bearer ${newToken}`
          }
        } catch (error) {
          console.error('主动刷新 token 失败:', error)
          // 刷新失败，仍然使用旧 token 发请求，让后端返回 401
        } finally {
          isRefreshing = false
        }
      } else {
        config.headers = config.headers || {}
        // eslint-disable-next-line @typescript-eslint/no-explicit-any
        ;(config.headers as any).Authorization = `Bearer ${token}`
      }
    }
    
    return config
  },
  function (error) {
    return Promise.reject(error)
  },
  { synchronous: false, runWhen: () => true },
)

axiosInstance.interceptors.response.use(
  function onFulfilled(response) {
    return response
  },
  async function onRejected(error) {
    const { response, config } = error || {}
    const typedConfig = config as CustomAxiosRequestConfig

    // 只处理 401 且未重试过的请求
    if (response && response.status === 401 && !typedConfig._retry) {
      typedConfig._retry = true
      const auth = useAuthStore()

      // 如果正在刷新，将请求加入队列
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push((token) => {
            if (token) {
              typedConfig.headers.Authorization = `Bearer ${token}`
              axiosInstance(typedConfig).then(resolve).catch(reject)
            } else {
              // 刷新失败，静默拒绝（不显示错误提示）
              const authError = new Error('AUTHENTICATION_REQUIRED')
              authError.name = 'AuthenticationError'
              reject(authError)
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
          typedConfig.headers.Authorization = `Bearer ${newToken}`
          return axiosInstance(typedConfig)
        }
      } catch {
        // 刷新失败（无 refresh token 或 refresh token 过期）
        pendingQueue.forEach((fn) => fn(undefined))
        pendingQueue = []

        // 静默退出登录
        await auth.logout()

        console.log('Token 刷新失败，跳转到登录页')
        
        // 跳转到登录页，并保存当前路径用于登录后重定向
        try {
          const currentPath = router.currentRoute.value.fullPath
          await router.replace({ 
            name: 'auth', 
            query: { redirect: currentPath } 
          })
        } catch {
          // 忽略路由跳转错误
        }

        // 返回自定义错误，标记为认证失败（不显示给用户）
        const authError = new Error('AUTHENTICATION_REQUIRED')
        authError.name = 'AuthenticationError'
        return Promise.reject(authError)
      } finally {
        isRefreshing = false
      }
    }

    return Promise.reject(error)
  },
)

export default axiosInstance
