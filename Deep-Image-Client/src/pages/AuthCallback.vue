<!--
  Google OAuth回调处理页面
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

onMounted(async () => {
  try {
    // 解析URL参数
    const params = new URLSearchParams(window.location.search)
    const accessToken = params.get('access_token')
    const refreshToken = params.get('refresh_token')
    const loginError = params.get('loginError')
    const loginSuccess = params.get('loginSuccess')
    
    // 检查是否有错误
    if (loginError || loginSuccess === 'false') {
      error.value = true
      errorMessage.value = getErrorMessage(loginError)
      loading.value = false
      return
    }
    
    // 检查token是否存在
    if (!accessToken || !refreshToken) {
      error.value = true
      errorMessage.value = '登录失败，缺少认证信息'
      loading.value = false
      return
    }
    
    // 保存token到store（使用localStorage持久化）
    authStore.applyTokenPair(
      {
        accessToken,
        refreshToken,
        expiresIn: 3600
      },
      'local'
    )
    
    // 获取用户信息
    await userStore.fetchProfile()
    
    message.success('登录成功！')
    
    // 跳转到首页
    setTimeout(() => {
      router.replace('/')
    }, 500)
    
  } catch (err: any) {
    error.value = true
    errorMessage.value = err.message || '登录处理失败'
    loading.value = false
  }
})

function getErrorMessage(errorCode: string | null): string {
  const errorMessages: Record<string, string> = {
    'GOOGLE_OAUTH_AUTHORIZATION_FAILED': 'Google授权失败，请重试',
    'GOOGLE_OAUTH_MISSING_CODE': '授权码缺失',
    'GOOGLE_OAUTH_TOKEN_EXCHANGE_FAILED': '获取访问令牌失败',
    'GOOGLE_OAUTH_MISSING_ID_TOKEN': '无法获取用户信息',
    'GOOGLE_OAUTH_MISSING_EMAIL': '无法获取邮箱信息',
    'GOOGLE_OAUTH_CALLBACK_PROCESSING_FAILED': '登录处理失败',
  }
  
  return errorMessages[errorCode || ''] || '未知错误，请重试'
}

function backToLogin() {
  router.replace('/auth')
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

/* 响应式设计 */
@media (max-width: 576px) {
  .callback-content {
    min-width: auto;
    width: 90vw;
    padding: 32px 24px;
  }
}
</style>

