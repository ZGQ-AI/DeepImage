/**
 * Token Storage Utilities
 *
 * **Architecture Principle: Storage-Only, No Memory Cache**
 * All token operations directly access browser storage (localStorage or sessionStorage).
 * No in-memory caching is performed to ensure single source of truth and prevent
 * synchronization issues when storage is manually cleared or modified across tabs.
 */

type StorageMode = 'local' | 'session'

const ACCESS_TOKEN_KEY = 'access_token'
const REFRESH_TOKEN_KEY = 'refresh_token'
const STORAGE_MODE_KEY = 'token_storage_mode'

/**
 * Get the appropriate storage (localStorage or sessionStorage) based on mode
 */
function getStorage(mode?: StorageMode): Storage {
  const m = mode || getTokenStorageMode()
  return m === 'local' ? localStorage : sessionStorage
}

/**
 * Set the storage mode preference (local or session)
 */
export function setTokenStorageMode(mode: StorageMode) {
  localStorage.setItem(STORAGE_MODE_KEY, mode)
}

/**
 * Get the current storage mode preference
 * Defaults to 'session' if not set
 */
export function getTokenStorageMode(): StorageMode {
  const m = localStorage.getItem(STORAGE_MODE_KEY)
  return m === 'local' || m === 'session' ? (m as StorageMode) : 'session'
}

/**
 * Store both access and refresh tokens in the specified storage
 *
 * **Storage-Only Policy:**
 * Tokens are written directly to browser storage without any memory caching.
 * The other storage type is cleared to prevent token conflicts.
 *
 * @param accessToken - The access token to store (or null to remove)
 * @param refreshToken - The refresh token to store (or null to remove)
 * @param mode - Storage mode: 'local' for localStorage, 'session' for sessionStorage
 */
export function setTokens(
  accessToken: string | null,
  refreshToken: string | null,
  mode: StorageMode,
) {
  setTokenStorageMode(mode)
  const storage = getStorage(mode)

  // Write tokens to the selected storage
  if (accessToken) storage.setItem(ACCESS_TOKEN_KEY, accessToken)
  else storage.removeItem(ACCESS_TOKEN_KEY)

  if (refreshToken) storage.setItem(REFRESH_TOKEN_KEY, refreshToken)
  else storage.removeItem(REFRESH_TOKEN_KEY)

  // Clear the other storage to avoid conflicts
  const other = mode === 'local' ? sessionStorage : localStorage
  other.removeItem(ACCESS_TOKEN_KEY)
  other.removeItem(REFRESH_TOKEN_KEY)
}

/**
 * Store only the access token
 *
 * **Storage-Only Policy:**
 * Token is written directly to storage without memory caching.
 *
 * @param token - The access token to store (or null to remove)
 * @param mode - Optional storage mode override
 */
export function setAccessToken(token: string | null, mode?: StorageMode) {
  const storage = getStorage(mode)
  if (token) storage.setItem(ACCESS_TOKEN_KEY, token)
  else storage.removeItem(ACCESS_TOKEN_KEY)
}

/**
 * Retrieve the access token from storage
 *
 * **Storage-Only Policy:**
 * Always reads directly from browser storage on every call.
 * No memory caching is performed to ensure the token is always up-to-date
 * and synchronized with manual storage changes or multi-tab operations.
 *
 * @returns The access token or null if not found
 */
export function getAccessToken(): string | null {
  const storage = getStorage()
  return storage.getItem(ACCESS_TOKEN_KEY)
}

/**
 * Store only the refresh token
 *
 * @param token - The refresh token to store (or null to remove)
 * @param mode - Optional storage mode override
 */
export function setRefreshToken(token: string | null, mode?: StorageMode) {
  const storage = getStorage(mode)
  if (token) storage.setItem(REFRESH_TOKEN_KEY, token)
  else storage.removeItem(REFRESH_TOKEN_KEY)
}

/**
 * Retrieve the refresh token from storage
 *
 * **Storage-Only Policy:**
 * Always reads directly from browser storage.
 *
 * @returns The refresh token or null if not found
 */
export function getRefreshToken(): string | null {
  const storage = getStorage()
  return storage.getItem(REFRESH_TOKEN_KEY)
}

/**
 * Clear all tokens from both localStorage and sessionStorage
 *
 * **Storage-Only Policy:**
 * Removes tokens from all storage locations to ensure complete cleanup.
 * This is called during logout or when authentication fails.
 */
export function clearTokens() {
  localStorage.removeItem(ACCESS_TOKEN_KEY)
  localStorage.removeItem(REFRESH_TOKEN_KEY)
  sessionStorage.removeItem(ACCESS_TOKEN_KEY)
  sessionStorage.removeItem(REFRESH_TOKEN_KEY)
}
