<template>
  <div style="max-width: 360px; margin: 80px auto;">
    <h2>登录</h2>
    <div style="display: flex; flex-direction: column; gap: 12px;">
      <input v-model="email" type="email" placeholder="邮箱" />
      <input v-model="password" type="password" placeholder="密码" />
      <label style="display:flex; align-items:center; gap: 6px;">
        <input type="checkbox" v-model="remember" /> 记住我
      </label>
      <button :disabled="loading" @click="onSubmit">{{ loading ? '登录中...' : '登录' }}</button>
      <p v-if="errorMsg" style="color: #d93025;">{{ errorMsg }}</p>
    </div>
  </div>
  
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useAuthStore } from '../stores/useAuthStore'
import { useRouter, useRoute } from 'vue-router'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()

const email = ref('')
const password = ref('')
const loading = ref(false)
const remember = ref(false)
const errorMsg = ref('')

function validate(): string | null {
    if (!email.value) return '请输入邮箱'
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)) return '邮箱格式不正确'
    if (!password.value) return '请输入密码'
    return null
}

async function onSubmit() {
    errorMsg.value = ''
    const err = validate()
    if (err) { errorMsg.value = err; return }
    loading.value = true
    try {
        await auth.login({ email: email.value, password: password.value, remember: remember.value })
        const redirect = (route.query.redirect as string) || '/'
        router.replace(redirect)
    } catch (e: any) {
        errorMsg.value = e?.message || '登录失败'
    } finally {
        loading.value = false
    }
}
</script>

<style scoped>
input {
  padding: 8px 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
}
button {
  padding: 8px 10px;
}
</style>


