<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <div class="auth-container">
    <div class="auth-card-wrapper">
      <a-card class="auth-card" :bordered="false">
        <!-- Logo 和标题 -->
        <div class="logo-section">
          <img src="@/assets/logo.svg" alt="logo" class="logo" />
          <h1 class="title">DeepImage</h1>
          <p class="subtitle">AI 图像处理平台</p>
        </div>

        <!-- Tabs 切换 -->
        <a-tabs
          v-model:activeKey="activeTab"
          centered
          size="large"
          :animated="{ inkBar: true, tabPane: true }"
        >
          <!-- 登录 Tab -->
          <a-tab-pane key="login" tab="登录">
            <!-- Google登录按钮 -->
            <a-button
              class="google-login-btn"
              size="large"
              block
              @click="handleGoogleLogin"
            >
              <template #icon>
                <img src="@/assets/google.svg" alt="Google" class="google-icon" />
              </template>
              使用 Google 账号登录
            </a-button>

            <!-- 分隔线 -->
            <a-divider>或使用邮箱密码登录</a-divider>

            <a-form
              ref="loginFormRef"
              :model="loginForm"
              :rules="loginRules"
              layout="vertical"
              @finish="onLogin"
            >
              <a-form-item label="邮箱" name="email">
                <a-input
                  v-model:value="loginForm.email"
                  placeholder="请输入邮箱"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <MailOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item label="密码" name="password">
                <a-input-password
                  v-model:value="loginForm.password"
                  placeholder="请输入密码"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item>
                <div style="display: flex; justify-content: space-between; align-items: center">
                  <a-checkbox v-model:checked="loginForm.remember">记住我</a-checkbox>
                  <a class="forgot-link" @click="activeTab = 'reset'">忘记密码？</a>
                </div>
              </a-form-item>

              <a-form-item>
                <a-button type="primary" html-type="submit" block size="large" :loading="loading">
                  登录
                </a-button>
              </a-form-item>
            </a-form>
          </a-tab-pane>

          <!-- 注册 Tab -->
          <a-tab-pane key="register" tab="注册">
            <a-form
              ref="registerFormRef"
              :model="registerForm"
              :rules="registerRules"
              layout="vertical"
              @finish="onRegister"
            >
              <a-form-item label="用户名" name="username">
                <a-input
                  v-model:value="registerForm.username"
                  placeholder="请输入用户名"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <UserOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item label="邮箱" name="email">
                <a-input
                  v-model:value="registerForm.email"
                  placeholder="请输入邮箱"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <MailOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item label="密码" name="password">
                <a-input-password
                  v-model:value="registerForm.password"
                  placeholder="请输入密码（至少8位，包含字母和数字）"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item label="确认密码" name="confirmPassword">
                <a-input-password
                  v-model:value="registerForm.confirmPassword"
                  placeholder="请再次输入密码"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item>
                <a-button type="primary" html-type="submit" block size="large" :loading="loading">
                  注册
                </a-button>
              </a-form-item>
            </a-form>
          </a-tab-pane>

          <!-- 重置密码 Tab -->
          <a-tab-pane key="reset" tab="重置密码">
            <a-form
              ref="resetFormRef"
              :model="resetForm"
              :rules="resetRules"
              layout="vertical"
              @finish="onResetPassword"
            >
              <a-form-item label="邮箱" name="email">
                <a-input
                  v-model:value="resetForm.email"
                  placeholder="请输入邮箱"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <MailOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input>
              </a-form-item>

              <a-form-item label="旧密码" name="oldPassword">
                <a-input-password
                  v-model:value="resetForm.oldPassword"
                  placeholder="请输入旧密码"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item label="新密码" name="newPassword">
                <a-input-password
                  v-model:value="resetForm.newPassword"
                  placeholder="请输入新密码（至少8位，包含字母和数字）"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item label="确认新密码" name="confirmPassword">
                <a-input-password
                  v-model:value="resetForm.confirmPassword"
                  placeholder="请再次输入新密码"
                  size="large"
                  allow-clear
                >
                  <template #prefix>
                    <LockOutlined style="color: rgba(0, 0, 0, 0.25)" />
                  </template>
                </a-input-password>
              </a-form-item>

              <a-form-item>
                <a-button type="primary" html-type="submit" block size="large" :loading="loading">
                  重置密码
                </a-button>
              </a-form-item>
            </a-form>
          </a-tab-pane>
        </a-tabs>
      </a-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { message } from 'ant-design-vue'
import { MailOutlined, LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '../stores/useAuthStore'
import { useUserStore } from '../stores/useUserStore'
import { register as registerApi, resetPassword as resetPasswordApi } from '../api/auth'
import type { LoginRequest, RegisterRequest, ResetPasswordRequest } from '../types/auth'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

// 当前激活的 Tab
const activeTab = ref((route.query.tab as string) || 'login')

// 监听 tab 变化，更新 URL（保持 redirect 参数）
watch(activeTab, (newTab, oldTab) => {
  // 只有在用户真正切换 tab 时才更新 URL
  if (oldTab !== undefined) {
    const newQuery = { ...route.query, tab: newTab }
    router.replace({ name: 'auth', query: newQuery })
  }
})

// 加载状态
const loading = ref(false)

// 登录表单
const loginFormRef = ref()
const loginForm = reactive({
  email: '',
  password: '',
  remember: false,
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// 注册表单
const registerFormRef = ref()
const registerForm = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const validatePassword = (_rule: any, value: string) => {
  if (!value) {
    return Promise.reject('请输入密码')
  }
  if (value.length < 8) {
    return Promise.reject('密码不少于8位')
  }
  if (!/(?=.*[A-Za-z])(?=.*\d)/.test(value)) {
    return Promise.reject('密码需包含字母和数字')
  }
  return Promise.resolve()
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const validateConfirmPassword = (_rule: any, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入密码')
  }
  if (value !== registerForm.password) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const registerRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  password: [{ required: true, validator: validatePassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateConfirmPassword, trigger: 'blur' }],
}

// 重置密码表单
const resetFormRef = ref()
const resetForm = reactive({
  email: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const validateNewPassword = (_rule: any, value: string) => {
  if (!value) {
    return Promise.reject('请输入新密码')
  }
  if (value.length < 8) {
    return Promise.reject('密码不少于8位')
  }
  if (!/(?=.*[A-Za-z])(?=.*\d)/.test(value)) {
    return Promise.reject('密码需包含字母和数字')
  }
  return Promise.resolve()
}

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const validateResetConfirmPassword = (_rule: any, value: string) => {
  if (!value) {
    return Promise.reject('请再次输入新密码')
  }
  if (value !== resetForm.newPassword) {
    return Promise.reject('两次输入的密码不一致')
  }
  return Promise.resolve()
}

const resetRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' },
  ],
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [{ required: true, validator: validateNewPassword, trigger: 'blur' }],
  confirmPassword: [{ required: true, validator: validateResetConfirmPassword, trigger: 'blur' }],
}

// 登录
async function onLogin() {
  loading.value = true
  try {
    const payload: LoginRequest & { remember?: boolean } = {
      email: loginForm.email,
      password: loginForm.password,
      remember: loginForm.remember,
    }
    await authStore.login(payload)
    message.success('登录成功！')
    
    // 获取用户完整信息
    const userStore = useUserStore()
    try {
      await userStore.fetchProfile()
    } catch (err) {
      console.warn('Failed to fetch user profile:', err)
    }
    
    // 获取重定向地址并跳转
    const redirect = (route.query.redirect as string) || '/'
    await router.push(redirect)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (e: any) {
    message.error(e?.message || '登录失败')
  } finally {
    loading.value = false
  }
}

// 注册
async function onRegister() {
  loading.value = true
  try {
    const payload: RegisterRequest = {
      username: registerForm.username,
      email: registerForm.email,
      password: registerForm.password,
    }
    const { data } = await registerApi(payload)
    if (data.code !== 200) throw new Error(data.message)

    message.success('注册成功！即将跳转到登录...')

    // 自动填充邮箱到登录表单
    loginForm.email = registerForm.email

    setTimeout(() => {
      activeTab.value = 'login'
    }, 800)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (e: any) {
    message.error(e?.message || '注册失败')
  } finally {
    loading.value = false
  }
}

// 重置密码
async function onResetPassword() {
  loading.value = true
  try {
    const payload: ResetPasswordRequest = {
      email: resetForm.email,
      oldPassword: resetForm.oldPassword,
      newPassword: resetForm.newPassword,
    }
    const { data } = await resetPasswordApi(payload)
    if (data.code !== 200) throw new Error(data.message)

    message.success('密码重置成功！请使用新密码登录')

    await authStore.logout()

    // 自动填充邮箱到登录表单
    loginForm.email = resetForm.email

    setTimeout(() => {
      activeTab.value = 'login'
    }, 800)
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (e: any) {
    message.error(e?.message || '重置密码失败')
  } finally {
    loading.value = false
  }
}

// Google登录
function handleGoogleLogin() {
  // 获取 redirect 参数
  const redirect = (route.query.redirect as string) || ''
  
  // 构建回调URL，将 redirect 参数传递给回调页面
  let callbackUrl = `${window.location.origin}/auth/callback`
  if (redirect) {
    callbackUrl += `?redirect=${encodeURIComponent(redirect)}`
  }
  
  // 跳转到后端OAuth发起接口
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const loginUrl = `${apiBaseUrl}/api/auth/google/login?fromUrl=${encodeURIComponent(callbackUrl)}`
  
  // 直接跳转（浏览器会重定向到Google）
  window.location.href = loginUrl
}
</script>

<style scoped>
.auth-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.auth-card-wrapper {
  animation: slideUp 0.5s ease-out;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.auth-card {
  width: 100%;
  max-width: 480px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  border-radius: 12px;
  overflow: hidden;
  backdrop-filter: blur(10px);
}

.logo-section {
  text-align: center;
  margin-bottom: 32px;
  animation: fadeIn 0.6s ease-out 0.2s both;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.logo {
  width: 72px;
  height: 72px;
  margin-bottom: 16px;
  animation: rotate 20s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.title {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.forgot-link {
  font-size: 14px;
  transition: color 0.3s;
}

.forgot-link:hover {
  color: #667eea;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 24px;
}

:deep(.ant-tabs-tab) {
  font-size: 16px;
  padding: 12px 20px;
  transition: all 0.3s;
}

:deep(.ant-tabs-tab:hover) {
  color: #667eea;
}

:deep(.ant-tabs-tab-active) {
  font-weight: 600;
}

:deep(.ant-tabs-ink-bar) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  height: 3px;
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-input-affix-wrapper),
:deep(.ant-input) {
  border-radius: 8px;
  transition: all 0.3s;
}

:deep(.ant-input-affix-wrapper:hover),
:deep(.ant-input:hover) {
  border-color: #667eea;
}

:deep(.ant-input-affix-wrapper:focus),
:deep(.ant-input-affix-wrapper-focused) {
  border-color: #667eea;
  box-shadow: 0 0 0 2px rgba(102, 126, 234, 0.1);
}

:deep(.ant-btn-primary) {
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 8px;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  transition: all 0.3s;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

:deep(.ant-btn-primary:active) {
  transform: translateY(0);
}

/* Google登录按钮样式 */
.google-login-btn {
  border: 1px solid #dadce0;
  color: #3c4043;
  background: white;
  height: 44px;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 500;
  transition: all 0.3s;
}

.google-login-btn:hover {
  background: #f7f8f8;
  border-color: #dadce0;
  color: #3c4043;
  transform: translateY(-2px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.google-login-btn:active {
  transform: translateY(0);
}

.google-icon {
  width: 20px;
  height: 20px;
  display: inline-block;
}

:deep(.ant-divider) {
  margin: 20px 0;
  font-size: 13px;
  color: #8c8c8c;
}

/* 响应式设计 */
@media (max-width: 576px) {
  .auth-card {
    max-width: 100%;
  }

  .title {
    font-size: 24px;
  }

  .logo {
    width: 56px;
    height: 56px;
  }
}
</style>
