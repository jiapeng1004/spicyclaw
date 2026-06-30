<script setup lang="ts">
import { ref } from 'vue'
import { api, type Skill, type SkillConfig } from '../api/client'

defineProps<{
  skills: Skill[]
  config: SkillConfig | null
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
  <div class="skills">
    <p class="hint">
      ClawHub 兼容 SKILL.md 格式。缓存目录：
      <code>{{ config?.skillsCacheDir ?? '…' }}</code>
      · 持久化：
      <code>{{ config?.skillsStoreDir ?? '…' }}</code>
    </p>
    <a
      v-if="config?.clawhubUrl"
      class="link"
      :href="config.clawhubUrl"
      target="_blank"
      rel="noreferrer"
    >浏览 ClawHub →</a>

    <div class="install">
      <input
        v-model="installPath"
        placeholder="本地技能目录或 .zip 路径"
        :disabled="loading"
      />
      <button :disabled="loading" @click="install">安装</button>
      <button class="secondary" :disabled="loading" @click="reload">重载</button>
    </div>
    <p v-if="error" class="error">{{ error }}</p>

    <ul>
      <li v-for="s in skills" :key="s.id">
        <div class="meta">
          <strong>{{ s.name }}</strong>
          <span class="slug">{{ s.slug }}</span>
        </div>
        <p>{{ s.description }}</p>
        <div class="actions">
          <span class="source">{{ s.source }}</span>
          <button
            class="secondary"
            @click="toggle(s.slug, s.enabled)"
          >
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
}

.hint {
  margin: 0;
  font-size: 12px;
  color: #8b949e;
}

code {
  color: #ff6b35;
  word-break: break-all;
}

.link {
  color: #58a6ff;
  font-size: 13px;
}

.install {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

input {
  background: #1a1f2b;
  border: 1px solid #30363d;
  border-radius: 8px;
  color: inherit;
  padding: 10px 12px;
}

button {
  background: #ff6b35;
  color: #fff;
}

button.secondary {
  background: #1f2430;
  color: #c9d1d9;
}

.error {
  color: #f85149;
  font-size: 13px;
  margin: 0;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

li {
  background: #1a1f2b;
  border-radius: 10px;
  padding: 12px;
}

.meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.slug {
  font-size: 11px;
  color: #8b949e;
}

p {
  margin: 8px 0;
  font-size: 13px;
  color: #c9d1d9;
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.source {
  font-size: 11px;
  color: #8b949e;
}

.empty {
  color: #8b949e;
  font-size: 13px;
}
</style>
