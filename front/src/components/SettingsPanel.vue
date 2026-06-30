<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { applyTheme, getTheme, type ThemeMode } from '../composables/useTheme'

defineEmits<{ 'open-usage': [] }>()

const theme = ref<ThemeMode>('light')

onMounted(() => {
  theme.value = getTheme()
})

function setTheme(mode: ThemeMode) {
  theme.value = mode
  applyTheme(mode)
}
</script>

<template>
  <div class="menu" @click.stop>
    <div class="menu-head">设置</div>

    <nav class="menu-list">
      <button class="item" @click="$emit('open-usage')">
        <span>积分余额</span>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>
      <button class="item" disabled>
        <span>成长计划</span>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>
      <button class="item" disabled>
        <span>帮助与反馈</span>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>
      <button class="item" disabled>
        <span>检查更新</span>
        <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <polyline points="9 18 15 12 9 6" />
        </svg>
      </button>

      <div class="item appearance">
        <span>外观</span>
        <div class="segment" role="group" aria-label="外观">
          <button type="button" :class="{ active: theme === 'light' }" @click="setTheme('light')">
            浅色
          </button>
          <button type="button" :class="{ active: theme === 'dark' }" @click="setTheme('dark')">
            深色
          </button>
        </div>
      </div>
    </nav>
  </div>
</template>

<style scoped>
.menu {
  position: absolute;
  left: 8px;
  right: 8px;
  bottom: calc(100% + 8px);
  z-index: 200;
  background: var(--wb-bg-elevated);
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-lg);
  box-shadow: var(--wb-shadow-lg);
  overflow: hidden;
}

.menu-head {
  padding: 12px 14px 8px;
  font-size: 13px;
  font-weight: 600;
  color: var(--wb-text);
}

.menu-list {
  display: flex;
  flex-direction: column;
  padding-bottom: 6px;
}

.item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  padding: 10px 14px;
  background: transparent;
  color: var(--wb-text);
  font-size: 13px;
  text-align: left;
  border-radius: 0;
}

.item:not(:disabled):not(.appearance):hover {
  background: var(--wb-bg-hover);
}

.item:disabled {
  opacity: 1;
  cursor: default;
}

.item svg {
  color: var(--wb-text-muted);
  flex-shrink: 0;
}

.appearance {
  cursor: default;
  padding-top: 12px;
  border-top: 1px solid var(--wb-border-subtle);
  margin-top: 4px;
}

.segment {
  display: flex;
  padding: 3px;
  background: var(--wb-segment-bg);
  border-radius: 999px;
  gap: 2px;
  flex-shrink: 0;
}

.segment button {
  padding: 4px 12px;
  font-size: 12px;
  border-radius: 999px;
  background: transparent;
  color: var(--wb-text-secondary);
  min-width: 48px;
}

.segment button.active {
  background: var(--wb-segment-active-bg);
  color: var(--wb-text);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}
</style>
