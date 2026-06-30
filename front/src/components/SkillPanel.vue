<script setup lang="ts">
import { ref } from 'vue'
import { api, type Skill, type SkillConfig } from '../api/client'

defineProps<{
  skills: Skill[]
  config: SkillConfig | null
  compact?: boolean
}>()

const emit = defineEmits<{ refresh: [] }>()

const installPath = ref('')
const loading = ref(false)
const error = ref('')

async function install() {
  if (!installPath.value.trim()) return
  loading.value = true
  error.value = ''
  try {
    await api.installSkill(installPath.value.trim())
    installPath.value = ''
    emit('refresh')
  } catch (e) {
    error.value = e instanceof Error ? e.message : String(e)
  } finally {
    loading.value = false
  }
}

async function toggle(slug: string, enabled: boolean) {
  await api.toggleSkill(slug, !enabled)
  emit('refresh')
}

async function reload() {
  await api.reloadSkills()
  emit('refresh')
}
</script>

<template>
  <div class="skills" :class="{ compact }">
    <header v-if="!compact" class="page-head">
      <h1>技能管理</h1>
      <p>ClawHub 兼容 SKILL.md 格式。技能是可复用的能力模块，由 Agent 按需加载，与专家和 MCP 连接器相互独立。</p>
    </header>

    <p class="hint">
      <template v-if="compact">技能扩展</template>
      <template v-else>缓存与持久化目录</template>
      <code>{{ config?.skillsCacheDir ?? '…' }}</code>
      <template v-if="!compact">
        · <code>{{ config?.skillsStoreDir ?? '…' }}</code>
      </template>
    </p>

    <a
      v-if="config?.clawhubUrl"
      class="link"
      :href="config.clawhubUrl"
      target="_blank"
      rel="noreferrer"
    >
      浏览 ClawHub →
    </a>

    <div class="install">
      <input
        v-model="installPath"
        placeholder="本地技能目录或 .zip 路径"
        :disabled="loading"
      />
      <div class="install-actions">
        <button :disabled="loading" @click="install">安装</button>
        <button class="secondary" :disabled="loading" @click="reload">重载</button>
      </div>
    </div>
    <p v-if="error" class="error">{{ error }}</p>

    <ul>
      <li v-for="s in skills" :key="s.id">
        <div class="card-head">
          <div class="meta">
            <strong>{{ s.name }}</strong>
            <span class="slug">{{ s.slug }}</span>
          </div>
          <span class="badge" :class="{ on: s.enabled }">
            {{ s.enabled ? '已启用' : '已禁用' }}
          </span>
        </div>
        <p class="desc">{{ s.description }}</p>
        <div class="actions">
          <span class="source">{{ s.source }}</span>
          <button class="secondary" @click="toggle(s.slug, s.enabled)">
            {{ s.enabled ? '禁用' : '启用' }}
          </button>
        </div>
      </li>
    </ul>
    <p v-if="!skills.length" class="empty">暂无技能，可从 ClawHub 安装或放入 skills 目录</p>
  </div>
</template>

<style scoped>
.skills {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 0;
}

.skills:not(.compact) {
  padding: 24px 32px;
  max-width: 860px;
}

.page-head h1 {
  margin: 0;
  font-size: 20px;
}

.page-head p {
  margin: 6px 0 0;
  color: var(--wb-text-secondary);
  font-size: 14px;
}

.skills:not(.compact) {
  background: var(--wb-bg-elevated);
}

.hint {
  margin: 0;
  font-size: 12px;
  color: var(--wb-text-secondary);
  line-height: 1.6;
}

code {
  color: var(--wb-accent);
  word-break: break-all;
  font-size: 11px;
}

.link {
  color: var(--wb-link);
  font-size: 13px;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
}

.install {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.install input {
  background: var(--wb-bg);
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-sm);
  color: inherit;
  padding: 10px 12px;
}

.install-actions {
  display: flex;
  gap: 8px;
}

button {
  background: var(--wb-accent);
  color: #fff;
  padding: 8px 14px;
}

button.secondary {
  background: var(--wb-bg-hover);
  color: var(--wb-text);
  border: 1px solid var(--wb-border);
}

.error {
  color: var(--wb-danger);
  font-size: 13px;
  margin: 0;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

li {
  background: var(--wb-bg);
  border: 1px solid var(--wb-border-subtle);
  border-radius: var(--wb-radius);
  padding: 12px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 8px;
}

.meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.slug {
  font-size: 11px;
  color: var(--wb-text-muted);
}

.badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 999px;
  background: var(--wb-bg-hover);
  color: var(--wb-text-muted);
  flex-shrink: 0;
}

.badge.on {
  background: rgba(63, 185, 80, 0.15);
  color: var(--wb-success);
}

.desc {
  margin: 8px 0;
  font-size: 13px;
  color: var(--wb-text-secondary);
  line-height: 1.5;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.source {
  font-size: 11px;
  color: var(--wb-text-muted);
}

.empty {
  color: var(--wb-text-muted);
  font-size: 13px;
  text-align: center;
  padding: 24px 8px;
}
</style>
