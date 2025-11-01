<template>
  <div class="grid-view">
    <div class="grid-container">
      <div
        v-for="image in images"
        :key="image.fileId"
        class="grid-item"
        :class="{ 'selected': isImageSelected(image.fileId) }"
        @click="handleImageClick(image)"
      >
        <div class="image-wrapper">
          <!-- Image container -->
          <div class="image-container">
            <!-- Checkbox in selection mode -->
            <div v-if="selectionMode" class="selection-checkbox" @click.stop="handleToggleSelect(image.fileId)">
              <a-checkbox :checked="isImageSelected(image.fileId)" />
            </div>
            
            <img 
              :src="image.thumbnailUrl || image.fileUrl" 
              :alt="image.originalFilename"
              class="grid-image"
              @error="handleImageError"
              loading="lazy"
            />
            <!-- Action overlay shown on hover -->
            <div v-if="!selectionMode" class="image-overlay">
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
          
          <!-- Image info area (always visible) -->
          <div class="image-card-info">
            <p class="image-card-title">{{ image.originalFilename }}</p>
            <div class="image-card-meta">
              <span class="image-card-size">{{ formatFileSize(image.fileSize) }}</span>
              <!-- Tag display -->
              <div v-if="image.tags && image.tags.length > 0" class="image-card-tags">
                <a-tag 
                  v-for="tag in image.tags.slice(0, 3)" 
                  :key="tag.tagId"
                  :color="tag.color || 'blue'"
                  size="small"
                >
                  {{ tag.tagName }}
                </a-tag>
                <a-tag v-if="image.tags.length > 3" size="small">
                  +{{ image.tags.length - 3 }}
                </a-tag>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Load more indicator -->
    <div v-if="loading" class="loading-indicator">
      <a-spin size="large" />
      <p>加载中...</p>
    </div>

    <!-- Empty state -->
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
import { formatFileSize } from '../../utils/file'
import type { FileInfoResponse } from '../../types/file'

// Props
interface Props {
  images: FileInfoResponse[]
  loading?: boolean
  selectionMode?: boolean
  selectedFileIds?: Set<number>
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
  toggleSelect: [fileId: number]
}>()

// Check if image is selected
const isImageSelected = (fileId: number) => {
  return props.selectedFileIds?.has(fileId) || false
}

// Toggle selection
const handleToggleSelect = (fileId: number) => {
  emit('toggleSelect', fileId)
}

// Image load error handling
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  // Prevent infinite loop: if already placeholder, don't retry
  if (img.src.includes('placeholder-image.png') || img.src.includes('data:image')) {
    return
  }
  // Use base64 inline gray placeholder image to avoid additional requests
  img.src = 'data:image/svg+xml;base64,PHN2ZyB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgeG1sbnM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvc3ZnIj48cmVjdCB3aWR0aD0iMjAwIiBoZWlnaHQ9IjIwMCIgZmlsbD0iI2YwZjBmMCIvPjx0ZXh0IHg9IjUwJSIgeT0iNTAlIiBmb250LWZhbWlseT0iQXJpYWwiIGZvbnQtc2l6ZT0iMTQiIGZpbGw9IiM5OTkiIHRleHQtYW5jaG9yPSJtaWRkbGUiIGR5PSIuM2VtIj7lm77niYfmlqDovb3lpLHotKU8L3RleHQ+PC9zdmc+'
}

// Event handlers
const handleImageClick = (image: FileInfoResponse) => {
  // If in selection mode, click toggles selection
  if (props.selectionMode) {
    handleToggleSelect(image.fileId)
  } else {
    // Otherwise preview
    handlePreview(image)
  }
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
  column-count: 4;
  column-gap: 16px;
  padding: 8px;
}

.grid-item {
  position: relative;
  cursor: pointer;
  width: 100%;
  break-inside: avoid;
  margin-bottom: 16px;
}

.grid-item.selected .image-wrapper {
  box-shadow: 0 0 0 3px #1890ff;
}

.image-wrapper {
  position: relative;
  width: 100%;
  border-radius: 12px;
  overflow: hidden;
  background: white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
}

.image-container {
  position: relative;
  width: 100%;
  background: #f5f5f5;
  border-radius: 12px 12px 0 0;
  overflow: hidden;
}

.selection-checkbox {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 10;
  background: white;
  border-radius: 4px;
  padding: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.image-wrapper:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.grid-image {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.3s ease;
}

.image-wrapper:hover .grid-image {
  transform: scale(1.02);
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
    rgba(0, 0, 0, 0.3) 70%,
    rgba(0, 0, 0, 0.7) 100%
  );
  opacity: 0;
  transition: opacity 0.3s ease;
  display: flex;
  align-items: flex-end;
  justify-content: center;
}

.image-container:hover .image-overlay {
  opacity: 1;
}

.image-actions {
  display: flex;
  gap: 8px;
  justify-content: center;
  flex-wrap: wrap;
  padding: 12px;
  width: 100%;
}

/* Image card info area (always visible) */
.image-card-info {
  padding: 12px;
  background: white;
  border-radius: 0 0 12px 12px;
}

.image-card-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 8px 0;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.image-card-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.image-card-size {
  font-size: 12px;
  color: #6b7280;
}

.image-card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.image-card-tags :deep(.ant-tag) {
  margin: 0;
  font-size: 11px;
  padding: 2px 8px;
  line-height: 18px;
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

/* Responsive design */
@media (min-width: 1600px) {
  .grid-container {
    column-count: 5;
  }
}

@media (max-width: 1200px) {
  .grid-container {
    column-count: 3;
  }
}

@media (max-width: 768px) {
  .grid-container {
    column-count: 2;
    column-gap: 12px;
    padding: 4px;
  }
  
  .grid-item {
    margin-bottom: 12px;
  }
  
  .image-card-info {
    padding: 10px;
  }
  
  .image-card-title {
    font-size: 13px;
    margin-bottom: 6px;
  }
  
  .image-actions {
    gap: 6px;
    padding: 6px;
  }
  
  :deep(.action-btn) {
    width: 28px;
    height: 28px;
  }
}

@media (max-width: 480px) {
  .grid-container {
    column-count: 1;
    column-gap: 0;
  }
  
  .grid-item {
    margin-bottom: 10px;
  }
  
  .image-card-info {
    padding: 8px;
  }
  
  .image-card-title {
    font-size: 12px;
  }
  
  .image-card-size {
    font-size: 11px;
  }
}
</style>
