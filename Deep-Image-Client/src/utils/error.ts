import { message } from 'ant-design-vue'

/**
 * 处理 API 错误，自动过滤认证错误
 * @param error 错误对象
 * @param defaultMessage 默认错误消息
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function handleApiError(error: any, defaultMessage: string): void {
  // 如果是认证错误，不显示提示（已经在拦截器中处理跳转）
  if (error?.name === 'AuthenticationError') {
    return
  }
  
  // 显示错误消息
  const errorMessage = error?.message || defaultMessage
  message.error(errorMessage)
}

/**
 * 检查是否是认证错误
 * @param error 错误对象
 */
// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function isAuthenticationError(error: any): boolean {
  return error?.name === 'AuthenticationError'
}

