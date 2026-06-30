const rawBase = import.meta.env.VITE_API_BASE_URL ?? '/api'

/** 去掉末尾斜杠，保证路径拼接一致 */
export const apiBaseUrl = rawBase.replace(/\/$/, '')

export const appTitle = import.meta.env.VITE_APP_TITLE ?? 'SpicyClaw'
