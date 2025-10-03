<!--
  个人信息编辑表单组件
-->
<template>
  <a-form
    ref="formRef"
    :model="form"
    :rules="rules"
    layout="vertical"
    @finish="onSubmit"
  >
    <a-form-item label="用户名" name="username">
      <a-input 
        v-model:value="form.username" 
        placeholder="3-50个字符，支持字母、数字、下划线、中文"
        :maxlength="50"
        size="large"
      >
        <template #prefix>
          <UserOutlined style="color: rgba(0,0,0,.25)" />
        </template>
      </a-input>
    </a-form-item>

    <a-form-item label="邮箱" name="email">
      <a-input v-model:value="form.email" disabled size="large">
        <template #prefix>
          <MailOutlined style="color: rgba(0,0,0,.25)" />
        </template>
      </a-input>
      <template #extra>
        <a-space>
          <a-tag color="green" v-if="form.verified">
            <CheckCircleOutlined /> 已验证
          </a-tag>
          <a-tag color="orange" v-else>
            <ClockCircleOutlined /> 未验证
          </a-tag>
          <span style="color: #999; font-size: 12px">邮箱无法修改</span>
        </a-space>
      </template>
    </a-form-item>

    <a-form-item label="手机号" name="phone">
      <a-input 
        v-model:value="form.phone" 
        placeholder="请输入手机号（可选）"
        size="large"
      >
        <template #prefix>
          <PhoneOutlined style="color: rgba(0,0,0,.25)" />
        </template>
      </a-input>
    </a-form-item>

    <a-form-item label="头像" name="avatarUrl">
      <AvatarUpload 
        v-model="form.avatarUrl"
        @upload-success="handleAvatarUploadSuccess"
      />
    </a-form-item>

    <a-form-item>
      <a-space>
        <a-button type="primary" html-type="submit" :loading="loading" size="large">
          <SaveOutlined /> 保存修改
        </a-button>
        <a-button @click="resetForm" size="large">
          <ReloadOutlined /> 重置
        </a-button>
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

// 表单引用
const formRef = ref()

// 表单数据
const form = reactive({
  username: '',
  email: '',
  phone: '',
  avatarUrl: '',
  verified: false,
})

// 加载状态
const loading = ref(false)

// 表单校验规则
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

// 初始化表单数据
function initForm() {
  if (userStore.profile) {
    form.username = userStore.profile.username
    form.email = userStore.profile.email
    form.phone = userStore.profile.phone || ''
    form.avatarUrl = userStore.profile.avatarUrl || ''
    form.verified = userStore.profile.verified
  }
}

// 监听用户信息变化
watch(
  () => userStore.profile,
  () => {
    initForm()
  },
  { immediate: true }
)

// 提交表单
async function onSubmit() {
  loading.value = true
  try {
    const request: UpdateUserProfileRequest = {}

    // 只提交修改过的字段
    if (form.username !== userStore.profile?.username) {
      request.username = form.username
    }
    if (form.phone !== userStore.profile?.phone) {
      request.phone = form.phone || undefined
    }
    if (form.avatarUrl !== userStore.profile?.avatarUrl) {
      request.avatarUrl = form.avatarUrl || undefined
    }

    // 如果没有修改，提示用户
    if (Object.keys(request).length === 0) {
      return
    }

    await userStore.updateProfile(request)
  } finally {
    loading.value = false
  }
}

// 重置表单
function resetForm() {
  initForm()
}

// 头像上传成功回调
function handleAvatarUploadSuccess() {
  // 头像 URL 已通过 v-model 自动更新到 form.avatarUrl
  message.success('头像已更新，请点击"保存修改"按钮')
}

// 组件挂载时获取用户信息
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

