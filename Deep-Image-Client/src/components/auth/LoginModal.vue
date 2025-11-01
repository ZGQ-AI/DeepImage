<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <a-modal
    v-model:open="visible"
    :title="null"
    :footer="null"
    :width="480"
    :centered="true"
    :destroyOnClose="true"
    @cancel="handleCancel"
    class="login-modal"
  >
    <div class="modal-content">
      <!-- Tab switching -->
      <a-tabs
        v-model:activeKey="activeTab"
        centered
        size="large"
        :animated="{ inkBar: true, tabPane: true }"
      >
        <!-- Login Tab -->
        <a-tab-pane key="login" tab="登录">
          <!-- Google login button -->
          <a-button class="google-login-btn" size="large" block @click="handleGoogleLogin">
            <template #icon>
              <img src="@/assets/google.svg" alt="Google" class="google-icon" />
            </template>
            使用 Google 账号登录
          </a-button>

          <!-- Divider -->
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

        <!-- Register Tab -->
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

        <!-- Reset Password Tab -->
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
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { MailOutlined, LockOutlined, UserOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '../../stores/useAuthStore'
import { useUserStore } from '../../stores/useUserStore'
import { register as registerApi, resetPassword as resetPasswordApi } from '../../api/auth'
import type { LoginRequest, RegisterRequest, ResetPasswordRequest } from '../../types/auth'

// Props
interface Props {
  open?: boolean
  initialTab?: 'login' | 'register' | 'reset'
  redirectPath?: string
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  initialTab: 'login',
  redirectPath: '',
})

// Emits
const emit = defineEmits<{
  'update:open': [value: boolean]
  'login-success': []
  'register-success': []
}>()

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

// Control modal visibility
const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value),
})

// Currently active tab
const activeTab = ref(props.initialTab)

// Loading state
const loading = ref(false)

// ===== Login Form =====
const loginFormRef = ref()
const loginForm = reactive<LoginRequest & { remember?: boolean }>({
  email: '',
  password: '',
  remember: true,
})

const loginRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

// ===== Register Form =====
const registerFormRef = ref()
const registerForm = reactive<RegisterRequest & { confirmPassword?: string }>({
  username: '',
  email: '',
  password: '',
  confirmPassword: '',
})

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度应在3-20个字符之间', trigger: 'blur' },
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,}$/,
      message: '密码必须包含字母和数字',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => {
        if (value !== registerForm.password) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
}

// ===== Reset Password Form =====
const resetFormRef = ref()
const resetForm = reactive<ResetPasswordRequest & { confirmPassword?: string }>({
  email: '',
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const resetRules = {
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' },
  ],
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' },
    {
      pattern: /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d@$!%*?&]{8,}$/,
      message: '密码必须包含字母和数字',
      trigger: 'blur',
    },
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (_rule: unknown, value: string) => {
        if (value !== resetForm.newPassword) {
          return Promise.reject('两次输入的密码不一致')
        }
        return Promise.resolve()
      },
      trigger: 'blur',
    },
  ],
}

// ===== Handler Functions =====
const handleCancel = () => {
  visible.value = false
  // Reset all forms
  loginFormRef.value?.resetFields()
  registerFormRef.value?.resetFields()
  resetFormRef.value?.resetFields()
}

const onLogin = async () => {
  try {
    loading.value = true
    await authStore.login(loginForm)
    
    // Load user information
    try {
      await userStore.fetchProfile()
    } catch (err) {
      console.warn('Failed to fetch profile after login:', err)
    }

    message.success('登录成功')
    emit('login-success')
    visible.value = false

    // Handle redirect
    if (props.redirectPath) {
      router.push(props.redirectPath)
    }
  } catch (error: any) {
    message.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

const onRegister = async () => {
  try {
    loading.value = true
    const { data } = await registerApi(registerForm)

    if (data.code === 200) {
      message.success('注册成功，请登录')
      emit('register-success')
      activeTab.value = 'login'
      // Clear register form
      registerFormRef.value?.resetFields()
    } else {
      message.error(data.message || '注册失败')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

const onResetPassword = async () => {
  try {
    loading.value = true
    const { data } = await resetPasswordApi(resetForm)

    if (data.code === 200) {
      message.success('密码重置成功，请使用新密码登录')
      activeTab.value = 'login'
      // Clear reset password form
      resetFormRef.value?.resetFields()
    } else {
      message.error(data.message || '密码重置失败')
    }
  } catch (error: any) {
    message.error(error.response?.data?.message || error.message || '密码重置失败')
  } finally {
    loading.value = false
  }
}

const handleGoogleLogin = () => {
  // Google OAuth login
  // Get redirect parameter (target path after successful login)
  const redirect = props.redirectPath || ''

  // Build callback URL, pass redirect parameter to callback page
  let callbackUrl = `${window.location.origin}/auth/callback`
  if (redirect) {
    callbackUrl += `?redirect=${encodeURIComponent(redirect)}`
  }

  // Navigate to backend OAuth initiation endpoint
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'
  const loginUrl = `${apiBaseUrl}/api/auth/google/login?fromUrl=${encodeURIComponent(callbackUrl)}`

  // Direct navigation (browser will redirect to Google)
  window.location.href = loginUrl
}

// Watch modal open, reset tab to initial value
watch(
  () => props.open,
  (newVal) => {
    if (newVal) {
      activeTab.value = props.initialTab
    }
  },
)
</script>

<style scoped>
.login-modal :deep(.ant-modal-content) {
  border-radius: 16px;
  overflow: hidden;
}

.login-modal :deep(.ant-modal-body) {
  padding: 32px;
}

.modal-content {
  max-height: 70vh;
  overflow-y: auto;
}

/* Google login button */
.google-login-btn {
  height: 48px;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.google-login-btn:hover {
  border-color: #1890ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.google-icon {
  width: 20px;
  height: 20px;
}

/* Divider styles */
:deep(.ant-divider) {
  margin: 24px 0;
  color: #9ca3af;
  font-size: 13px;
}

/* Form style optimizations */
:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-form-item-label > label) {
  font-weight: 500;
  color: #374151;
}

:deep(.ant-input),
:deep(.ant-input-password) {
  border-radius: 8px;
}

:deep(.ant-input:focus),
:deep(.ant-input-password:focus),
:deep(.ant-input-focused) {
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.1);
}

:deep(.ant-btn-primary) {
  height: 48px;
  border-radius: 8px;
  font-weight: 500;
  font-size: 16px;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.2);
  transition: all 0.3s ease;
}

:deep(.ant-btn-primary:hover) {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(24, 144, 255, 0.3);
}

.forgot-link {
  color: #1890ff;
  font-size: 14px;
  transition: color 0.3s ease;
}

.forgot-link:hover {
  color: #40a9ff;
}

/* Tabs style optimizations */
:deep(.ant-tabs) {
  margin-top: 0;
}

:deep(.ant-tabs-nav) {
  margin-bottom: 24px;
}

:deep(.ant-tabs-tab) {
  font-size: 16px;
  font-weight: 500;
  padding: 12px 0;
}

/* Responsive design */
@media (max-width: 640px) {
  .login-modal :deep(.ant-modal-body) {
    padding: 24px 20px;
  }
}

/* Scrollbar styles */
.modal-content::-webkit-scrollbar {
  width: 6px;
}

.modal-content::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb {
  background: #c1c1c1;
  border-radius: 3px;
}

.modal-content::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>

