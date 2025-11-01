<!-- 
  Main Application Component
  Serves as the root component of the entire application, responsible for rendering the base layout
-->
<template>
  <div id="app">
    <!-- Use base layout component, includes header, content area and footer -->
    <BasicLayout />

    <!-- Global login modal -->
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

// On app initialization, if token exists, load user information
onMounted(async () => {
  if (authStore.isAuthenticated && !userStore.profile) {
    try {
      await userStore.fetchProfile()
    } catch (err) {
      console.warn('Failed to fetch user profile on app mount:', err)
    }
  }
})

// Handle login success
const handleLoginSuccess = () => {
  // After successful login, modal will handle navigation internally
  // Additional global logic can be added here (e.g., refresh data, etc.)
  authStore.hideLoginModal()
}
</script>

<style scoped>
/* App root component styles */
</style>
