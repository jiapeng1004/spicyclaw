const TOKEN_KEY = 'spicyclaw-access-token'
const USER_KEY = 'spicyclaw-user'

export interface StoredUser {
  id: string
  username: string
  displayName: string
}

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
  } catch {
    return null
  }
}

export function getStoredUser(): StoredUser | null {
  try {
    const raw = localStorage.getItem(USER_KEY)
    if (!raw) return null
    const parsed = JSON.parse(raw) as StoredUser
    if (!parsed?.id || !parsed?.username) return null
    return parsed
  } catch {
    return null
  }
}

export function saveAccessToken(token: string) {
  if (!token) {
    throw new Error('accessToken 为空，无法保存')
  }
  localStorage.setItem(TOKEN_KEY, token)
}

export function saveAuthSession(token: string, user: StoredUser) {
  saveAccessToken(token)
  localStorage.setItem(USER_KEY, JSON.stringify(user))
}

export function clearAuthSession() {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(USER_KEY)
}

/** @deprecated 使用 clearAuthSession */
export function clearAccessToken() {
  clearAuthSession()
}

export function hasStoredToken(): boolean {
  return Boolean(getStoredToken())
}

export function authHeader(): Record<string, string> {
  const token = getStoredToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}
