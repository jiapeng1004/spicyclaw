export type ThemeMode = 'light' | 'dark'

const STORAGE_KEY = 'spicyclaw-theme'

function readStoredTheme(): ThemeMode | null {
  const v = localStorage.getItem(STORAGE_KEY)
  return v === 'light' || v === 'dark' ? v : null
}

function systemTheme(): ThemeMode {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function applyTheme(mode: ThemeMode) {
  document.documentElement.dataset.theme = mode
  localStorage.setItem(STORAGE_KEY, mode)
}

export function initTheme() {
  applyTheme(readStoredTheme() ?? systemTheme())
}

export function getTheme(): ThemeMode {
  return document.documentElement.dataset.theme === 'dark' ? 'dark' : 'light'
}
