<template>
  <div style="max-width: 420px; margin: 80px auto;">
    <h2>重置密码</h2>
    <div style="display:flex; flex-direction:column; gap:12px;">
      <input v-model="email" type="email" placeholder="邮箱" />
      <input v-model="oldPassword" type="password" placeholder="旧密码" />
      <input v-model="newPassword" type="password" placeholder="新密码" />
      <input v-model="confirmPassword" type="password" placeholder="确认新密码" />
      <button :disabled="loading" @click="onSubmit">{{ loading ? '提交中...' : '重置密码' }}</button>
      <p v-if="errorMsg" style="color:#d93025;">{{ errorMsg }}</p>
      <p v-if="success" style="color:#107c10;">重置成功，请使用新密码登录</p>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { resetPassword as resetPasswordApi } from '../api/auth'
import type { ResetPasswordRequest } from '../types/auth'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/useAuthStore'

const router = useRouter()
const auth = useAuthStore()
const email = ref('')
const oldPassword = ref('')
const newPassword = ref('')
const confirmPassword = ref('')
const loading = ref(false)
const errorMsg = ref('')
const success = ref(false)

function validate(): string | null {
  if (!email.value) return '请输入邮箱'
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) return '邮箱格式不正确'
  if (!oldPassword.value) return '请输入旧密码'
  if (!newPassword.value) return '请输入新密码'
  if (newPassword.value.length < 8) return '密码不少于8位'
  if (!/(?=.*[A-Za-z])(?=.*\d).+/.test(newPassword.value)) return '密码需包含字母和数字'
  if (newPassword.value !== confirmPassword.value) return '两次输入的密码不一致'
  return null
}

async function onSubmit() {
  errorMsg.value = ''
  success.value = false
  const err = validate()
  if (err) { errorMsg.value = err; return }
  loading.value = true
  try {
    const payload: ResetPasswordRequest = { email: email.value, oldPassword: oldPassword.value, newPassword: newPassword.value }
    const { data } = await resetPasswordApi(payload)
    if (data.code !== 200) throw new Error(data.message)
    success.value = true
    await auth.logout()
    router.replace({ name: 'login' })
  } catch (e: any) {
    errorMsg.value = e?.message || '重置密码失败'
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
input { padding: 8px 10px; border: 1px solid #ddd; border-radius: 4px; }
button { padding: 8px 10px; }
</style>


