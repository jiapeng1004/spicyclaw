<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { api, type UsageOverview } from '../api/client'
import type { User } from '../api/client'

defineProps<{
  user: User
}>()

defineEmits<{ back: [] }>()

const nav = [
  { id: 'plans', label: '套餐管理', icon: 'grid' },
  { id: 'usage', label: '用量管理', icon: 'chart' },
  { id: 'growth', label: '成长计划', icon: 'trend' },
  { id: 'billing', label: '账单与发票', icon: 'receipt' },
  { id: 'api', label: 'API 管理', icon: 'api' },
  { id: 'account', label: '账号设置', icon: 'user' },
] as const

const activeNav = ref<'usage'>('usage')
const loading = ref(true)
const error = ref('')
const overview = ref<UsageOverview | null>(null)
const rangeDays = ref<3 | 7 | 30>(7)
const dateRange = ref('')

const records = computed(() => overview.value?.records ?? [])

const activityProgress = computed(() => {
  const pkg = overview.value?.activityPackage
  if (!pkg || pkg.totalPoints <= 0) return 0
  return Math.min(100, (pkg.usedPoints / pkg.totalPoints) * 100)
})

async function load() {
  loading.value = true
  error.value = ''
  try {
    overview.value = await api.usageOverview(rangeDays.value)
    updateDateRangeLabel()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function updateDateRangeLabel() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - (rangeDays.value - 1))
  const fmt = (d: Date) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`
  dateRange.value = `${fmt(start)} - ${fmt(end)}`
}

function setRange(days: 3 | 7 | 30) {
  rangeDays.value = days
}

function exportCsv() {
  const rows = records.value
  if (!rows.length) return
  const header = 'RequestID,积分消耗,User Prompt,模型,时间\n'
  const body = rows
    .map((r) =>
      [r.requestId, r.pointsConsumed, `"${r.userPrompt.replace(/"/g, '""')}"`, r.model, r.createdAt].join(','),
    )
    .join('\n')
  const blob = new Blob(['\ufeff' + header + body], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `spicyclaw-usage-${rangeDays.value}d.csv`
  a.click()
  URL.revokeObjectURL(url)
}

function truncateId(id: string) {
  return id.length > 12 ? `${id.slice(0, 12)}…` : id
}

watch(rangeDays, load)
onMounted(load)
</script>

<template>
  <div class="usage-shell">
    <aside class="usage-nav">
      <div class="brand-block">
        <div class="brand-logo">🦞</div>
        <div>
          <div class="brand-name">SpicyClaw</div>
          <div class="brand-edition">{{ user.displayName || user.username }} · 个人版</div>
        </div>
      </div>

      <nav class="nav-list">
        <button
          v-for="item in nav"
          :key="item.id"
          class="nav-item"
          :class="{ active: item.id === activeNav, disabled: item.id !== 'usage' }"
          :disabled="item.id !== 'usage'"
        >
          <span class="nav-icon" :data-icon="item.icon" />
          {{ item.label }}
        </button>
      </nav>

      <button class="back-workspace" @click="$emit('back')">
        ← 返回工作台
      </button>
    </aside>

    <main class="usage-main">
      <div v-if="loading" class="state">加载中…</div>
      <div v-else-if="error" class="state error">{{ error }}</div>
      <template v-else-if="overview">
        <section class="card">
          <header class="card-head">
            <div class="card-title">
              <h2>{{ overview.activityPackage.name }}</h2>
              <span v-if="overview.activityPackage.badge" class="badge purple">
                {{ overview.activityPackage.badge }}
              </span>
            </div>
            <button class="link-btn" type="button">详情 ↗</button>
          </header>
          <p class="desc">{{ overview.activityPackage.description }}</p>
          <div class="progress-wrap">
            <div class="progress">
              <div class="progress-fill" :style="{ width: `${activityProgress}%` }" />
            </div>
            <div class="progress-meta">
              <span>{{ overview.activityPackage.usedPoints }} / {{ overview.activityPackage.totalPoints }}</span>
              <span>{{ overview.activityPackage.remainingPoints }} 剩余</span>
            </div>
          </div>
        </section>

        <section class="card">
          <header class="card-head">
            <div class="card-title">
              <h2>{{ overview.addonPackage.name }}</h2>
              <span v-if="overview.addonPackage.badge" class="badge gold">
                {{ overview.addonPackage.badge }}
              </span>
            </div>
            <button v-if="overview.addonPackage.purchasable" class="buy-btn" type="button">
              立即购买
            </button>
          </header>
          <p class="desc">{{ overview.addonPackage.description }}</p>
          <p v-if="overview.addonPackage.hint" class="hint">{{ overview.addonPackage.hint }}</p>
          <div class="progress-wrap">
            <div class="progress empty">
              <div class="progress-fill" style="width: 0" />
            </div>
            <div class="progress-meta muted">
              <span>- / -</span>
              <span>- 剩余</span>
            </div>
          </div>
        </section>

        <section class="records">
          <header class="records-head">
            <div>
              <h2>用量明细</h2>
              <p class="records-sub">用量数据存在 2-3 小时的数据延迟</p>
            </div>
            <div class="records-tools">
              <div class="range-group">
                <button
                  type="button"
                  :class="{ active: rangeDays === 3 }"
                  @click="setRange(3)"
                >
                  3d
                </button>
                <button
                  type="button"
                  :class="{ active: rangeDays === 7 }"
                  @click="setRange(7)"
                >
                  7d
                </button>
                <button
                  type="button"
                  :class="{ active: rangeDays === 30 }"
                  @click="setRange(30)"
                >
                  30d
                </button>
              </div>
              <label class="date-range">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="18" rx="2" />
                  <line x1="16" y1="2" x2="16" y2="6" />
                  <line x1="8" y1="2" x2="8" y2="6" />
                  <line x1="3" y1="10" x2="21" y2="10" />
                </svg>
                <span>{{ dateRange }}</span>
              </label>
              <button class="export-btn" type="button" @click="exportCsv">
                <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="7 10 12 15 17 10" />
                  <line x1="12" y1="15" x2="12" y2="3" />
                </svg>
                导出
              </button>
            </div>
          </header>

          <div class="table-wrap">
            <table>
              <thead>
                <tr>
                  <th>RequestID</th>
                  <th>积分消耗</th>
                  <th>User Prompt</th>
                  <th>模型</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="row in records" :key="row.requestId">
                  <td class="mono" :title="row.requestId">{{ truncateId(row.requestId) }}</td>
                  <td>{{ row.pointsConsumed.toFixed(2) }}</td>
                  <td class="prompt">{{ row.userPrompt }}</td>
                  <td><span class="model-tag">{{ row.model }}</span></td>
                </tr>
                <tr v-if="!records.length">
                  <td colspan="4" class="empty-row">所选时间范围内暂无用量记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </section>
      </template>
    </main>
  </div>
</template>

<style scoped>
.usage-shell {
  --usage-accent: #6c5ce7;
  --usage-accent-hover: #5b4bd4;
  --usage-accent-soft: rgba(108, 92, 231, 0.16);
  --usage-gold: #f5a623;
  --usage-card: var(--wb-bg-elevated);
  --usage-page: var(--wb-bg);

  flex: 1;
  display: flex;
  min-width: 0;
  height: 100vh;
  background: var(--usage-page);
}

.usage-nav {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid var(--wb-border-subtle);
  background: var(--usage-card);
  display: flex;
  flex-direction: column;
  padding: 20px 12px 16px;
}

.brand-block {
  display: flex;
  gap: 10px;
  align-items: center;
  padding: 0 8px 20px;
}

.brand-logo {
  font-size: 28px;
}

.brand-name {
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.02em;
}

.brand-edition {
  margin-top: 2px;
  font-size: 11px;
  color: var(--wb-text-muted);
}

.nav-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
  flex: 1;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  padding: 10px 12px;
  border-radius: var(--wb-radius-sm);
  background: transparent;
  color: var(--wb-text-secondary);
  font-size: 14px;
  text-align: left;
}

.nav-item.active {
  background: var(--usage-accent-soft);
  color: var(--usage-accent);
  font-weight: 500;
}

.nav-item.disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.nav-icon {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  background: currentColor;
  opacity: 0.35;
}

.nav-item.active .nav-icon {
  opacity: 0.8;
}

.back-workspace {
  margin-top: 12px;
  background: transparent;
  color: var(--wb-text-muted);
  font-size: 13px;
  padding: 8px 12px;
  text-align: left;
}

.back-workspace:hover {
  color: var(--wb-text);
  background: var(--wb-bg-hover);
}

.usage-main {
  flex: 1;
  overflow: auto;
  padding: 28px 32px 40px;
  min-width: 0;
}

.state {
  color: var(--wb-text-secondary);
  padding: 40px 0;
}

.state.error {
  color: var(--wb-danger);
}

.card {
  background: var(--usage-card);
  border: 1px solid var(--wb-border-subtle);
  border-radius: var(--wb-radius-lg);
  padding: 20px 22px;
  margin-bottom: 16px;
}

.card-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 10px;
}

.card-title {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.card-title h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.badge {
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 11px;
  font-weight: 500;
}

.badge.purple {
  background: var(--usage-accent-soft);
  color: var(--usage-accent);
}

.badge.gold {
  background: rgba(245, 166, 35, 0.15);
  color: var(--usage-gold);
}

.link-btn {
  background: transparent;
  color: var(--wb-text-muted);
  font-size: 13px;
  padding: 4px 8px;
}

.buy-btn {
  background: var(--usage-accent);
  color: #fff;
  font-size: 13px;
  padding: 7px 14px;
  border-radius: var(--wb-radius-sm);
}

.buy-btn:hover {
  background: var(--usage-accent-hover);
}

.desc {
  margin: 0 0 14px;
  color: var(--wb-text-secondary);
  font-size: 13px;
  line-height: 1.6;
}

.hint {
  margin: -8px 0 14px;
  color: #e67e22;
  font-size: 12px;
}

.progress-wrap {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress {
  height: 8px;
  border-radius: 999px;
  background: var(--wb-bg-hover);
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: inherit;
  background: linear-gradient(90deg, var(--usage-accent), #8b7cf0);
}

.progress-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: var(--wb-text-secondary);
}

.progress-meta.muted {
  color: var(--wb-text-muted);
}

.records {
  margin-top: 8px;
}

.records-head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.records-head h2 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.records-sub {
  margin: 4px 0 0;
  font-size: 12px;
  color: var(--wb-text-muted);
}

.records-tools {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.range-group {
  display: flex;
  padding: 3px;
  background: var(--wb-bg-hover);
  border-radius: var(--wb-radius-sm);
  gap: 2px;
}

.range-group button {
  padding: 5px 12px;
  font-size: 12px;
  background: transparent;
  color: var(--wb-text-secondary);
  border-radius: 4px;
}

.range-group button.active {
  background: var(--usage-accent);
  color: #fff;
}

.date-range {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-sm);
  background: var(--usage-card);
  color: var(--wb-text-secondary);
  font-size: 12px;
}

.export-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: var(--usage-card);
  border: 1px solid var(--wb-border);
  color: var(--wb-text);
  font-size: 13px;
}

.table-wrap {
  border: 1px solid var(--wb-border-subtle);
  border-radius: var(--wb-radius-lg);
  overflow: auto;
  background: var(--usage-card);
}

table {
  width: 100%;
  border-collapse: collapse;
  min-width: 720px;
}

th,
td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid var(--wb-border-subtle);
  font-size: 13px;
}

th {
  color: var(--wb-text-muted);
  font-weight: 500;
  background: var(--wb-bg-hover);
}

tbody tr:last-child td {
  border-bottom: none;
}

.mono {
  font-family: ui-monospace, SFMono-Regular, Menlo, Consolas, monospace;
  color: var(--wb-text-secondary);
}

.prompt {
  max-width: 420px;
  line-height: 1.5;
}

.model-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--wb-bg-hover);
  font-size: 12px;
}

.empty-row {
  text-align: center;
  color: var(--wb-text-muted);
  padding: 32px 16px !important;
}
</style>
