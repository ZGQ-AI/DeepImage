/**
 * Time-related utility functions
 */

/**
 * Format time as relative time (e.g., "5 minutes ago") or absolute time
 */
export function formatTime(dateStr: string): string {
  if (!dateStr) return '-'

  const date = new Date(dateStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()

  // Calculate time difference
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  // Relative time
  if (seconds < 60) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`

  // Absolute time
  return formatDateTime(dateStr)
}

/**
 * Format as full date and time
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
 * Format as date (without time)
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
