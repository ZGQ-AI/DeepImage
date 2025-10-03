export interface DecodedJwtPayload {
  loginId?: number | string
  username?: string
  email?: string
  eff?: number  // 过期时间（毫秒时间戳）
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key: string]: any
}

export function decodeJwt(token: string): DecodedJwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = parts[1]
    const json = JSON.parse(decodeBase64Url(payload))
    return json as DecodedJwtPayload
  } catch {
    return null
  }
}

/**
 * 检查 token 是否即将过期
 * @param token JWT token
 * @param advanceMinutes 提前多少分钟判定为即将过期，默认 5 分钟
 * @returns true 表示即将过期或已过期
 */
export function isTokenExpiringSoon(token: string, advanceMinutes: number = 5): boolean {
  const payload = decodeJwt(token)
  if (!payload || !payload.eff) {
    return true // 无法解析或无过期时间，视为过期
  }

  const now = Date.now()
  const expiryTime = payload.eff
  const advanceMs = advanceMinutes * 60 * 1000

  // 如果当前时间 + 提前量 >= 过期时间，则认为即将过期
  return now + advanceMs >= expiryTime
}

/**
 * 检查 token 是否已过期
 * @param token JWT token
 * @returns true 表示已过期
 */
export function isTokenExpired(token: string): boolean {
  const payload = decodeJwt(token)
  if (!payload || !payload.eff) {
    return true
  }

  return Date.now() >= payload.eff
}

function decodeBase64Url(input: string): string {
  // base64url -> base64
  input = input.replace(/-/g, '+').replace(/_/g, '/')
  const pad = input.length % 4
  if (pad) input = input + '='.repeat(4 - pad)
  const atobImpl =
    typeof window !== 'undefined' && window.atob
      ? window.atob
      : typeof atob !== 'undefined'
        ? atob
        : null
  if (!atobImpl) return ''
  try {
    return decodeURIComponent(escape(atobImpl(input)))
  } catch {
    return atobImpl(input)
  }
}
