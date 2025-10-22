<!-- 
  主应用组件
  作为整个应用的根组件，负责渲染基础布局
-->
<template>
  <div id="app">
    <!-- 使用基础布局组件，包含头部、内容区域和底部 -->
    <BasicLayout />

    <!-- 全局登录弹窗 -->
    <LoginModal
      v-model:open="authStore.loginModalVisible"
      :redirectPath="authStore.loginModalRedirectPath"
      @login-success="handleLoginSuccess"
    />
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import BasicLayout from './layout/BasicLayout.vue'
import LoginModal from './components/auth/LoginModal.vue'
import { useAuthStore } from './stores/useAuthStore'
import { useUserStore } from './stores/useUserStore'

const authStore = useAuthStore()
const userStore = useUserStore()

// 应用初始化时，如果有 token 则加载用户信息
onMounted(async () => {
  if (authStore.isAuthenticated && !userStore.profile) {
    try {
      await userStore.fetchProfile()
    } catch (err) {
      console.warn('Failed to fetch user profile on app mount:', err)
    }
  }
})

// 登录成功处理
const handleLoginSuccess = () => {
  // 登录成功后，弹窗内部会处理跳转
  // 这里可以添加额外的全局逻辑（如刷新数据等）
  authStore.hideLoginModal()
}
</script>

<style scoped>
/* 应用根组件样式 */
</style>
