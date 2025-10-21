type StorageMode = 'local' | 'session'

const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const STORAGE_MODE_KEY = 'token_storage_mode'

function getStorage(mode?: StorageMode): Storage {
  const m = mode || getTokenStorageMode()
  return m === 'local' ? localStorage : sessionStorage
}

export function setTokenStorageMode(mode: StorageMode) {
  localStorage.setItem(STORAGE_MODE_KEY, mode)
}

export function getTokenStorageMode(): StorageMode {
  const m = localStorage.getItem(STORAGE_MODE_KEY)
  return m === 'local' || m === 'session' ? (m as StorageMode) : 'session'
}

export function setTokens(
  accessToken: string | null,
  refreshToken: string | null,
  mode: StorageMode,
) {
  setTokenStorageMode(mode)
  const storage = getStorage(mode)
  if (accessToken) storage.setItem(ACCESS_TOKEN_KEY, accessToken)
  else storage.removeItem(ACCESS_TOKEN_KEY)
  if (refreshToken) storage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  else storage.removeItem(REFRESH_TOKEN_KEY)
  // 清理另一种存储，避免脏数据
  const other = mode === 'local' ? sessionStorage : localStorage
  other.removeItem(ACCESS_TOKEN_KEY)
  other.removeItem(REFRESH_TOKEN_KEY)
}

export function setAccessToken(token: string | null, mode?: StorageMode) {
  const storage = getStorage(mode)
  if (token) storage.setItem(ACCESS_TOKEN_KEY, token)
  else storage.removeItem(ACCESS_TOKEN_KEY)
}

export function getAccessToken(): string | null {
  const storage = getStorage()
  return storage.getItem(ACCESS_TOKEN_KEY)
}

export function setRefreshToken(token: string | null, mode?: StorageMode) {
  const storage = getStorage(mode)
  if (token) storage.setItem(REFRESH_TOKEN_KEY, token)
  else storage.removeItem(REFRESH_TOKEN_KEY)
}

export function getRefreshToken(): string | null {
  const storage = getStorage()
  return storage.getItem(REFRESH_TOKEN_KEY)
}

export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
}
