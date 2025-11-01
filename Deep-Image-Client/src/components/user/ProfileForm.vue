<!--
  User Profile Edit Form Component
-->
<template>
  <a-form ref="formRef" :model="form" :rules="rules" layout="vertical" @finish="onSubmit">
    <a-form-item label="用户名" name="username">
      <a-input
        v-model:value="form.username"
        placeholder="3-50个字符，支持字母、数字、下划线、中文"
        :maxlength="50"
        size="large"
      >
        <template #prefix>
          <UserOutlined style="color: rgba(0, 0, 0, 0.25)" />
        </template>
      </a-input>
    </a-form-item>

    <a-form-item label="邮箱" name="email">
      <a-input v-model:value="form.email" disabled size="large">
        <template #prefix>
          <MailOutlined style="color: rgba(0, 0, 0, 0.25)" />
        </template>
      </a-input>
      <template #extra>
        <a-space>
          <a-tag color="green" v-if="form.verified"> <CheckCircleOutlined /> 已验证 </a-tag>
          <a-tag color="orange" v-else> <ClockCircleOutlined /> 未验证 </a-tag>
          <span style="color: #999; font-size: 12px">邮箱无法修改</span>
        </a-space>
      </template>
    </a-form-item>

    <a-form-item label="手机号" name="phone">
      <a-input v-model:value="form.phone" placeholder="请输入手机号（可选）" size="large">
        <template #prefix>
          <PhoneOutlined style="color: rgba(0, 0, 0, 0.25)" />
        </template>
      </a-input>
    </a-form-item>

    <a-form-item label="头像" name="avatarUrl">
      <AvatarUpload v-model="form.avatarUrl" @upload-success="handleAvatarUploadSuccess" />
    </a-form-item>

    <a-form-item>
      <a-space>
        <a-button type="primary" html-type="submit" :loading="loading" size="large">
          <SaveOutlined /> 保存修改
        </a-button>
        <a-button @click="resetForm" size="large"> <ReloadOutlined /> 重置 </a-button>
      </a-space>
    </a-form-item>
  </a-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  SaveOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { useUserStore } from '../../stores/useUserStore'
import type { UpdateUserProfileRequest } from '../../types/user'
import AvatarUpload from './AvatarUpload.vue'

const userStore = useUserStore()

// Form reference
const formRef = ref()

// Form data
const form = reactive({
  username: '',
  email: '',
  phone: '',
  avatarUrl: '',
  verified: false,
})

// Loading state
const loading = ref(false)

// Form validation rules
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 50, message: '用户名长度为3-50个字符', trigger: 'blur' },
    {
      pattern: /^[a-zA-Z0-9_\u4e00-\u9fa5]+$/,
      message: '只能包含字母、数字、下划线和中文',
      trigger: 'blur',
    },
  ],
  phone: [
    {
      pattern: /^1[3-9]\d{9}$/,
      message: '手机号格式不正确',
      trigger: 'blur',
    },
  ],
}

// Initialize form data
function initForm() {
  if (userStore.profile) {
    form.username = userStore.profile.username
    form.email = userStore.profile.email
    form.phone = userStore.profile.phone || ''
    form.avatarUrl = userStore.profile.avatarUrl || ''
    form.verified = userStore.profile.verified
  }
}

// Watch user profile changes
watch(
  () => userStore.profile,
  () => {
    initForm()
  },
  { immediate: true },
)

// Submit form
async function onSubmit() {
  loading.value = true
  try {
    const request: UpdateUserProfileRequest = {}

    // Only submit modified fields
    if (form.username !== userStore.profile?.username) {
      request.username = form.username
    }
    if (form.phone !== userStore.profile?.phone) {
      request.phone = form.phone || undefined
    }
    if (form.avatarUrl !== userStore.profile?.avatarUrl) {
      request.avatarUrl = form.avatarUrl || undefined
    }

    // If no changes, return
    if (Object.keys(request).length === 0) {
      return
    }

    await userStore.updateProfile(request)
  } finally {
    loading.value = false
  }
}

// Reset form
function resetForm() {
  initForm()
}

// Avatar upload success callback
function handleAvatarUploadSuccess() {
  // Avatar URL has been automatically updated to form.avatarUrl via v-model
  message.success('头像已更新，请点击"保存修改"按钮')
}

// Fetch user information on component mount
onMounted(async () => {
  if (!userStore.profile) {
    await userStore.fetchProfile()
  }
})
</script>

<style scoped>
:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-input-affix-wrapper),
:deep(.ant-input) {
  border-radius: 8px;
}

:deep(.ant-btn) {
  border-radius: 8px;
}
</style>
