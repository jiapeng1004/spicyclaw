<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch, computed } from 'vue'
import { api, type LlmModel, type Message, type Session, type Skill, type SkillConfig, type User } from './api/client'
import { getStoredUser, saveAuthSession } from './auth/token'
import SessionSidebar from './components/SessionSidebar.vue'
import ChatPanel from './components/ChatPanel.vue'
import SkillPanel from './components/SkillPanel.vue'
import ExpertPanel from './components/ExpertPanel.vue'
import ConnectorPanel from './components/ConnectorPanel.vue'
import LoginPanel from './components/LoginPanel.vue'
import SettingsPanel from './components/SettingsPanel.vue'
import UsageManagement from './components/UsageManagement.vue'

const user = ref<User | null>(null)
const authChecked = ref(false)
const sessions = ref<Session[]>([])
const skills = ref<Skill[]>([])
const models = ref<LlmModel[]>([])
const activeSessionId = ref<string | null>(null)
const selectedModelSlug = ref<string | null>(null)
const messages = ref<Message[]>([])
const tab = ref<'chat' | 'experts' | 'connectors' | 'skills'>('chat')
const skillConfig = ref<SkillConfig | null>(null)
const sidebarCollapsed = ref(false)
const showSearch = ref(false)
const searchQuery = ref('')
const settingsOpen = ref(false)
const page = ref<'workspace' | 'usage'>('workspace')
const footRef = ref<HTMLElement | null>(null)
const workspaceError = ref('')
const creatingSession = ref(false)

function switchTab(next: typeof tab.value) {
  tab.value = next
  workspaceError.value = ''
}

function openChat() {
  switchTab('chat')
}

function openUsage() {
  settingsOpen.value = false
  page.value = 'usage'
}

function onDocClick(e: MouseEvent) {
  if (!settingsOpen.value) return
  if (footRef.value?.contains(e.target as Node)) return
  settingsOpen.value = false
}

onMounted(() => {
  checkAuth()
  document.addEventListener('click', onDocClick)
})

onUnmounted(() => {
  document.removeEventListener('click', onDocClick)
})

async function checkAuth() {
  if (!api.hasToken()) {
    user.value = null
    authChecked.value = true
    return
  }
  try {
    const me = await api.me()
    user.value = me
    saveAuthSession(api.getToken()!, me)
  } catch {
    user.value = null
  }
  authChecked.value = true
  if (user.value) {
    try {
      await bootstrap()
    } catch {
      // 保持登录态，token 仍在 localStorage；业务数据稍后重试
    }
  }
}

async function onLoginSuccess(loggedIn: User) {
  user.value = loggedIn
  try {
    await bootstrap()
  } catch {
    // 登录已成功，token 已写入 storage；bootstrap 失败不踢回登录页
  }
}

async function logout() {
  await api.logout()
  user.value = null
  sessions.value = []
  models.value = []
  messages.value = []
  activeSessionId.value = null
}

async function refreshSessions() {
  sessions.value = await api.listSessions()
  if (!activeSessionId.value && sessions.value.length > 0) {
    activeSessionId.value = sessions.value[0].id
  }
}

async function refreshSkills() {
  skills.value = await api.listSkills()
  skillConfig.value = await api.skillConfig()
}

async function refreshModels() {
  models.value = await api.listModels()
  if (!selectedModelSlug.value) {
    selectedModelSlug.value =
      models.value.find((m) => m.isDefault && m.enabled)?.slug ??
      models.value.find((m) => m.enabled)?.slug ??
      null
  }
}

async function refreshMessages() {
  if (!activeSessionId.value) {
    messages.value = []
    return
  }
  messages.value = await api.listMessages(activeSessionId.value)
}

async function createSession() {
  if (creatingSession.value) return activeSessionId.value
  creatingSession.value = true
  workspaceError.value = ''
  try {
    const session = await api.createSession(
      selectedModelSlug.value ? { modelSlug: selectedModelSlug.value } : undefined,
    )
    sessions.value.unshift(session)
    activeSessionId.value = session.id
    messages.value = []
    switchTab('chat')
    return session.id
  } catch (e) {
    const msg = e instanceof Error ? e.message : '创建任务失败'
    if (msg.includes('未登录') || msg.includes('令牌')) {
      user.value = null
    } else {
      workspaceError.value = msg
    }
    return null
  } finally {
    creatingSession.value = false
  }
}

async function ensureSession(): Promise<string | null> {
  if (activeSessionId.value) return activeSessionId.value
  return createSession()
}

async function deleteSession(id: string) {
  await api.deleteSession(id)
  sessions.value = sessions.value.filter((s) => s.id !== id)
  if (activeSessionId.value === id) {
    activeSessionId.value = sessions.value[0]?.id ?? null
    if (!activeSessionId.value) {
      await createSession()
    }
  }
}

async function bootstrap() {
  await Promise.all([refreshSessions(), refreshSkills(), refreshModels()])
  if (sessions.value.length > 0 && !activeSessionId.value) {
    activeSessionId.value = sessions.value[0].id
  }
}

const filteredSessions = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  if (!q) return sessions.value
  return sessions.value.filter((s) => s.title.toLowerCase().includes(q))
})

watch(activeSessionId, refreshMessages)
</script>

<template>
  <div v-if="!authChecked" class="loading">
    <span class="spinner" />
    <p>加载中…</p>
  </div>
  <LoginPanel v-else-if="!user" @success="onLoginSuccess" />
  <UsageManagement
    v-else-if="page === 'usage'"
    :user="user"
    @back="page = 'workspace'"
  />
  <div v-else class="shell">
    <aside class="sidebar" :class="{ collapsed: sidebarCollapsed }">
      <header class="sb-head">
        <div v-if="!sidebarCollapsed" class="sb-brand">
          <span class="logo-mark">🦞</span>
          <div>
            <span class="logo-name">SpicyClaw</span>
            <span class="logo-ver">v0.0.1</span>
          </div>
        </div>
        <div class="sb-tools">
          <button class="tool" title="收起侧栏" @click="sidebarCollapsed = !sidebarCollapsed">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <rect x="3" y="3" width="18" height="18" rx="2" />
              <line x1="9" y1="3" x2="9" y2="21" />
            </svg>
          </button>
          <button v-if="!sidebarCollapsed" class="tool" title="搜索" @click="showSearch = !showSearch">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8" />
              <line x1="21" y1="21" x2="16.65" y2="16.65" />
            </svg>
          </button>
        </div>
      </header>

      <div v-if="showSearch && !sidebarCollapsed" class="sb-search">
        <input v-model="searchQuery" placeholder="搜索任务…" />
      </div>

      <nav v-if="!sidebarCollapsed" class="sb-nav">
        <button class="nav-item highlight" :disabled="creatingSession" @click="createSession">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <line x1="12" y1="5" x2="12" y2="19" />
            <line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          {{ creatingSession ? '创建中…' : '新建任务' }}
        </button>
        <button class="nav-item" :class="{ active: tab === 'chat' }" @click="openChat">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
          </svg>
          助理
        </button>
        <button class="nav-item muted" disabled>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
          </svg>
          项目
        </button>
        <button class="nav-item" :class="{ active: tab === 'experts' }" @click="switchTab('experts')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="8" r="4" />
            <path d="M6 20v-2a4 4 0 0 1 4-4h4a4 4 0 0 1 4 4v2" />
          </svg>
          专家
        </button>
        <button class="nav-item" :class="{ active: tab === 'connectors' }" @click="switchTab('connectors')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71" />
            <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71" />
          </svg>
          连接器
          <span class="nav-tag">MCP</span>
        </button>
        <button class="nav-item" :class="{ active: tab === 'skills' }" @click="switchTab('skills')">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polygon points="12 2 2 7 12 12 22 7 12 2" />
            <polyline points="2 17 12 22 22 17" />
            <polyline points="2 12 12 17 22 12" />
          </svg>
          技能
        </button>
        <button class="nav-item muted" disabled>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
          自动化
        </button>
        <button class="nav-item muted" disabled>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="1" />
            <circle cx="19" cy="12" r="1" />
            <circle cx="5" cy="12" r="1" />
          </svg>
          更多
          <span class="nav-tag">资料库·灵感</span>
        </button>
      </nav>

      <SessionSidebar
        v-if="!sidebarCollapsed && tab === 'chat'"
        :sessions="filteredSessions"
        :active-id="activeSessionId"
        @select="(id) => { activeSessionId = id; openChat() }"
        @delete="deleteSession"
      />

      <footer v-if="!sidebarCollapsed" ref="footRef" class="sb-foot">
        <SettingsPanel v-if="settingsOpen" @open-usage="openUsage" />

        <button
          class="user-chip"
          :class="{ active: settingsOpen }"
          @click.stop="settingsOpen = !settingsOpen"
        >
          <div class="avatar">{{ (user.displayName || user.username).charAt(0).toUpperCase() }}</div>
          <span class="uname">{{ user.displayName || user.username }}</span>
        </button>
        <div class="foot-actions">
          <button class="tool" title="通知" disabled>
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
              <path d="M13.73 21a2 2 0 0 1-3.46 0" />
            </svg>
          </button>
          <button class="tool" title="退出登录" @click="logout">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
              <polyline points="16 17 21 12 16 7" />
              <line x1="21" y1="12" x2="9" y2="12" />
            </svg>
          </button>
        </div>
      </footer>
    </aside>

    <main class="main">
      <ChatPanel
        v-if="tab === 'chat'"
        :session-id="activeSessionId"
        :messages="messages"
        :models="models"
        :model-slug="selectedModelSlug"
        :error="workspaceError"
        :ensure-session="ensureSession"
        @sent="refreshMessages(); refreshSessions()"
        @update:model-slug="selectedModelSlug = $event"
        @clear-error="workspaceError = ''"
      />
      <ExpertPanel v-else-if="tab === 'experts'" :models="models" />
      <ConnectorPanel v-else-if="tab === 'connectors'" />
      <SkillPanel
        v-else-if="tab === 'skills'"
        :skills="skills"
        :config="skillConfig"
        @refresh="refreshSkills"
      />
    </main>
  </div>
</template>

<style scoped>
.loading {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: var(--wb-text-secondary);
  background: var(--wb-bg-elevated);
}

.spinner {
  width: 28px;
  height: 28px;
  border: 2px solid var(--wb-border);
  border-top-color: var(--wb-accent);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.shell {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: var(--wb-bg);
}

.sidebar {
  width: var(--wb-sidebar-width);
  background: var(--wb-bg-elevated);
  border-right: 1px solid var(--wb-border-subtle);
  display: flex;
  flex-direction: column;
  flex-shrink: 0;
  transition: width 0.2s;
}

.sidebar.collapsed {
  width: 52px;
}

.sb-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 12px 10px;
}

.sb-brand {
  display: flex;
  align-items: center;
  gap: 8px;
}

.logo-mark {
  font-size: 22px;
}

.logo-name {
  display: block;
  font-size: 15px;
  font-weight: 600;
  line-height: 1.2;
}

.logo-ver {
  font-size: 11px;
  color: var(--wb-text-muted);
}

.sb-tools {
  display: flex;
  gap: 2px;
}

.tool {
  background: transparent;
  color: var(--wb-text-muted);
  padding: 6px;
  border-radius: var(--wb-radius-sm);
  display: grid;
  place-items: center;
}

.tool:hover:not(:disabled) {
  background: var(--wb-bg-hover);
  color: var(--wb-text);
}

.sb-search {
  padding: 0 12px 8px;
}

.sb-search input {
  width: 100%;
  padding: 7px 10px;
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-sm);
  background: var(--wb-bg);
  color: var(--wb-text);
  font-size: 13px;
}

.sb-nav {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 0 8px 8px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 8px 10px;
  background: transparent;
  color: var(--wb-text);
  font-size: 14px;
  text-align: left;
  border-radius: var(--wb-radius-sm);
}

.nav-item svg {
  color: var(--wb-text-secondary);
  flex-shrink: 0;
}

.nav-item:hover:not(:disabled):not(.muted) {
  background: var(--wb-bg-hover);
}

.nav-item.active {
  background: var(--wb-bg-hover);
  font-weight: 500;
}

.nav-item.highlight {
  font-weight: 500;
}

.nav-item.muted {
  opacity: 0.45;
  cursor: default;
}

.nav-tag {
  margin-left: auto;
  font-size: 11px;
  color: var(--wb-text-muted);
}

.sb-foot {
  margin-top: auto;
  position: relative;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-top: 1px solid var(--wb-border-subtle);
}

.user-chip {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
  background: transparent;
  padding: 0;
  color: inherit;
  text-align: left;
  flex: 1;
}

.user-chip:hover,
.user-chip.active {
  opacity: 0.85;
}

.avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: var(--wb-accent);
  color: #fff;
  display: grid;
  place-items: center;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.uname {
  font-size: 13px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.foot-actions {
  display: flex;
  gap: 2px;
}

.main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  background: var(--wb-bg-elevated);
}
</style>
