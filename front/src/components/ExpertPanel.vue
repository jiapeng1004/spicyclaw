<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, type AgentExpert, type LlmModel } from '../api/client'

defineProps<{
  models: LlmModel[]
}>()

const agents = ref<AgentExpert[]>([])
const loading = ref(true)
const error = ref('')

onMounted(async () => {
  try {
    agents.value = await api.listAgents()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
})

function modelLabel(slug: string | null | undefined, models: LlmModel[]) {
  if (!slug) return '未配置'
  return models.find((m) => m.slug === slug)?.displayName ?? slug
}
</script>

<template>
  <div class="page">
    <div class="page-inner">
    <header class="page-head">
      <h1>专家管理</h1>
      <p>Agent 智能体：负责理解任务、规划步骤并调度技能与 MCP 连接器执行。</p>
    </header>

    <div v-if="loading" class="state">加载中…</div>
    <p v-else-if="error" class="state error">{{ error }}</p>

    <ul v-else>
      <li v-for="agent in agents" :key="agent.id">
        <div class="card-head">
          <div>
            <strong>{{ agent.name }}</strong>
            <span class="slug">{{ agent.id }}</span>
          </div>
          <span class="badge" :class="{ on: agent.enabled }">
            {{ agent.enabled ? '运行中' : '已停用' }}
          </span>
        </div>
        <p class="desc">{{ agent.description }}</p>
        <dl class="meta-grid">
          <div>
            <dt>默认模型</dt>
            <dd>{{ modelLabel(agent.defaultModelSlug, models) }}</dd>
          </div>
          <div>
            <dt>最大迭代</dt>
            <dd>{{ agent.maxIters }}</dd>
          </div>
        </dl>
        <details class="prompt">
          <summary>系统提示词</summary>
          <pre>{{ agent.sysPrompt }}</pre>
        </details>
      </li>
    </ul>

    <p v-if="!loading && !error && !agents.length" class="state">暂无 Agent 配置</p>

    <aside class="note">
      <strong>维度说明</strong>
      <ul>
        <li><b>专家</b>：Agent 智能体本体（本页）</li>
        <li><b>连接器</b>：MCP 外部工具与数据源</li>
        <li><b>技能</b>：SKILL.md 可复用能力模块</li>
      </ul>
    </aside>
    </div>
  </div>
</template>

<style scoped>
.page {
  flex: 1;
  width: 100%;
  overflow: auto;
  padding: 32px 48px;
  background: var(--wb-bg-elevated);
}

.page-inner {
  max-width: 920px;
  margin: 0 auto;
}

.page-head h1 {
  margin: 0;
  font-size: 20px;
}

.page-head p {
  margin: 6px 0 0;
  color: var(--wb-text-secondary);
  font-size: 14px;
  line-height: 1.6;
}

.state {
  color: var(--wb-text-secondary);
  padding: 24px 0;
}

.state.error {
  color: var(--wb-danger);
}

ul {
  list-style: none;
  margin: 20px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

li {
  background: var(--wb-bg);
  border: 1px solid var(--wb-border-subtle);
  border-radius: var(--wb-radius);
  padding: 16px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
}

.slug {
  display: block;
  margin-top: 2px;
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
  height: fit-content;
}

.badge.on {
  background: rgba(63, 185, 80, 0.15);
  color: var(--wb-success);
}

.desc {
  margin: 10px 0;
  font-size: 13px;
  color: var(--wb-text-secondary);
  line-height: 1.5;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin: 0 0 12px;
}

.meta-grid dt {
  font-size: 11px;
  color: var(--wb-text-muted);
  margin-bottom: 2px;
}

.meta-grid dd {
  margin: 0;
  font-size: 13px;
}

.prompt summary {
  cursor: pointer;
  font-size: 13px;
  color: var(--wb-link);
}

.prompt pre {
  margin: 10px 0 0;
  padding: 12px;
  background: var(--wb-bg-hover);
  border-radius: var(--wb-radius-sm);
  white-space: pre-wrap;
  word-break: break-word;
  font-size: 12px;
  line-height: 1.55;
  max-height: 240px;
  overflow: auto;
}

.note {
  margin-top: 24px;
  padding: 14px 16px;
  border-radius: var(--wb-radius);
  background: var(--wb-bg);
  border: 1px solid var(--wb-border-subtle);
  font-size: 13px;
  color: var(--wb-text-secondary);
}

.note strong {
  display: block;
  margin-bottom: 8px;
  color: var(--wb-text);
}

.note ul {
  margin: 0;
  padding-left: 18px;
  list-style: disc;
}

.note li {
  border: none;
  padding: 2px 0;
  background: transparent;
}
</style>
