<!--
  标签列表组件
-->
<template>
  <div class="tag-list">
    <a-spin :spinning="loading">
      <a-empty v-if="!loading && tags.length === 0" description="暂无标签" style="padding: 48px 0">
        <template #image>
          <TagOutlined :style="{ fontSize: '64px', color: '#d9d9d9' }" />
        </template>
      </a-empty>

      <div v-else class="tag-grid">
        <div
          v-for="tag in tags"
          :key="tag.id"
          class="tag-card"
          :style="{ borderLeftColor: tag.color || '#1890ff' }"
        >
          <!-- 标签信息 -->
          <div class="tag-info">
            <div class="tag-header">
              <div class="tag-name-wrapper">
                <div
                  class="tag-color-dot"
                  :style="{ backgroundColor: tag.color || '#1890ff' }"
                ></div>
                <span class="tag-name">{{ tag.tagName }}</span>
              </div>
              <a-tag :color="tag.color || '#1890ff'" class="usage-tag">
                {{ tag.usageCount }} 次使用
              </a-tag>
            </div>
            <div class="tag-meta">
              <span class="created-time">
                <ClockCircleOutlined />
                {{ formatTime(tag.createdAt) }}
              </span>
            </div>
          </div>

          <!-- 操作按钮 -->
          <div class="tag-actions">
            <a-button type="text" size="small" @click="$emit('edit', tag)">
              <EditOutlined />
              编辑
            </a-button>
            <a-popconfirm
              title="确定删除此标签吗？"
              ok-text="确定"
              cancel-text="取消"
              @confirm="$emit('delete', tag.id)"
            >
              <a-button type="text" size="small" danger>
                <DeleteOutlined />
                删除
              </a-button>
            </a-popconfirm>
          </div>
        </div>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import {
  TagOutlined,
  EditOutlined,
  DeleteOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons-vue'
import { formatTime as formatTimeUtil } from '../../utils/time'
import type { TagResponse } from '../../types/tag'

interface Props {
  tags: TagResponse[]
  loading?: boolean
}

interface Emits {
  (e: 'edit', tag: TagResponse): void
  (e: 'delete', tagId: number): void
}

withDefaults(defineProps<Props>(), {
  loading: false,
})

defineEmits<Emits>()

// 格式化时间
function formatTime(dateStr: string): string {
  return formatTimeUtil(dateStr)
}
</script>

<style scoped>
.tag-list {
  min-height: 200px;
}

.tag-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 16px;
}

.tag-card {
  background: white;
  border: 1px solid #f0f0f0;
  border-left: 4px solid;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.tag-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.tag-info {
  flex: 1;
}

.tag-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 8px;
}

.tag-name-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.tag-color-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  flex-shrink: 0;
}

.tag-name {
  font-size: 16px;
  font-weight: 600;
  color: #262626;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.usage-tag {
  flex-shrink: 0;
  margin: 0;
  font-size: 12px;
}

.tag-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: #8c8c8c;
}

.created-time {
  display: flex;
  align-items: center;
  gap: 4px;
}

.tag-actions {
  display: flex;
  gap: 8px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.tag-actions .ant-btn {
  padding: 4px 8px;
  height: auto;
  font-size: 13px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .tag-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 576px) {
  .tag-card {
    padding: 12px;
  }

  .tag-name {
    font-size: 14px;
  }

  .tag-actions {
    flex-wrap: wrap;
  }
}
</style>
