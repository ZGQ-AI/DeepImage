<!--
  Google OAuth Callback Handler Page
-->
<template>
  <div class="callback-page">
    <a-spin :spinning="loading" size="large">
      <div class="callback-content">
        <template v-if="!error">
          <CheckCircleOutlined class="icon success" />
          <h2>登录成功</h2>
          <p>正在跳转...</p>
        </template>
        <template v-else>
          <CloseCircleOutlined class="icon error" />
          <h2>登录失败</h2>
          <p>{{ errorMessage }}</p>
          <a-button type="primary" @click="backToLogin">返回登录</a-button>
        </template>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { CheckCircleOutlined, CloseCircleOutlined } from '@ant-design/icons-vue'
import { useAuthStore } from '../stores/useAuthStore'
import { useUserStore } from '../stores/useUserStore'

const router = useRouter()
const authStore = useAuthStore()
const userStore = useUserStore()

const loading = ref(true)
const error = ref(false)
const errorMessage = ref('')
const redirectPath = ref('/')

onMounted(async () => {
  try {
    // Parse URL parameters
    const params = new URLSearchParams(window.location.search)
    const accessToken = params.get('access_token')
    const refreshToken = params.get('refresh_token')
    const loginError = params.get('loginError')
    const loginSuccess = params.get('loginSuccess')
    redirectPath.value = params.get('redirect') || '/'

    // Check if there's an error
    if (loginError || loginSuccess === 'false') {
      error.value = true
      errorMessage.value = getErrorMessage(loginError)
      loading.value = false
      return
    }

    // Check if token exists
    if (!accessToken || !refreshToken) {
      error.value = true
      errorMessage.value = '登录失败，缺少认证信息'
      loading.value = false
      return
    }

    // Save token to store (use localStorage as default storage mode)
    authStore.applyTokenPair(
      {
        accessToken,
        refreshToken,
        expiresIn: 3600,
      },
      'local', // Google OAuth always uses localStorage
    )

    // Fetch user information (with retry mechanism)
    try {
      await fetchProfileWithRetry()
    } catch (profileError) {
      // Even if fetching user info fails, token is saved, allow user to continue
      console.warn('[AuthCallback] Failed to fetch user profile:', profileError)
      // Don't throw error, allow navigation to continue
    }

    message.success('登录成功！')

    // Navigate to specified page immediately
    router.replace(redirectPath.value)

    // eslint-disable-next-line @typescript-eslint/no-explicit-any
  } catch (err: any) {
    error.value = true
    errorMessage.value = err.message || '登录处理失败'
    loading.value = false
  }
})

/**
 * Fetch user profile with retry mechanism
 * Automatically retries on network fluctuations to improve success rate
 */
async function fetchProfileWithRetry(maxRetries = 2) {
  let lastError

  for (let i = 0; i < maxRetries; i++) {
    try {
      await userStore.fetchProfile()
      return // Success
    } catch (err) {
      lastError = err
      console.warn(`Failed to fetch user profile, retry ${i + 1}/${maxRetries}`)

      if (i < maxRetries - 1) {
        // Wait 1 second before retry
        await new Promise((resolve) => setTimeout(resolve, 1000))
      }
    }
  }

  throw lastError // All retries failed
}

function getErrorMessage(errorCode: string | null): string {
  const errorMessages: Record<string, string> = {
    GOOGLE_OAUTH_AUTHORIZATION_FAILED: 'Google授权失败，请重试',
    GOOGLE_OAUTH_MISSING_CODE: '授权码缺失',
    GOOGLE_OAUTH_TOKEN_EXCHANGE_FAILED: '获取访问令牌失败',
    GOOGLE_OAUTH_MISSING_ID_TOKEN: '无法获取用户信息',
    GOOGLE_OAUTH_MISSING_EMAIL: '无法获取邮箱信息',
    GOOGLE_OAUTH_CALLBACK_PROCESSING_FAILED: '登录处理失败',
  }

  return errorMessages[errorCode || ''] || '未知错误，请重试'
}

/**
 * Return to login page, preserve redirect parameter
 */
function backToLogin() {
  if (redirectPath.value && redirectPath.value !== '/') {
    router.replace({
      path: '/auth',
      query: { redirect: redirectPath.value },
    })
  } else {
    router.replace('/auth')
  }
}
</script>

<style scoped>
.callback-page {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: #f0f2f5;
}

.callback-content {
  text-align: center;
  padding: 48px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  min-width: 400px;
}

.icon {
  font-size: 64px;
  margin-bottom: 24px;
}

.icon.success {
  color: #52c41a;
}

.icon.error {
  color: #ff4d4f;
}

h2 {
  margin-bottom: 16px;
  font-size: 24px;
  font-weight: 600;
}

p {
  color: #666;
  margin-bottom: 24px;
  font-size: 16px;
}

/* Responsive design */
@media (max-width: 576px) {
  .callback-content {
    min-width: auto;
    width: 90vw;
    padding: 32px 24px;
  }
}
</style>
