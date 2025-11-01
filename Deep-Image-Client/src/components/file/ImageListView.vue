<template>
  <div class="list-view">
    <div class="list-container">
      <!-- Table header -->
      <div class="list-header">
        <div v-if="selectionMode" class="header-cell checkbox-col"></div>
        <div class="header-cell thumbnail-col">预览</div>
        <div class="header-cell name-col" @click="handleSort('name')">
          文件名
          <span class="sort-icon" v-if="sortField === 'name'">
            {{ sortOrder === 'asc' ? '↑' : '↓' }}
          </span>
        </div>
        <div class="header-cell size-col" @click="handleSort('size')">
          大小
          <span class="sort-icon" v-if="sortField === 'size'">
            {{ sortOrder === 'asc' ? '↑' : '↓' }}
          </span>  
        </div>
        <div class="header-cell date-col" @click="handleSort('date')">
          上传时间
          <span class="sort-icon" v-if="sortField === 'date'">
            {{ sortOrder === 'asc' ? '↑' : '↓' }}
          </span>
        </div>
        <div v-if="!selectionMode" class="header-cell actions-col">操作</div>
      </div>

      <!-- List content -->
      <div class="list-body">
        <div
          v-for="image in sortedImages"
          :key="image.fileId"
          class="list-item"
          :class="{ 'selected': isImageSelected(image.fileId) }"
          @click="handleImageClick(image)"
        >
          <div v-if="selectionMode" class="list-cell checkbox-col" @click.stop="handleToggleSelect(image.fileId)">
            <a-checkbox :checked="isImageSelected(image.fileId)" />
          </div>
          <div class="list-cell thumbnail-col">
            <div class="thumbnail-wrapper">
              <a-image 
                :src="image.thumbnailUrl || image.fileUrl" 
                :alt="image.originalFilename"
                :preview="false"
                class="list-thumbnail"
                @error="handleImageError"
              />
            </div>
          </div>
          
          <div class="list-cell name-col">
            <div class="file-name">{{ image.originalFilename }}</div>
            <div class="file-type">
              <span>{{ getFileExtension(image.originalFilename) }}</span>
              <!-- Tag display -->
              <div v-if="image.tags && image.tags.length > 0" class="image-tags">
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
          
          <div class="list-cell size-col">
            {{ formatFileSize(image.fileSize) }}
          </div>
          
          <div class="list-cell date-col">
            <div class="date-primary">{{ formatDate(image.createdAt) }}</div>
            <div class="date-secondary">{{ formatDateTime(image.createdAt) }}</div>
          </div>
          
          <div v-if="!selectionMode" class="list-cell actions-col">
            <div class="action-buttons">
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handlePreview(image)"
                title="预览"
              >
                <EyeOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handleDownload(image)"
                title="下载"
              >
                <DownloadOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handleRename(image)"
                title="重命名"
              >
                <EditOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                @click.stop="handleManageTags(image)"
                title="管理标签"
              >
                <TagOutlined />
              </a-button>
              <a-button 
                type="text" 
                size="small" 
                danger
                @click.stop="handleDelete(image)"
                title="删除"
              >
                <DeleteOutlined />
              </a-button>
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
    <div v-if="!loading && images.length === 0" class="empty-list">
      <PictureOutlined :style="{ fontSize: '48px', color: '#d9d9d9' }" />
      <p>暂无图片</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  PictureOutlined, 
  EyeOutlined,
  DownloadOutlined,
  EditOutlined,
  TagOutlined,
  DeleteOutlined 
} from '@ant-design/icons-vue'
import { formatFileSize } from '../../utils/file'
import { formatDateTime } from '../../utils/time'
import type { FileInfoResponse } from '../../types/file'

// Props
interface Props {
  images: FileInfoResponse[]
  loading?: boolean
  selectionMode?: boolean
  selectedFileIds?: Set<number>
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  selectionMode: false
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

// Sort state
const sortField = ref<'name' | 'size' | 'date'>('date')
const sortOrder = ref<'asc' | 'desc'>('desc')

// Sorted image list
const sortedImages = computed(() => {
  const sorted = [...props.images].sort((a, b) => {
    let compareValue = 0
    
    switch (sortField.value) {
      case 'name':
        compareValue = a.originalFilename.localeCompare(b.originalFilename)
        break
      case 'size':
        compareValue = a.fileSize - b.fileSize
        break
      case 'date':
        compareValue = new Date(a.createdAt).getTime() - new Date(b.createdAt).getTime()
        break
    }
    
    return sortOrder.value === 'asc' ? compareValue : -compareValue
  })
  
  return sorted
})

// Handle sort
const handleSort = (field: 'name' | 'size' | 'date') => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = field === 'date' ? 'desc' : 'asc'
  }
}

// Format date (relative time, e.g., "today", "yesterday", etc.)
// Note: This function differs from formatDate in utils/time.ts, keeping local implementation
const formatDate = (dateString: string): string => {
  const date = new Date(dateString)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  
  if (days === 0) return '今天'
  if (days === 1) return '昨天'
  if (days < 7) return `${days}天前`
  if (days < 30) return `${Math.floor(days / 7)}周前`
  if (days < 365) return `${Math.floor(days / 30)}个月前`
  return `${Math.floor(days / 365)}年前`
}

// formatDateTime imported from utils/time.ts
// formatFileSize imported from utils/file.ts

// Get file extension
const getFileExtension = (filename: string): string => {
  const extension = filename.split('.').pop()?.toUpperCase()
  return extension || ''
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
.list-view {
  width: 100%;
}

.list-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow: hidden;
}

.list-header {
  display: grid;
  grid-template-columns: 80px 1fr 120px 160px 120px;
  gap: 16px;
  padding: 16px 20px;
  background: #fafafa;
  border-bottom: 1px solid #e5e7eb;
  font-weight: 600;
  font-size: 14px;
  color: #374151;
}

.list-header:has(.checkbox-col) {
  grid-template-columns: 50px 80px 1fr 120px 160px;
}

.header-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s ease;
}

.checkbox-col {
  justify-content: center;
}

.header-cell:hover {
  color: #1890ff;
}

.sort-icon {
  margin-left: 4px;
  font-size: 12px;
  color: #1890ff;
}

.list-body {
  max-height: 600px;
  overflow-y: auto;
}

.list-item {
  display: grid;
  grid-template-columns: 80px 1fr 120px 160px 120px;
  gap: 16px;
  padding: 16px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s ease;
}

.list-item:has(.checkbox-col) {
  grid-template-columns: 50px 80px 1fr 120px 160px;
}

.list-item.selected {
  background-color: #e6f7ff;
  border-left: 3px solid #1890ff;
}

.list-item:hover {
  background: #f8faff;
}

.list-item:last-child {
  border-bottom: none;
}

.list-cell {
  display: flex;
  align-items: center;
  min-height: 60px;
}

.thumbnail-col .thumbnail-wrapper {
  width: 60px;
  height: 45px;
  border-radius: 6px;
  overflow: hidden;
  background: #f5f5f5;
}

.thumbnail-wrapper :deep(.list-thumbnail) {
  width: 100%;
  height: 100%;
}

.thumbnail-wrapper :deep(.list-thumbnail img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 4px;
}

.name-col {
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
}

.file-type {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-type .image-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.file-type .image-tags :deep(.ant-tag) {
  margin: 0;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.file-type {
  font-size: 12px;
  color: #6b7280;
  background: #f3f4f6;
  padding: 2px 6px;
  border-radius: 4px;
  font-weight: 500;
}

.size-col {
  font-size: 14px;
  color: #4b5563;
  font-weight: 500;
}

.date-col {
  flex-direction: column;
  align-items: flex-start;
  gap: 2px;
}

.date-primary {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.date-secondary {
  font-size: 12px;
  color: #6b7280;
}

.actions-col .action-buttons {
  display: flex;
  gap: 4px;
  opacity: 1;
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

.empty-list {
  text-align: center;
  padding: 60px 20px;
  color: #9ca3af;
}

.empty-list p {
  margin-top: 16px;
  font-size: 16px;
}

/* Responsive design */
@media (max-width: 1024px) {
  .list-header,
  .list-item {
    grid-template-columns: 60px 1fr 100px 140px 100px;
    gap: 12px;
    padding: 12px 16px;
  }
  
  .thumbnail-col .thumbnail-wrapper {
    width: 50px;
    height: 38px;
  }
}

@media (max-width: 768px) {
  .list-header,
  .list-item {
    grid-template-columns: 50px 1fr 80px 120px 80px;
    gap: 8px;
    padding: 10px 12px;
  }
  
  .thumbnail-col .thumbnail-wrapper {
    width: 40px;
    height: 30px;
  }
  
  .list-header {
    font-size: 12px;
  }
  
  .file-name {
    font-size: 13px;
  }
  
  .size-col,
  .date-primary {
    font-size: 13px;
  }
}

@media (max-width: 640px) {
  .list-header,
  .list-item {
    grid-template-columns: 50px 1fr 70px 80px;
    gap: 8px;
  }
  
  .date-col {
    display: none;
  }
  
  .list-header .date-col {
    display: none;
  }
}
</style>
