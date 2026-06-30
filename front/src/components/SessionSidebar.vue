<script setup lang="ts">
import { computed, ref } from 'vue'
import type { Session } from '../api/client'

const props = defineProps<{
  sessions: Session[]
  activeId: string | null
}>()

defineEmits<{
  select: [id: string]
  delete: [id: string]
}>()

const query = ref('')
const spaceOpen = ref(true)

const filtered = computed(() => {
  const q = query.value.trim().toLowerCase()
  if (!q) return props.sessions
  return props.sessions.filter((s) => s.title.toLowerCase().includes(q))
})
</script>

<template>
  <div class="space">
    <button class="space-head" @click="spaceOpen = !spaceOpen">
      <svg
        class="chevron"
        :class="{ open: spaceOpen }"
        width="12"
        height="12"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <polyline points="9 18 15 12 9 6" />
      </svg>
      <span>空间 ({{ sessions.length }})</span>
    </button>

    <div v-if="spaceOpen" class="space-body">
      <ul v-if="filtered.length">
        <li
          v-for="s in filtered"
          :key="s.id"
          :class="{ active: s.id === activeId }"
          @click="$emit('select', s.id)"
        >
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
          </svg>
          <span class="title">{{ s.title }}</span>
          <button class="del" title="删除" @click.stop="$emit('delete', s.id)">×</button>
        </li>
      </ul>
      <p v-else class="empty">暂无任务</p>
    </div>
  </div>
</template>

<style scoped>
.space {
  flex: 1;
  overflow: auto;
  min-height: 0;
  padding: 0 4px;
}

.space-head {
  display: flex;
  align-items: center;
  gap: 4px;
  width: 100%;
  padding: 6px 8px;
  background: transparent;
  color: var(--wb-text-secondary);
  font-size: 12px;
  font-weight: 500;
}

.chevron {
  transition: transform 0.15s;
}

.chevron.open {
  transform: rotate(90deg);
}

.space-body ul {
  list-style: none;
  margin: 0;
  padding: 0 0 0 8px;
}

.space-body li {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: var(--wb-radius-sm);
  cursor: pointer;
  font-size: 13px;
  color: var(--wb-text);
}

.space-body li:hover {
  background: var(--wb-bg-hover);
}

.space-body li.active {
  background: var(--wb-bg-active);
  font-weight: 500;
}

.space-body li svg {
  color: var(--wb-text-muted);
  flex-shrink: 0;
}

.title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.del {
  opacity: 0;
  background: transparent;
  color: var(--wb-text-muted);
  padding: 0 4px;
  font-size: 16px;
  line-height: 1;
}

li:hover .del {
  opacity: 1;
}

.empty {
  margin: 8px 0 0 18px;
  font-size: 12px;
  color: var(--wb-text-muted);
}
</style>
