<!--
  Session Management List Component
-->
<template>
  <div class="session-list">
    <a-list :data-source="userStore.sessions" :loading="userStore.sessionsLoading">
      <template #renderItem="{ item }">
        <a-list-item>
          <a-list-item-meta>
            <!-- Device icon -->
            <template #avatar>
              <a-avatar :style="{ backgroundColor: item.isCurrent ? '#1890ff' : '#999' }">
                <template #icon>
                  <LaptopOutlined v-if="isDesktop(item.userAgent)" />
                  <MobileOutlined v-else />
                </template>
              </a-avatar>
            </template>

            <!-- Device name -->
            <template #title>
              <a-space>
                <span>{{ parseDeviceName(item.userAgent) }}</span>
                <a-tag v-if="item.isCurrent" color="blue"> <CheckCircleOutlined /> 当前设备 </a-tag>
              </a-space>
            </template>

            <!-- Detailed information -->
            <template #description>
              <div class="session-details">
                <div>
                  <EnvironmentOutlined />
                  <span>IP: {{ item.ipAddress }}</span>
                </div>
                <div>
                  <ClockCircleOutlined />
                  <span>登录时间: {{ formatTime(item.createdAt) }}</span>
                </div>
                <div>
                  <SyncOutlined />
                  <span>上次活跃: {{ formatTime(item.lastRefreshAt) }}</span>
                </div>
                <div style="color: #999; font-size: 12px; margin-top: 4px">
                  <GlobalOutlined />
                  <span>{{ parseBrowserName(item.userAgent) }}</span>
                </div>
              </div>
            </template>
          </a-list-item-meta>

          <!-- Action buttons -->
          <template #actions>
            <a-popconfirm
              v-if="!item.isCurrent"
              title="确定要移除此设备吗？移除后该设备需要重新登录。"
              ok-text="确定"
              cancel-text="取消"
              @confirm="handleDeleteSession(item.id)"
            >
              <a-button type="link" danger size="small"> <DeleteOutlined /> 移除 </a-button>
            </a-popconfirm>
            <span v-else style="color: #999; font-size: 12px">当前使用中</span>
          </template>
        </a-list-item>
      </template>

      <!-- Empty state -->
      <template #empty>
        <a-empty description="暂无登录设备" />
      </template>
    </a-list>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import {
  LaptopOutlined,
  MobileOutlined,
  CheckCircleOutlined,
  EnvironmentOutlined,
  ClockCircleOutlined,
  SyncOutlined,
  GlobalOutlined,
  DeleteOutlined,
} from '@ant-design/icons-vue'
import { useUserStore } from '../../stores/useUserStore'
import { parseDeviceName, isDesktop, parseBrowserName } from '../../utils/device'
import { formatTime } from '../../utils/time'

const userStore = useUserStore()

// Delete session
async function handleDeleteSession(sessionId: number) {
  await userStore.deleteSession(sessionId)
}

// Fetch session list on component mount
onMounted(async () => {
  await userStore.fetchSessions()
})
</script>

<style scoped>
.session-list {
  max-height: 600px;
  overflow-y: auto;
}

.session-details {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
}

.session-details > div {
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.ant-list-item) {
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

:deep(.ant-list-item:hover) {
  background-color: #fafafa;
}

:deep(.ant-list-item:last-child) {
  border-bottom: none;
}

/* Scrollbar styles */
.session-list::-webkit-scrollbar {
  width: 6px;
}

.session-list::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

.session-list::-webkit-scrollbar-thumb:hover {
  background-color: rgba(0, 0, 0, 0.3);
}
</style>
