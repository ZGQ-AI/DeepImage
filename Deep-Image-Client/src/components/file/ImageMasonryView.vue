<template>
  <div class="grid-view">
    <div class="grid-container">
      <div
        v-for="image in shuffledImages"
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
            <!-- More action button (only visible on hover, positioned at bottom right of image) -->
            <div v-if="!selectionMode && isOwner(image)" class="image-more-button">
              <a-dropdown 
                :trigger="['click']"
                placement="bottomRight"
              >
                <a-button 
                  type="text" 
                  size="small"
                  class="more-btn"
                  @click.stop
                >
                  <MoreOutlined />
                </a-button>
                <template #overlay>
                  <a-menu @click="(e: { key: string }) => handleMenuClick(e, image)">
                    <a-menu-item key="download">
                      <DownloadOutlined /> 下载
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="details">
                      <EditOutlined /> 文件详情
                    </a-menu-item>
                    <a-menu-item key="tags">
                      <TagOutlined /> 管理标签
                    </a-menu-item>
                    <a-menu-divider />
                    <a-menu-item key="delete" danger>
                      <DeleteOutlined /> 删除
                    </a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
          </div>
          
          <!-- Image info area (always visible) -->
          <div class="image-card-info">
            <p class="image-card-title">{{ getFileNameWithoutExtension(image.originalFilename, image.fileExtension) }}</p>
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
    <div v-if="!loading && shuffledImages.length === 0" class="empty-grid">
      <PictureOutlined :style="{ fontSize: '48px', color: '#d9d9d9' }" />
      <p>暂无图片</p>
    </div>

    <!-- File Details Drawer -->
    <FileMetadataDrawer
      v-model:open="metadataDrawerOpen"
      :file-info="selectedFileForMetadata"
      @save="handleDrawerSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { 
  PictureOutlined, 
  DownloadOutlined,
  EditOutlined,
  TagOutlined,
  DeleteOutlined,
  MoreOutlined
} from '@ant-design/icons-vue'
import { formatFileSize } from '../../utils/file'
import { useUserStore } from '../../stores/useUserStore'
import type { FileInfoResponse } from '../../types/file'
import FileMetadataDrawer from './FileMetadataDrawer.vue'

// Get current user
const userStore = useUserStore()

// Get filename without extension
const getFileNameWithoutExtension = (filename: string, fileExtension?: string): string => {
  if (!filename) return ''
  
  // If filename has extension with dot, remove it
  const lastDotIndex = filename.lastIndexOf('.')
  if (lastDotIndex !== -1 && lastDotIndex > 0 && lastDotIndex < filename.length - 1) {
    const ext = filename.substring(lastDotIndex + 1).toLowerCase()
    const fileExt = fileExtension?.replace(/^\./, '').toLowerCase()
    // If the extension matches fileExtension or looks like a valid extension
    if (fileExt === ext || (ext.length >= 1 && ext.length <= 5 && /^[a-zA-Z0-9]+$/.test(ext))) {
      return filename.substring(0, lastDotIndex)
    }
  }
  
  // If filename ends with fileExtension without dot, remove it
  if (fileExtension) {
    const extClean = fileExtension.replace(/^\./, '').toLowerCase()
    if (filename.toLowerCase().endsWith(extClean.toLowerCase())) {
      return filename.substring(0, filename.length - extClean.length)
    }
  }
  
  return filename
}

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

// Shuffled images for better distribution in masonry layout
// Uses deterministic hash-based shuffling to ensure stable but randomized order
const shuffledImages = computed(() => {
  // Only shuffle if we have images
  if (props.images.length === 0) return []
  
  // Create a deterministic hash function based on fileId
  const hash = (n: number): number => {
    let h = n
    h = ((h >> 16) ^ h) * 0x45d9f3b
    h = ((h >> 16) ^ h) * 0x45d9f3b
    h = (h >> 16) ^ h
    return h >>> 0
  }
  
  // Sort images by hash value, which creates a deterministic but seemingly random order
  // This ensures:
  // 1. Same images always appear in the same order (stable)
  // 2. New images will appear in different positions (distributed)
  // 3. Images are evenly distributed across columns
  const shuffled = [...props.images].sort((a, b) => {
    const hashA = hash(a.fileId)
    const hashB = hash(b.fileId)
    return hashA - hashB
  })
  
  return shuffled
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

// Check if current user is the owner of the file
const isOwner = (image: FileInfoResponse): boolean => {
  if (!image.userId) return false
  return image.userId === userStore.profile?.id
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

// Metadata drawer state
const metadataDrawerOpen = ref(false)
const selectedFileForMetadata = ref<FileInfoResponse | null>(null)

const handleViewMetadata = (image: FileInfoResponse) => {
  selectedFileForMetadata.value = image
  metadataDrawerOpen.value = true
}

// Handle save from drawer (filename and/or visibility)
const handleDrawerSave = async (fileId: number, updateData: { originalFilename?: string; visibility?: string }) => {
  // Find the file (search in original images array, not shuffled)
  const image = props.images.find(img => img.fileId === fileId)
  if (image) {
    // Create a new image object with updated properties
    const updatedImage = { 
      ...image, 
      ...(updateData.originalFilename && { originalFilename: updateData.originalFilename }),
      ...(updateData.visibility && { visibility: updateData.visibility })
    }
    // Emit rename event (backward compatible with existing parent handlers)
    // The parent component will detect changes and handle API call
    emit('rename', updatedImage)
  }
}

// Handle menu item click
const handleMenuClick = ({ key }: { key: string }, image: FileInfoResponse) => {
  switch (key) {
    case 'download':
      handleDownload(image)
      break
    case 'details':
      handleViewMetadata(image)
      break
    case 'tags':
      handleManageTags(image)
      break
    case 'delete':
      handleDelete(image)
      break
  }
}
</script>

<style scoped>
.grid-view {
  width: 100%;
  margin: 0;
  padding: 0;
}

.grid-container {
  column-count: 4;
  column-gap: 4px;
  padding: 0;
}

.grid-item {
  position: relative;
  cursor: pointer;
  width: 100%;
  break-inside: avoid;
  margin-bottom: 4px;
}

.grid-item.selected .image-wrapper {
  box-shadow: 0 0 0 3px #1890ff;
}

.image-wrapper {
  position: relative;
  width: 100%;
  border-radius: 8px;
  overflow: hidden;
  background: transparent;
  box-shadow: none;
  transition: all 0.3s ease;
}

.image-wrapper:hover {
  z-index: 5;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.image-container {
  position: relative;
  width: 100%;
  background: #1a1a1a;
  border-radius: 8px 8px 0 0;
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

.grid-image {
  width: 100%;
  height: auto;
  display: block;
  transition: transform 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.image-wrapper:hover .grid-image {
  transform: scale(1.05);
}

/* More button (bottom right corner, only visible on hover) */
.image-more-button {
  position: absolute;
  bottom: 8px;
  right: 8px;
  z-index: 10;
  opacity: 0;
  transition: opacity 0.2s ease, transform 0.2s ease;
  pointer-events: none;
}

.image-wrapper:hover .image-more-button {
  opacity: 1;
  pointer-events: auto;
}

.more-btn {
  background: rgba(0, 0, 0, 0.6) !important;
  backdrop-filter: blur(8px);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
  transition: all 0.2s ease;
  color: white !important;
  border: none !important;
  padding: 0;
}

.more-btn:hover {
  background: rgba(0, 0, 0, 0.8) !important;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.4);
  transform: scale(1.1);
  color: white !important;
}

.more-btn :deep(.anticon) {
  font-size: 16px;
}

/* Image card info area (only visible on hover, overlay style) */
.image-card-info {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px 12px 12px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8) 0%, rgba(0, 0, 0, 0.4) 60%, transparent 100%);
  border-radius: 0 0 8px 8px;
  opacity: 0;
  transition: opacity 0.3s ease;
  pointer-events: none;
}

.image-wrapper:hover .image-card-info {
  opacity: 1;
}

.image-card-title {
  font-size: 14px;
  font-weight: 500;
  color: white;
  margin: 0 0 6px 0;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.image-card-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.image-card-size {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.8);
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.5);
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
  background: rgba(255, 255, 255, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: white;
  backdrop-filter: blur(4px);
}

.image-card-tags :deep(.ant-tag:hover) {
  background: rgba(255, 255, 255, 0.3);
  border-color: rgba(255, 255, 255, 0.5);
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
    column-gap: 3px;
  }
  
  .grid-item {
    margin-bottom: 3px;
  }
  
  .image-card-info {
    padding: 12px 10px 10px;
  }
  
  .image-card-title {
    font-size: 13px;
    margin-bottom: 6px;
  }
}

@media (max-width: 480px) {
  .grid-container {
    column-count: 1;
    column-gap: 0;
  }
  
  .grid-item {
    margin-bottom: 2px;
  }
  
  .image-card-info {
    padding: 12px 8px 8px;
  }
  
  .image-card-title {
    font-size: 12px;
  }
  
  .image-card-size {
    font-size: 11px;
  }
}
</style>
