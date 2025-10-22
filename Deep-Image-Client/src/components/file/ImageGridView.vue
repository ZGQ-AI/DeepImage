<template>
  <div class="grid-view">
    <div class="grid-container">
      <div
        v-for="image in images"
        :key="image.fileId"
        class="grid-item"
        @click="handleImageClick(image)"
      >
        <div class="image-wrapper">
          <a-image 
            :src="image.thumbnailUrl || image.fileUrl" 
            :alt="image.originalFilename"
            :preview="false"
            class="grid-image"
            @error="handleImageError"
          />
          <!-- 悬停时显示的信息覆盖层 -->
          <div class="image-overlay">
            <div class="image-info">
              <p class="image-name">{{ image.originalFilename }}</p>
              <p class="image-meta">{{ formatFileSize(image.fileSize) }}</p>
              <!-- 标签显示 -->
              <div v-if="image.tags && image.tags.length > 0" class="image-tags">
                <a-tag 
                  v-for="tag in image.tags.slice(0, 2)" 
                  :key="tag.tagId"
                  :color="tag.color || 'blue'"
                  size="small"
                >
                  {{ tag.tagName }}
                </a-tag>
                <a-tag v-if="image.tags.length > 2" size="small">
                  +{{ image.tags.length - 2 }}
                </a-tag>
              </div>
            </div>
            <div class="image-actions">
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handlePreview(image)"
                class="action-btn"
                title="预览"
              >
                <EyeOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handleDownload(image)"
                class="action-btn"
                title="下载"
              >
                <DownloadOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handleRename(image)"
                class="action-btn"
                title="重命名"
              >
                <EditOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handleManageTags(image)"
                class="action-btn"
                title="管理标签"
              >
                <TagOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                danger
                @click.stop="handleDelete(image)"
                class="action-btn"
                title="删除"
              >
                <DeleteOutlined />
              </a-button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载更多指示器 -->
    <div v-if="loading" class="loading-indicator">
      <a-spin size="large" />
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-if="!loading && images.length === 0" class="empty-grid">
      <PictureOutlined :style="{ fontSize: '48px', color: '#d9d9d9' }" />
      <p>暂无图片</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { 
  PictureOutlined, 
  EyeOutlined,
  DownloadOutlined,
  EditOutlined,
  TagOutlined,
  DeleteOutlined 
} from '@ant-design/icons-vue'
import type { FileInfoResponse } from '../../types/file'

// Props
interface Props {
  images: FileInfoResponse[]
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

// Emits
const emit = defineEmits<{
  preview: [image: FileInfoResponse]
  download: [image: FileInfoResponse]
  rename: [image: FileInfoResponse]
  manageTags: [image: FileInfoResponse]
  delete: [image: FileInfoResponse]
  loadMore: []
}>()

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 图片加载错误处理
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/placeholder-image.png' // 设置占位图
}

// 事件处理
const handleImageClick = (image: FileInfoResponse) => {
  // 点击图片不做任何操作
}

const handlePreview = (image: FileInfoResponse) => {
  emit('preview', image)
}

const handleDownload = (image: FileInfoResponse) => {
  emit('download', image)
}

const handleRename = (image: FileInfoResponse) => {
  emit('rename', image)
}

const handleManageTags = (image: FileInfoResponse) => {
  emit('manageTags', image)
}

const handleDelete = (image: FileInfoResponse) => {
  emit('delete', image)
}
</script>

<style scoped>
.grid-view {
  width: 100%;
}

.grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  padding: 8px;
}

.grid-item {
  position: relative;
  cursor: pointer;
  aspect-ratio: 1;
}

.image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.image-wrapper:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.image-wrapper :deep(.grid-image) {
  width: 100%;
  height: 100%;
}

.image-wrapper :deep(.grid-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  transition: transform 0.3s ease;
  border-radius: 8px;
}

.image-wrapper:hover :deep(.grid-image img) {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(
    to bottom,
    rgba(0, 0, 0, 0) 0%,
    rgba(0, 0, 0, 0.1) 50%,
    rgba(0, 0, 0, 0.8) 100%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 16px;
}

.image-wrapper:hover .image-overlay {
  opacity: 1;
}

.image-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: flex-end;
}

.image-name {
  color: white;
  font-size: 14px;
  font-weight: 500;
  margin: 0 0 4px 0;
  word-break: break-all;
  display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-meta {
  color: rgba(255, 255, 255, 0.8);
  font-size: 12px;
  margin: 0;
}

.image-tags {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.image-tags :deep(.ant-tag) {
  margin: 0;
  font-size: 11px;
  padding: 0 6px;
  line-height: 18px;
}

.image-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
  margin-top: 12px;
}

:deep(.action-btn) {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.2);
  color: white !important;
  border: none !important;
  backdrop-filter: blur(10px);
  transition: all 0.2s ease;
}

:deep(.action-btn:hover) {
  background: rgba(255, 255, 255, 0.3);
  transform: scale(1.1);
}

:deep(.action-btn.ant-btn-dangerous:hover) {
  background: rgba(255, 59, 48, 0.8);
}

.loading-indicator {
  text-align: center;
  padding: 40px 20px;
  color: #6b7280;
}

.loading-indicator p {
  margin-top: 16px;
  font-size: 14px;
}

.empty-grid {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-grid p {
  margin-top: 16px;
  font-size: 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
  }
}

@media (max-width: 768px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 10px;
    padding: 4px;
  }
  
  .image-overlay {
    padding: 12px;
  }
  
  .image-actions {
    gap: 6px;
  }
  
  :deep(.action-btn) {
    width: 28px;
    height: 28px;
  }
}

@media (max-width: 480px) {
  .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
    gap: 8px;
  }
}
</style>
