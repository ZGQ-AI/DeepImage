export interface DecodedJwtPayload {
  loginType?: string
  loginId?: number | string
  deviceType?: string
  eff?: number // JWT 过期时间（毫秒级时间戳）
  rnStr?: string
  username?: string
  email?: string
  avatarUrl?: string
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
 * 检查 token 是否已过期
 * @param token - JWT token
 * @param bufferSeconds - 提前多少秒判定为过期（默认 60 秒，即提前 1 分钟）
 * @returns true 表示已过期或即将过期，false 表示仍然有效
 */
export function isTokenExpired(token: string | null, bufferSeconds: number = 60): boolean {
  if (!token) return true

  const payload = decodeJwt(token)
  if (!payload || !payload.eff) return true

  // eff 是毫秒级时间戳，直接使用
  const expirationTime = payload.eff
  const currentTime = Date.now()
  const bufferTime = bufferSeconds * 1000

  // 如果当前时间 + 缓冲时间 >= 过期时间，则认为已过期
  return currentTime + bufferTime >= expirationTime
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
