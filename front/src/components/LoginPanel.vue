<script setup lang="ts">
import { ref } from 'vue'
import { api, type User } from '../api/client'

const emit = defineEmits<{
  success: [user: User]
}>()

const username = ref('admin')
const password = ref('spicyclaw')
const error = ref('')
const loading = ref(false)

async function submit() {
  error.value = ''
  loading.value = true
  try {
    const user = await api.login(username.value, password.value)
    emit('success', user)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '登录失败'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="login">
    <form class="card" @submit.prevent="submit">
      <h2>登录 SpicyClaw</h2>
      <p class="hint">使用管理员账号访问 Agent 与技能管理</p>
      <label>
        用户名
        <input v-model="username" autocomplete="username" required />
      </label>
      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" required />
      </label>
      <p v-if="error" class="error">{{ error }}</p>
      <button type="submit" :disabled="loading">{{ loading ? '登录中…' : '登录' }}</button>
    </form>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: #0d1117;
}

.card {
  width: min(360px, 92vw);
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 24px;
  background: #151922;
  border: 1px solid #252a36;
  border-radius: 12px;
}

h2 {
  margin: 0;
}

.hint {
  margin: 0;
  color: #8b949e;
  font-size: 13px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #c9d1d9;
}

input {
  padding: 10px 12px;
  border-radius: 8px;
  border: 1px solid #30363d;
  background: #0d1117;
  color: #f0f6fc;
}

button {
  margin-top: 4px;
  background: #ff6b35;
  color: #fff;
}

.error {
  margin: 0;
  color: #ff7b72;
  font-size: 13px;
}
</style>
