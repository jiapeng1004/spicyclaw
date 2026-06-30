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
      <div class="brand">
        <span class="logo">🦞</span>
        <div>
          <h1>SpicyClaw</h1>
          <p>你的职场超能力</p>
        </div>
      </div>

      <div class="fields">
        <label>
          <span>用户名</span>
          <input v-model="username" autocomplete="username" required />
        </label>
        <label>
          <span>密码</span>
          <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            required
            @keydown.enter="submit"
          />
        </label>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <button type="submit" class="submit" :disabled="loading">
        {{ loading ? '登录中…' : '进入工作台' }}
      </button>
    </form>
  </div>
</template>

<style scoped>
.login {
  min-height: 100vh;
  display: grid;
  place-items: center;
  background: var(--wb-bg);
}

.card {
  width: min(400px, 92vw);
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 36px;
  background: var(--wb-bg-elevated);
  border: 1px solid var(--wb-border);
  border-radius: var(--wb-radius-xl);
  box-shadow: var(--wb-shadow-lg);
}

.brand {
  display: flex;
  align-items: center;
  gap: 14px;
}

.logo {
  font-size: 40px;
}

.brand h1 {
  margin: 0;
  font-size: 24px;
  font-weight: 700;
}

.brand p {
  margin: 4px 0 0;
  color: var(--wb-text-secondary);
  font-size: 14px;
}

.fields {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

label {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

label span {
  font-size: 12px;
  color: var(--wb-text-secondary);
}

input {
  padding: 10px 12px;
  border-radius: var(--wb-radius-sm);
  border: 1px solid var(--wb-border);
  background: var(--wb-bg-elevated);
  color: var(--wb-text);
}

input:focus {
  outline: none;
  border-color: var(--wb-accent);
}

.submit {
  background: var(--wb-accent);
  color: #fff;
  padding: 11px 16px;
  font-weight: 500;
  border-radius: var(--wb-radius-sm);
}

.submit:hover:not(:disabled) {
  background: var(--wb-accent-hover);
}

.error {
  margin: 0;
  color: var(--wb-danger);
  font-size: 13px;
}
</style>
