const TOKEN_KEY = 'spicyclaw-access-token'

export function getStoredToken(): string | null {
  try {
    return localStorage.getItem(TOKEN_KEY)
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

export function clearAccessToken() {
  localStorage.removeItem(TOKEN_KEY)
}

export function hasStoredToken(): boolean {
  return Boolean(getStoredToken())
}

export function authHeader(): Record<string, string> {
  const token = getStoredToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}
