<script setup lang="ts">
import type { LlmModel, Session } from '../api/client'

defineProps<{
  sessions: Session[]
  models: LlmModel[]
  activeId: string | null
  modelSlug: string | null
}>()

defineEmits<{
  select: [id: string]
  create: []
  delete: [id: string]
  'update:modelSlug': [slug: string | null]
}>()
</script>

<template>
  <div class="panel">
    <label v-if="models.length" class="model-pick">
      新对话模型
      <select
        :value="modelSlug ?? ''"
        @change="$emit('update:modelSlug', ($event.target as HTMLSelectElement).value || null)"
      >
        <option v-for="m in models.filter((x) => x.enabled)" :key="m.id" :value="m.slug">
          {{ m.displayName }}{{ m.isDefault ? '（默认）' : '' }}
        </option>
      </select>
    </label>
    <button class="new" @click="$emit('create')">+ 新对话</button>
    <ul>
      <li
        v-for="s in sessions"
        :key="s.id"
        :class="{ active: s.id === activeId }"
        @click="$emit('select', s.id)"
      >
        <div class="info">
          <span class="title">{{ s.title }}</span>
          <span v-if="s.modelRef" class="model">{{ s.modelRef }}</span>
        </div>
        <button class="del" @click.stop="$emit('delete', s.id)">×</button>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.panel {
  flex: 1;
  overflow: auto;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.model-pick {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: #8b949e;
}

select {
  background: #1a1f2b;
  border: 1px solid #30363d;
  border-radius: 8px;
  color: #e8eaed;
  padding: 8px 10px;
}

.new {
  background: #ff6b35;
  color: #fff;
  width: 100%;
}

ul {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #1a1f2b;
  cursor: pointer;
}

li.active {
  background: #252b3a;
  outline: 1px solid #ff6b35;
}

.info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.model {
  font-size: 10px;
  color: #8b949e;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.del {
  background: transparent;
  color: #8b949e;
  padding: 0 6px;
  font-size: 18px;
}
</style>
