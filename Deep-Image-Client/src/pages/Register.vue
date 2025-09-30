<template>
  <div style="max-width: 420px; margin: 80px auto;">
    <h2>注册</h2>
    <div style="display:flex; flex-direction:column; gap:12px;">
      <input v-model="username" type="text" placeholder="用户名" />
      <input v-model="email" type="email" placeholder="邮箱" />
      <input v-model="password" type="password" placeholder="密码" />
      <input v-model="confirmPassword" type="password" placeholder="确认密码" />
      <button :disabled="loading" @click="onSubmit">{{ loading ? '提交中...' : '注册' }}</button>
      <p v-if="errorMsg" style="color:#d93025;">{{ errorMsg }}</p>
      <p v-if="success" style="color:#107c10;">注册成功，请前往登录</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { register as registerApi } from '../api/auth'
import type { RegisterRequest } from '../types/auth'
import { useRouter } from 'vue-router'

const router = useRouter()
const email = ref('')
const username = ref('')
const password = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')
const success = ref(false)

function validate(): string | null {
  if (!username.value) return '请输入用户名'
  if (!email.value) return '请输入邮箱'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) return '邮箱格式不正确'
  if (!password.value) return '请输入密码'
  if (password.value.length < 8) return '密码不少于8位'
  if (!/(?=.*[A-Za-z])(?=.*\d).+/.test(password.value)) return '密码需包含字母和数字'
  if (password.value !== confirmPassword.value) return '两次输入的密码不一致'
  return null
}

async function onSubmit() {
  errorMsg.value = ''
  success.value = false
  const err = validate()
  if (err) { errorMsg.value = err; return }
  loading.value = true
  try {
    const payload: RegisterRequest = { email: email.value, username: username.value, password: password.value }
    const { data } = await registerApi(payload)
    if (data.code !== 200) throw new Error(data.message)
    success.value = true
    setTimeout(() => router.replace({ name: 'login' }), 800)
  } catch (e: any) {
    errorMsg.value = e?.message || '注册失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; }
button { padding: 8px 10px; }
</style>


