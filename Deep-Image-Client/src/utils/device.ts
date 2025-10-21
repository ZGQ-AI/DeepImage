/**
 * 设备相关工具函数
 */

/**
 * 解析User-Agent，返回设备名称
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
 * 判断是否为桌面设备
 */
export function isDesktop(userAgent: string): boolean {
  if (!userAgent) return false
  return /Windows|Macintosh|Linux/.test(userAgent) && !/Mobile|Android|iPhone|iPad/.test(userAgent)
}

/**
 * 解析浏览器名称
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
