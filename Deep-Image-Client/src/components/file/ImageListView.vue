<template>
  <div class="list-view">
    <div class="list-container">
      <!-- 表头 -->
      <div class="list-header">
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
        <div class="header-cell actions-col">操作</div>
      </div>

      <!-- 列表内容 -->
      <div class="list-body">
        <div
          v-for="image in sortedImages"
          :key="image.fileId"
          class="list-item"
          @click="handleImageClick(image)"
        >
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
              <!-- 标签显示 -->
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
          
          <div class="list-cell actions-col">
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

    <!-- 加载更多指示器 -->
    <div v-if="loading" class="loading-indicator">
      <a-spin size="large" />
      <p>加载中...</p>
    </div>

    <!-- 空状态 -->
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

// 排序状态
const sortField = ref<'name' | 'size' | 'date'>('date')
const sortOrder = ref<'asc' | 'desc'>('desc')

// 排序后的图片列表
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

// 处理排序
const handleSort = (field: 'name' | 'size' | 'date') => {
  if (sortField.value === field) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortField.value = field
    sortOrder.value = field === 'date' ? 'desc' : 'asc'
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 格式化日期
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

// 格式化详细时间
const formatDateTime = (dateString: string): string => {
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 获取文件扩展名
const getFileExtension = (filename: string): string => {
  const extension = filename.split('.').pop()?.toUpperCase()
  return extension || ''
}

// 图片加载错误处理
const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = '/placeholder-image.png' // 设置占位图
}

// 事件处理
const handleImageClick = (image: FileInfoResponse) => {
  // 点击行不做任何操作
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

.header-cell {
  display: flex;
  align-items: center;
  cursor: pointer;
  user-select: none;
  transition: color 0.2s ease;
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

/* 响应式设计 */
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
