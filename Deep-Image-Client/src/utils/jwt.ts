export interface DecodedJwtPayload {
  loginType?: string
  loginId?: number | string
  deviceType?: string
  eff?: number // JWT expiration time (millisecond timestamp)
  rnStr?: string
  username?: string
  email?: string
  avatarUrl?: string
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  [key: string]: any
}

// Cache for token expiration time to avoid repeated JWT decoding
// Key: token value (first 20 chars as identifier), Value: expiration timestamp in milliseconds
let tokenExpirationCache: { token: string; expirationTime: number } | null = null

/**
 * Clear the token expiration cache
 * Call this when token is refreshed or cleared
 */
export function clearTokenExpirationCache(): void {
  tokenExpirationCache = null
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
 * Check if token is expired
 * 
 * **Optimized with caching:** Caches expiration time to avoid repeated JWT decoding.
 * Cache is cleared when token changes or when clearTokenExpirationCache() is called.
 * 
 * @param token - JWT token
 * @param bufferSeconds - How many seconds in advance to consider expired (default 60 seconds, i.e., 1 minute in advance)
 * @returns true means expired or about to expire, false means still valid
 */
export function isTokenExpired(token: string | null, bufferSeconds: number = 60): boolean {
  if (!token) {
    tokenExpirationCache = null
    return true
  }

  // Check cache: if token matches and cache exists, use cached expiration time
  if (tokenExpirationCache && tokenExpirationCache.token === token) {
    const currentTime = Date.now()
    const bufferTime = bufferSeconds * 1000
    return currentTime + bufferTime >= tokenExpirationCache.expirationTime
  }

  // Cache miss: decode JWT and cache the expiration time
  const payload = decodeJwt(token)
  if (!payload || !payload.eff) {
    tokenExpirationCache = null
    return true
  }

  // Cache the expiration time for this token
  tokenExpirationCache = {
    token,
    expirationTime: payload.eff,
  }

  // Check expiration
  const currentTime = Date.now()
  const bufferTime = bufferSeconds * 1000
  return currentTime + bufferTime >= payload.eff
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
