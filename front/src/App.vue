<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { api, type LlmModel, type Message, type Session, type Skill, type SkillConfig, type User } from './api/client'
import SessionSidebar from './components/SessionSidebar.vue'
import ChatPanel from './components/ChatPanel.vue'
import SkillPanel from './components/SkillPanel.vue'
import LoginPanel from './components/LoginPanel.vue'

const user = ref<User | null>(null)
const authChecked = ref(false)
const sessions = ref<Session[]>([])
const skills = ref<Skill[]>([])
const models = ref<LlmModel[]>([])
const activeSessionId = ref<string | null>(null)
const selectedModelSlug = ref<string | null>(null)
const messages = ref<Message[]>([])
const tab = ref<'chat' | 'skills'>('chat')
const skillConfig = ref<SkillConfig | null>(null)

async function checkAuth() {
  try {
    user.value = await api.me()
    await bootstrap()
  } catch {
    user.value = null
  } finally {
    authChecked.value = true
  }
}

async function onLoginSuccess(loggedIn: User) {
  user.value = loggedIn
  await bootstrap()
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
  const session = await api.createSession(
    selectedModelSlug.value ? { modelSlug: selectedModelSlug.value } : undefined,
  )
  sessions.value.unshift(session)
  activeSessionId.value = session.id
  messages.value = []
}

async function deleteSession(id: string) {
  await api.deleteSession(id)
  sessions.value = sessions.value.filter((s) => s.id !== id)
  if (activeSessionId.value === id) {
    activeSessionId.value = sessions.value[0]?.id ?? null
  }
}

async function bootstrap() {
  await Promise.all([refreshSessions(), refreshSkills(), refreshModels()])
  if (!activeSessionId.value) {
    await createSession()
  }
}

watch(activeSessionId, refreshMessages)

onMounted(checkAuth)
</script>

<template>
  <div v-if="!authChecked" class="loading">加载中…</div>
  <LoginPanel v-else-if="!user" @success="onLoginSuccess" />
  <div v-else class="layout">
    <aside class="sidebar">
      <header class="brand">
        <span class="logo">🦞</span>
        <div>
          <h1>SpicyClaw</h1>
          <p>{{ user.displayName || user.username }}</p>
        </div>
        <button class="logout" @click="logout">退出</button>
      </header>
      <nav class="tabs">
        <button :class="{ active: tab === 'chat' }" @click="tab = 'chat'">对话</button>
        <button :class="{ active: tab === 'skills' }" @click="tab = 'skills'">技能</button>
      </nav>
      <SessionSidebar
        v-if="tab === 'chat'"
        :sessions="sessions"
        :models="models"
        :active-id="activeSessionId"
        :model-slug="selectedModelSlug"
        @select="activeSessionId = $event"
        @create="createSession"
        @delete="deleteSession"
        @update:model-slug="selectedModelSlug = $event"
      />
      <SkillPanel
        v-else
        :skills="skills"
        :config="skillConfig"
        @refresh="refreshSkills"
      />
    </aside>
    <main class="main">
      <ChatPanel
        v-if="tab === 'chat' && activeSessionId"
        :session-id="activeSessionId"
        :messages="messages"
        @sent="refreshMessages(); refreshSessions()"
      />
      <div v-else-if="tab === 'chat'" class="empty">创建或选择一个会话</div>
    </main>
  </div>
</template>

<style scoped>
.loading {
  min-height: 100vh;
  display: grid;
  place-items: center;
  color: #8b949e;
}

.layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  min-height: 100vh;
}

.sidebar {
  background: #151922;
  border-right: 1px solid #252a36;
  display: flex;
  flex-direction: column;
  padding: 16px;
  gap: 12px;
}

.brand {
  display: flex;
  gap: 12px;
  align-items: center;
}

.logo {
  font-size: 28px;
}

.brand h1 {
  margin: 0;
  font-size: 18px;
}

.brand p {
  margin: 2px 0 0;
  color: #8b949e;
  font-size: 12px;
}

.logout {
  margin-left: auto;
  padding: 6px 10px;
  font-size: 12px;
  background: #1f2430;
  color: #c9d1d9;
}

.tabs {
  display: flex;
  gap: 8px;
}

.tabs button {
  flex: 1;
  background: #1f2430;
  color: #c9d1d9;
}

.tabs button.active {
  background: #ff6b35;
  color: #fff;
}

.main {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.empty {
  margin: auto;
  color: #8b949e;
}
</style>
