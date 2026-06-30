import { apiBaseUrl } from '../config/env'

export interface User {
  id: string
  username: string
  displayName: string
}

export interface Session {
  id: string
  title: string
  agentName: string
  modelRef?: string
  createdAt: string
  updatedAt: string
}

export interface Message {
  id: string
  sessionId: string
  role: string
  content: string
  createdAt: string
}

export interface Skill {
  id: string
  slug: string
  name: string
  description: string
  source: string
  path: string
  enabled: boolean
}

export interface SkillConfig {
  skillsCacheDir: string
  skillsStoreDir: string
  clawhubUrl: string
  clawhubEnabled: boolean
}

export interface LlmModel {
  id: string
  slug: string
  registryKey: string
  displayName: string
  provider: string
  modelName: string
  apiKeyMasked: string
  baseUrl?: string
  stream: boolean
  enableThinking?: boolean
  isDefault: boolean
  enabled: boolean
  createdAt: string
  updatedAt: string
}

const fetchDefaults: RequestInit = {
  credentials: 'include',
}

async function parseError(res: Response): Promise<string> {
  const text = await res.text()
  if (!text) return res.statusText
  try {
    const json = JSON.parse(text) as { detail?: string; title?: string }
    return json.detail ?? json.title ?? text
  } catch {
    return text
  }
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${apiBaseUrl}${path}`, {
    ...fetchDefaults,
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  })
  if (!res.ok) {
    throw new Error(await parseError(res))
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

export const api = {
  login: (username: string, password: string) =>
    request<User>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username, password }),
    }),
  me: () => request<User>('/auth/me'),
  logout: () =>
    fetch(`${apiBaseUrl}/auth/logout`, { method: 'POST', ...fetchDefaults }),
  health: () => request<{ status: string }>('/health'),
  listModels: () => request<LlmModel[]>('/models'),
  listSessions: () => request<Session[]>('/chat/sessions'),
  createSession: (options?: { title?: string; modelSlug?: string }) =>
    request<Session>('/chat/sessions', {
      method: 'POST',
      body: JSON.stringify(options ?? {}),
    }),
  deleteSession: async (id: string) => {
    const res = await fetch(`${apiBaseUrl}/chat/sessions/${id}`, { method: 'DELETE', ...fetchDefaults })
    if (!res.ok) throw new Error(await parseError(res))
  },
  listMessages: (sessionId: string) =>
    request<Message[]>(`/chat/sessions/${sessionId}/messages`),
  streamMessage: async function* (sessionId: string, content: string) {
    const res = await fetch(`${apiBaseUrl}/chat/sessions/${sessionId}/stream`, {
      ...fetchDefaults,
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ content }),
    })
    if (!res.ok || !res.body) {
      throw new Error(await parseError(res))
    }
    const reader = res.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''
    while (true) {
      const { done, value } = await reader.read()
      if (done) break
      buffer += decoder.decode(value, { stream: true })
      const parts = buffer.split('\n\n')
      buffer = parts.pop() ?? ''
      for (const part of parts) {
        const lines = part.split('\n')
        let event = 'message'
        let data = ''
        for (const line of lines) {
          if (line.startsWith('event:')) event = line.slice(6).trim()
          if (line.startsWith('data:')) data += line.slice(5).trim()
        }
        if (data) yield { event, data }
      }
    }
  },
  listSkills: () => request<Skill[]>('/skills'),
  installSkill: (path: string) =>
    request<Skill>('/skills/install', {
      method: 'POST',
      body: JSON.stringify({ path }),
    }),
  toggleSkill: (slug: string, enabled: boolean) =>
    request<Skill>(`/skills/${slug}/enabled`, {
      method: 'PUT',
      body: JSON.stringify({ enabled }),
    }),
  reloadSkills: () =>
    request<Skill[]>('/skills/reload', { method: 'POST' }),
  skillConfig: () => request<SkillConfig>('/skills/config'),
}
