<script setup lang="ts">
import { ref } from 'vue'

defineProps<{
  sessionTitle?: string
}>()

const tab = ref<'output' | 'files' | 'changes'>('output')
</script>

<template>
  <aside class="detail">
    <header class="detail-head">
      <h2>任务详情</h2>
      <p v-if="sessionTitle" class="session">{{ sessionTitle }}</p>
    </header>
    <nav class="detail-tabs">
      <button :class="{ active: tab === 'output' }" @click="tab = 'output'">产物</button>
      <button :class="{ active: tab === 'files' }" @click="tab = 'files'">文件</button>
      <button :class="{ active: tab === 'changes' }" @click="tab = 'changes'">变更</button>
    </nav>
    <div class="detail-body">
      <div v-if="tab === 'output'" class="placeholder">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
          <polyline points="14 2 14 8 20 8" />
        </svg>
        <p>任务执行完成后，生成的文档、表格等产物将在此展示</p>
      </div>
      <div v-else-if="tab === 'files'" class="placeholder">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M22 19a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h5l2 3h9a2 2 0 0 1 2 2z" />
        </svg>
        <p>工作空间文件列表将在此显示，支持预览与下载</p>
      </div>
      <div v-else class="placeholder">
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <polyline points="16 18 22 12 16 6" />
          <polyline points="8 6 2 12 8 18" />
        </svg>
        <p>对话过程中的文件变更记录将在此追踪</p>
      </div>
    </div>
  </aside>
</template>

<style scoped>
.detail {
  width: var(--wb-detail-width);
  border-left: 1px solid var(--wb-border-subtle);
  background: var(--wb-bg-elevated);
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.detail-head {
  padding: 16px 16px 12px;
  border-bottom: 1px solid var(--wb-border-subtle);
}

.detail-head h2 {
  margin: 0;
  font-size: 14px;
  font-weight: 600;
}

.session {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--wb-text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-tabs {
  display: flex;
  gap: 4px;
  padding: 8px 12px;
  border-bottom: 1px solid var(--wb-border-subtle);
}

.detail-tabs button {
  flex: 1;
  padding: 6px 8px;
  font-size: 12px;
  background: transparent;
  color: var(--wb-text-secondary);
}

.detail-tabs button.active {
  background: var(--wb-accent-soft);
  color: var(--wb-accent);
}

.detail-body {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.placeholder {
  height: 100%;
  min-height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  text-align: center;
  color: var(--wb-text-muted);
}

.placeholder svg {
  opacity: 0.5;
}

.placeholder p {
  margin: 0;
  font-size: 13px;
  line-height: 1.6;
  max-width: 220px;
}
</style>
