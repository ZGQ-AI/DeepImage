<!--
  用户中心页面
-->
<template>
  <div class="user-profile-page">
    <!-- 页面头部 -->
    <a-page-header title="个人中心" @back="() => router.back()">
      <template #extra>
        <a-space>
          <a-button @click="handleRefresh" :loading="loading">
            <ReloadOutlined /> 刷新
          </a-button>
        </a-space>
      </template>
    </a-page-header>

    <!-- 主要内容区域 -->
    <div class="content-wrapper">
      <a-row :gutter="[16, 16]">
        <!-- 左侧：个人信息 -->
        <a-col :xs="24" :lg="14">
          <a-card title="个人信息" :bordered="false" class="profile-card">
            <ProfileForm />
          </a-card>
        </a-col>

        <!-- 右侧：登录设备管理 -->
        <a-col :xs="24" :lg="10">
          <a-card title="登录设备管理" :bordered="false" class="session-card">
            <template #extra>
              <a-popconfirm
                title="确定移除所有其他设备吗？"
                ok-text="确定"
                cancel-text="取消"
                @confirm="handleDeleteOtherSessions"
              >
                <a-button type="link" danger size="small">
                  <DeleteOutlined /> 移除其他设备
                </a-button>
              </a-popconfirm>
            </template>
            <SessionList />
          </a-card>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ReloadOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { useUserStore } from '../stores/useUserStore'
import ProfileForm from '../components/user/ProfileForm.vue'
import SessionList from '../components/user/SessionList.vue'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)

// 刷新所有数据
async function handleRefresh() {
  loading.value = true
  try {
    await Promise.all([
      userStore.fetchProfile(),
      userStore.fetchSessions(),
    ])
  } finally {
    loading.value = false
  }
}

// 删除其他所有会话
async function handleDeleteOtherSessions() {
  await userStore.deleteOtherSessions()
}

// 页面初始化
onMounted(async () => {
  await handleRefresh()
})
</script>

<style scoped>
.user-profile-page {
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.ant-page-header) {
  background-color: white;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.content-wrapper {
  padding: 0 24px;
}

.profile-card,
.session-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
}

.profile-card:hover,
.session-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

:deep(.ant-card-head) {
  border-bottom: 2px solid #f0f0f0;
  font-weight: 600;
}

:deep(.ant-card-body) {
  padding: 24px;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .content-wrapper {
    padding: 0 12px;
  }

  :deep(.ant-card-body) {
    padding: 16px;
  }
}

@media (max-width: 576px) {
  :deep(.ant-page-header) {
    padding: 12px 16px;
  }

  .content-wrapper {
    padding: 0 8px;
  }
}
</style>

