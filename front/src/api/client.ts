import { apiBaseUrl } from '../config/env'
import {
  authHeader,
  clearAuthSession,
  getStoredToken,
  hasStoredToken,
  saveAuthSession,
} from '../auth/token'

export interface User {
  id: string
  username: string
  displayName: string
}

export interface LoginResult extends User {
  accessToken: string
  tokenType: string
  expiresIn: number
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

export interface UsagePackage {
  id: string
  name: string
  statusLabel?: string
  description: string
  hint?: string
  totalPoints: number
  usedPoints: number
  remainingPoints: number
  badge?: string
  purchasable: boolean
}

export interface UsageRecord {
  requestId: string
  pointsConsumed: number
  userPrompt: string
  model: string
  createdAt: string
}

export interface UsageOverview {
  activityPackage: UsagePackage
  addonPackage: UsagePackage
  records: UsageRecord[]
}

export interface AgentExpert {
  id: string
  name: string
  description: string
  sysPrompt: string
  maxIters: number
  defaultModelSlug: string | null
  enabled: boolean
}

export interface McpConnector {
  id: string
  name: string
  transport: string
  endpoint: string
  description: string
  enabled: boolean
  status: string
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

async function request<T>(path: string, init?: RequestInit & { revokeOn401?: boolean }): Promise<T> {
  const { revokeOn401 = path === '/auth/me', ...fetchInit } = init ?? {}
  const res = await fetch(`${apiBaseUrl}${path}`, {
    ...fetchInit,
    cache: 'no-store',
    headers: {
      'Content-Type': 'application/json',
      ...authHeader(),
      ...fetchInit.headers,
    },
  })
  if (res.status === 401 && revokeOn401) {
    clearAuthSession()
  }
  if (!res.ok) {
    throw new Error(await parseError(res))
  }
  if (res.status === 204) return undefined as T
  return res.json()
}

export const api = {
  hasToken: hasStoredToken,
  getToken: getStoredToken,

  login: async (username: string, password: string) => {
    const res = await fetch(`${apiBaseUrl}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password }),
    })
    if (!res.ok) {
      throw new Error(await parseError(res))
    }
    const result = (await res.json()) as LoginResult
    if (!result.accessToken) {
      throw new Error('登录响应缺少 accessToken')
    }
    const user: User = {
      id: result.id,
      username: result.username,
      displayName: result.displayName,
    }
    saveAuthSession(result.accessToken, user)
    return user
  },

  me: () => request<User>('/auth/me', { revokeOn401: true }),

  logout: async () => {
    try {
      await fetch(`${apiBaseUrl}/auth/logout`, {
        method: 'POST',
        headers: { ...authHeader() },
      })
    } finally {
      clearAuthSession()
    }
  },

  health: () => request<{ status: string }>('/health'),
  listModels: () => request<LlmModel[]>('/models'),
  listSessions: () => request<Session[]>('/chat/sessions'),
  createSession: (options?: { title?: string; modelSlug?: string }) =>
    request<Session>('/chat/sessions', {
      method: 'POST',
      body: JSON.stringify(options ?? {}),
      revokeOn401: true,
    }),
  deleteSession: async (id: string) => {
    const res = await fetch(`${apiBaseUrl}/chat/sessions/${id}`, {
      method: 'DELETE',
      cache: 'no-store',
      headers: { ...authHeader() },
    })
    if (!res.ok) throw new Error(await parseError(res))
  },
  listMessages: (sessionId: string) =>
    request<Message[]>(`/chat/sessions/${sessionId}/messages`),
  streamMessage: async function* (sessionId: string, content: string) {
    const res = await fetch(`${apiBaseUrl}/chat/sessions/${sessionId}/stream`, {
      method: 'POST',
      cache: 'no-store',
      headers: {
        'Content-Type': 'application/json',
        ...authHeader(),
      },
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
  usageOverview: (days = 7) => request<UsageOverview>(`/usage/overview?days=${days}`),
  listAgents: () => request<AgentExpert[]>('/agents'),
  listMcpConnectors: () => request<McpConnector[]>('/connectors/mcp'),
  registerMcpConnector: (body: {
    name: string
    transport: string
    endpoint: string
    description?: string
  }) =>
    request<McpConnector>('/connectors/mcp', {
      method: 'POST',
      body: JSON.stringify(body),
    }),
  deleteMcpConnector: async (id: string) => {
    const res = await fetch(`${apiBaseUrl}/connectors/mcp/${id}`, {
      method: 'DELETE',
      headers: { ...authHeader() },
    })
    if (!res.ok) throw new Error(await parseError(res))
  },
}
