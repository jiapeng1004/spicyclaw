<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { api, type McpConnector } from '../api/client'

const connectors = ref<McpConnector[]>([])
const loading = ref(true)
const error = ref('')
const saving = ref(false)

const name = ref('')
const transport = ref('stdio')
const endpoint = ref('')
const description = ref('')

async function load() {
  loading.value = true
  error.value = ''
  try {
    connectors.value = await api.listMcpConnectors()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

async function register() {
  if (!name.value.trim() || !endpoint.value.trim()) return
  saving.value = true
  error.value = ''
  try {
    await api.registerMcpConnector({
      name: name.value.trim(),
      transport: transport.value,
      endpoint: endpoint.value.trim(),
      description: description.value.trim(),
    })
    name.value = ''
    endpoint.value = ''
    description.value = ''
    await load()
  } catch (e) {
    error.value = e instanceof Error ? e.message : '注册失败'
  } finally {
    saving.value = false
  }
}

async function remove(id: string) {
  await api.deleteMcpConnector(id)
  await load()
}

onMounted(load)
</script>

<template>
  <div class="page">
    <div class="page-inner">
    <header class="page-head">
      <h1>连接器</h1>
      <p>MCP（Model Context Protocol）连接器：接入外部工具、数据库与 SaaS，供 Agent 调用。</p>
    </header>

    <form class="register" @submit.prevent="register">
      <div class="row">
        <label>
          名称
          <input v-model="name" placeholder="例如 filesystem" :disabled="saving" />
        </label>
        <label>
          传输
          <select v-model="transport" :disabled="saving">
            <option value="stdio">stdio</option>
            <option value="sse">sse</option>
            <option value="http">http</option>
          </select>
        </label>
      </div>
      <label>
        端点 / 启动命令
        <input v-model="endpoint" placeholder="npx @modelcontextprotocol/server-filesystem /path" :disabled="saving" />
      </label>
      <label>
        描述（可选）
        <input v-model="description" placeholder="本地文件系统读写" :disabled="saving" />
      </label>
      <button type="submit" :disabled="saving || !name.trim() || !endpoint.trim()">
        {{ saving ? '注册中…' : '注册 MCP 连接器' }}
      </button>
    </form>

    <p v-if="error" class="error">{{ error }}</p>

    <div v-if="loading" class="state">加载中…</div>
    <ul v-else>
      <li v-for="c in connectors" :key="c.id">
        <div class="card-head">
          <div>
            <strong>{{ c.name }}</strong>
            <span class="slug">{{ c.transport }} · {{ c.endpoint }}</span>
          </div>
          <span class="badge">{{ c.status }}</span>
        </div>
        <p v-if="c.description" class="desc">{{ c.description }}</p>
        <div class="actions">
          <span class="tag">MCP</span>
          <button class="secondary" @click="remove(c.id)">移除</button>
        </div>
      </li>
    </ul>
    <p v-if="!loading && !connectors.length" class="state">
      暂无 MCP 连接器，注册后 Agent 可通过 MCP 协议调用外部能力。
    </p>
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

.register {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 16px;
  border: 1px solid var(--wb-border-subtle);
  border-radius: var(--wb-radius);
  background: var(--wb-bg);
}

.row {
  display: grid;
  grid-template-columns: 1fr 140px;
  gap: 12px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 12px;
  color: var(--wb-text-secondary);
}

input,
select {
  padding: 10px 12px;
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-sm);
  background: var(--wb-bg-elevated);
  color: var(--wb-text);
}

button {
  align-self: flex-start;
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
}

.state {
  color: var(--wb-text-muted);
  padding: 24px 0;
  font-size: 13px;
}

ul {
  list-style: none;
  margin: 16px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

li {
  padding: 14px;
  border: 1px solid var(--wb-border-subtle);
  border-radius: var(--wb-radius);
  background: var(--wb-bg);
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
  word-break: break-all;
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

.desc {
  margin: 8px 0 0;
  font-size: 13px;
  color: var(--wb-text-secondary);
}

.actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;
}

.tag {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 4px;
  background: var(--wb-accent-soft);
  color: var(--wb-accent);
}
</style>
