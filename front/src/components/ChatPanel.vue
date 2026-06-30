<script setup lang="ts">
import { ref, nextTick } from 'vue'
import { api, type Message } from '../api/client'

const props = defineProps<{
  sessionId: string
  messages: Message[]
}>()

const emit = defineEmits<{ sent: [] }>()

const input = ref('')
const streaming = ref(false)
const streamText = ref('')
const listRef = ref<HTMLElement | null>(null)

async function send() {
  const text = input.value.trim()
  if (!text || streaming.value) return
  input.value = ''
  streaming.value = true
  streamText.value = ''
  await nextTick()
  scrollBottom()

  try {
    for await (const chunk of api.streamMessage(props.sessionId, text)) {
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
</script>

<template>
  <div class="chat">
    <div ref="listRef" class="messages">
      <div
        v-for="m in messages"
        :key="m.id"
        class="msg"
        :class="m.role"
      >
        <div class="role">{{ m.role === 'user' ? '你' : 'Claw' }}</div>
        <pre>{{ m.content }}</pre>
      </div>
      <div v-if="streaming" class="msg assistant">
        <div class="role">Claw</div>
        <pre>{{ streamText || '思考中…' }}</pre>
      </div>
    </div>
    <div class="composer">
      <textarea
        v-model="input"
        rows="3"
        placeholder="输入任务，支持 /skill 风格指令…"
        :disabled="streaming"
        @keydown="onKeydown"
      />
      <button :disabled="streaming || !input.trim()" @click="send">
        {{ streaming ? '生成中…' : '发送' }}
      </button>
    </div>
  </div>
</template>

<style scoped>
.chat {
  display: flex;
  flex-direction: column;
  height: 100vh;
}

.messages {
  flex: 1;
  overflow: auto;
  padding: 24px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.msg {
  max-width: 820px;
  width: 100%;
  margin: 0 auto;
}

.msg.user pre {
  background: #1f2430;
}

.msg.assistant pre {
  background: #182033;
  border-left: 3px solid #ff6b35;
}

.role {
  font-size: 12px;
  color: #8b949e;
  margin-bottom: 6px;
}

pre {
  margin: 0;
  padding: 14px 16px;
  border-radius: 12px;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.composer {
  border-top: 1px solid #252a36;
  padding: 16px 24px 24px;
  display: grid;
  grid-template-columns: 1fr auto;
  gap: 12px;
  align-items: end;
}

textarea {
  width: 100%;
  resize: vertical;
  min-height: 72px;
  background: #151922;
  color: #e8eaed;
  border: 1px solid #30363d;
  border-radius: 12px;
  padding: 12px 14px;
}

button {
  background: #ff6b35;
  color: #fff;
  height: 44px;
  min-width: 88px;
}

button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
