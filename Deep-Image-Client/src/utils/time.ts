/**
 * 时间相关工具函数
 */

/**
 * 格式化时间为相对时间（如"5分钟前"）或绝对时间
 */
export function formatTime(dateStr: string): string {
  if (!dateStr) return '-'

  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // 计算时间差
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  // 相对时间
  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  // 绝对时间
  return formatDateTime(dateStr)
}

/**
 * 格式化为完整日期时间
 */
export function formatDateTime(dateStr: string): string {
  if (!dateStr) return '-'

  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
  })
}

/**
 * 格式化为日期（不含时间）
 */
export function formatDate(dateStr: string): string {
  if (!dateStr) return '-'

  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  })
}

