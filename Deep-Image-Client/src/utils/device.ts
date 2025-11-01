/**
 * Device-related utility functions
 */

/**
 * Parse User-Agent and return device name
 */
export function parseDeviceName(userAgent: string): string {
  if (!userAgent) return '未知设备'

  if (/Windows NT 10/.test(userAgent)) return 'Windows 10/11 电脑'
  if (/Windows/.test(userAgent)) return 'Windows 电脑'
  if (/Macintosh/.test(userAgent)) return 'Mac 电脑'
  if (/iPhone/.test(userAgent)) return 'iPhone'
  if (/iPad/.test(userAgent)) return 'iPad'
  if (/Android/.test(userAgent)) return 'Android 设备'
  if (/Linux/.test(userAgent)) return 'Linux 设备'

  return '未知设备'
}

/**
 * Check if it's a desktop device
 */
export function isDesktop(userAgent: string): boolean {
  if (!userAgent) return false
  return /Windows|Macintosh|Linux/.test(userAgent) && !/Mobile|Android|iPhone|iPad/.test(userAgent)
}

/**
 * Parse browser name
 */
export function parseBrowserName(userAgent: string): string {
  if (!userAgent) return '未知浏览器'

  if (/Edg/.test(userAgent)) return 'Microsoft Edge'
  if (/Chrome/.test(userAgent)) return 'Google Chrome'
  if (/Safari/.test(userAgent) && !/Chrome/.test(userAgent)) return 'Safari'
  if (/Firefox/.test(userAgent)) return 'Firefox'
  if (/Opera|OPR/.test(userAgent)) return 'Opera'

  return '未知浏览器'
}
