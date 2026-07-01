<script setup lang="ts">
import { ref, nextTick, computed } from 'vue'
import { api, type LlmModel, type Message } from '../api/client'

const props = defineProps<{
  sessionId: string | null
  messages: Message[]
  models: LlmModel[]
  modelSlug: string | null
  error?: string
  ensureSession: () => Promise<string | null>
}>()

const emit = defineEmits<{
  sent: []
  'update:modelSlug': [slug: string | null]
  'clear-error': []
}>()

const input = ref('')
const streaming = ref(false)
const streamText = ref('')
const listRef = ref<HTMLElement | null>(null)
const category = ref<'office' | 'code' | 'design'>('office')

const hasMessages = computed(() => props.messages.length > 0 || streaming.value)

const categories = [
  { id: 'office' as const, label: '日常办公', icon: '☕' },
  { id: 'code' as const, label: '代码开发', icon: '</>' },
  { id: 'design' as const, label: '设计创意', icon: '🎨' },
]

const quickActions: Record<string, string[]> = {
  office: ['写一份周报', '整理会议纪要', '数据分析报告', '更多'],
  code: ['代码审查', 'Bug 修复', '项目 README', '更多'],
  design: ['幻灯片', '海报设计', '交互原型', '更多'],
}

const enabledModels = computed(() => props.models.filter((m) => m.enabled))

async function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return

  emit('clear-error')
  let sessionId = props.sessionId
  if (!sessionId) {
    sessionId = await props.ensureSession()
    if (!sessionId) return
  }

  input.value = ''
  streaming.value = true
  streamText.value = ''
  await nextTick()
  scrollBottom()

  try {
    for await (const chunk of api.streamMessage(sessionId, text)) {
      if (chunk.event === 'delta' || chunk.event === 'error') {
        streamText.value += chunk.data
        scrollBottom()
      }
    }
  } finally {
    streaming.value = false
    streamText.value = ''
    emit('sent')
  }
}

function scrollBottom() {
  nextTick(() => {
    if (listRef.value) {
      listRef.value.scrollTop = listRef.value.scrollHeight
    }
  })
}

function onKeydown(e: KeyboardEvent) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault()
    send()
  }
}

function fillPrompt(text: string) {
  if (text === '更多') return
  input.value = text
}
</script>

<template>
  <div class="workspace">
    <p v-if="error" class="banner error">{{ error }}</p>

    <div v-if="hasMessages" ref="listRef" class="thread">
      <div v-for="m in messages" :key="m.id" class="msg" :class="m.role">
        <div class="msg-label">{{ m.role === 'user' ? '你' : 'SpicyClaw' }}</div>
        <div class="msg-body">{{ m.content }}</div>
      </div>
      <div v-if="streaming" class="msg assistant">
        <div class="msg-label">SpicyClaw</div>
        <div class="msg-body">{{ streamText || '思考中…' }}</div>
      </div>
    </div>

    <div v-else class="hero">
      <h1 class="hero-title">
        <span class="brand">SpicyClaw</span>
        <span class="tagline">你的职场超能力</span>
      </h1>

      <div class="categories">
        <button
          v-for="c in categories"
          :key="c.id"
          class="cat"
          :class="{ active: category === c.id }"
          @click="category = c.id"
        >
          <span class="cat-icon">{{ c.icon }}</span>
          {{ c.label }}
        </button>
      </div>

      <div class="chips">
        <button
          v-for="chip in quickActions[category]"
          :key="chip"
          class="chip"
          @click="fillPrompt(chip)"
        >
          {{ chip }}
        </button>
      </div>
    </div>

    <div class="composer-area">
      <div class="composer-card">
        <textarea
          v-model="input"
          rows="2"
          placeholder="今天帮你做些什么？@ 引用对话文件，/ 调用技能与指令"
          :disabled="streaming"
          @keydown="onKeydown"
        />

        <div class="composer-bar">
          <div class="composer-tools">
            <button class="tool-chip" type="button">Craft ▾</button>
            <select
              v-if="enabledModels.length"
              class="tool-chip select"
              :value="modelSlug ?? ''"
              @change="emit('update:modelSlug', ($event.target as HTMLSelectElement).value || null)"
            >
              <option v-for="m in enabledModels" :key="m.id" :value="m.slug">
                {{ m.displayName }}{{ m.isDefault ? '（默认）' : '' }}
              </option>
            </select>
            <button v-else class="tool-chip" type="button">自动 ▾</button>
            <button class="tool-chip" type="button">技能 ▾</button>
            <button class="tool-chip" type="button">连应用 ▾</button>
            <button class="tool-chip" type="button">默认权限 ▾</button>
          </div>

          <div class="composer-actions">
            <button class="icon-btn" type="button" title="添加" disabled>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="5" x2="12" y2="19" />
                <line x1="5" y1="12" x2="19" y2="12" />
              </svg>
            </button>
            <button class="icon-btn" type="button" title="灵感" disabled>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 2l1.5 4.5L18 8l-4.5 1.5L12 14l-1.5-4.5L6 8l4.5-1.5L12 2z" />
              </svg>
            </button>
            <button class="icon-btn" type="button" title="语音" disabled>
              <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M12 1a3 3 0 0 0-3 3v8a3 3 0 0 0 6 0V4a3 3 0 0 0-3-3z" />
                <path d="M19 10v2a7 7 0 0 1-14 0v-2" />
                <line x1="12" y1="19" x2="12" y2="23" />
              </svg>
            </button>
            <button
              class="send-btn"
              type="button"
              :disabled="streaming || !input.trim()"
              @click="send"
            >
              <svg v-if="!streaming" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <line x1="12" y1="19" x2="12" y2="5" />
                <polyline points="5 12 12 5 19 12" />
              </svg>
              <span v-else class="spinner" />
            </button>
          </div>
        </div>
      </div>
      <button class="workspace-link" type="button">选择工作空间 ›</button>
    </div>
  </div>
</template>

<style scoped>
.workspace {
  flex: 1;
  display: flex;
  flex-direction: column;
  min-height: 0;
  background: var(--wb-bg-elevated);
  position: relative;
}

.banner {
  margin: 12px 48px 0;
  padding: 10px 14px;
  border-radius: var(--wb-radius-sm);
  font-size: 13px;
}

.banner.error {
  background: rgba(248, 81, 73, 0.08);
  border: 1px solid rgba(248, 81, 73, 0.25);
  color: var(--wb-danger);
}

.thread {
  flex: 1;
  overflow: auto;
  padding: 24px 32px 200px;
  max-width: 820px;
  width: 100%;
  margin: 0 auto;
}

.msg {
  margin-bottom: 20px;
}

.msg-label {
  font-size: 12px;
  color: var(--wb-text-muted);
  margin-bottom: 6px;
}

.msg.user .msg-label {
  text-align: right;
}

.msg-body {
  padding: 14px 16px;
  border-radius: var(--wb-radius-lg);
  background: var(--wb-bg);
  border: 1px solid var(--wb-border-subtle);
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.65;
}

.msg.user .msg-body {
  background: var(--wb-bg-hover);
}

.msg.assistant .msg-body {
  border-left: 3px solid var(--wb-accent);
}

.hero {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  justify-content: center;
  padding: 0 48px 180px;
  max-width: 900px;
}

.hero-title {
  margin: 0 0 28px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.brand {
  font-size: 36px;
  font-weight: 700;
  color: var(--wb-text);
  letter-spacing: -0.02em;
}

.tagline {
  font-size: 36px;
  font-weight: 700;
  color: var(--wb-text);
}

.categories {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.cat {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 999px;
  background: var(--wb-bg-elevated);
  border: 1px solid var(--wb-border);
  color: var(--wb-text);
  font-size: 14px;
}

.cat.active {
  background: var(--wb-pill-active-bg);
  border-color: var(--wb-pill-active-bg);
  color: var(--wb-pill-active-text);
}

.cat-icon {
  font-size: 13px;
}

.chips {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.chip {
  padding: 6px 14px;
  border-radius: 999px;
  background: transparent;
  border: 1px solid var(--wb-border);
  color: var(--wb-text-secondary);
  font-size: 13px;
}

.chip:hover {
  background: var(--wb-bg-hover);
  color: var(--wb-text);
}

.composer-area {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 0 48px 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  background: linear-gradient(to top, var(--wb-bg-elevated) 70%, transparent);
}

.composer-card {
  width: 100%;
  max-width: 760px;
  background: var(--wb-bg-elevated);
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-xl);
  box-shadow: var(--wb-shadow-lg);
  overflow: hidden;
}

.composer-card textarea {
  width: 100%;
  border: none;
  padding: 18px 20px 8px;
  resize: none;
  background: transparent;
  color: var(--wb-text);
  font-size: 15px;
  line-height: 1.5;
  min-height: 56px;
}

.composer-card textarea:focus {
  outline: none;
}

.composer-card textarea::placeholder {
  color: var(--wb-text-muted);
}

.composer-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 12px 12px;
}

.composer-tools {
  display: flex;
  gap: 6px;
  flex-wrap: wrap;
  min-width: 0;
}

.tool-chip {
  padding: 4px 10px;
  border-radius: 999px;
  background: var(--wb-bg);
  border: 1px solid var(--wb-border-subtle);
  color: var(--wb-text-secondary);
  font-size: 12px;
  white-space: nowrap;
}

.tool-chip.select {
  appearance: none;
  padding-right: 22px;
  background-image: url("data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='10' height='10' viewBox='0 0 24 24' fill='none' stroke='%238f959e' stroke-width='2'%3E%3Cpolyline points='6 9 12 15 18 9'/%3E%3C/svg%3E");
  background-repeat: no-repeat;
  background-position: right 8px center;
}

.composer-actions {
  display: flex;
  align-items: center;
  gap: 4px;
  flex-shrink: 0;
}

.icon-btn {
  background: transparent;
  color: var(--wb-text-muted);
  padding: 8px;
  border-radius: 50%;
  display: grid;
  place-items: center;
}

.send-btn {
  width: 36px;
  height: 36px;
  padding: 0;
  border-radius: 50%;
  background: var(--wb-accent);
  color: #fff;
  display: grid;
  place-items: center;
}

.send-btn:hover:not(:disabled) {
  background: var(--wb-accent-hover);
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.35);
  border-top-color: #fff;
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.workspace-link {
  margin-top: 10px;
  background: transparent;
  color: var(--wb-text-muted);
  font-size: 12px;
  padding: 4px 8px;
}

.workspace-link:hover {
  color: var(--wb-text-secondary);
}
</style>
