<template>
  <div class="image-gallery">
    <div class="gallery-header">
      <h1>{{ PAGE_TITLES.GALLERY }}</h1>
      <p class="gallery-description">{{ PAGE_DESCRIPTIONS.GALLERY }}</p>
    </div>

    <!-- Show toolbar when there are images -->
    <div v-if="images.length > 0" class="gallery-toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="showUploader = !showUploader">
          <UploadOutlined />
          {{ showUploader ? BUTTON_TEXTS.UPLOAD_COLLAPSE : BUTTON_TEXTS.UPLOAD }}
        </a-button>
        <a-divider type="vertical" />
        <a-button @click="toggleSelectionMode">
          {{ selectionMode ? BUTTON_TEXTS.EXIT_SELECTION : BUTTON_TEXTS.BATCH_OPERATE }}
        </a-button>
        <a-divider type="vertical" />
        <span class="toolbar-label">
          共 {{ images.length }} 张图片
          <template v-if="selectionMode && selectedFileIds.size > 0">
            (已选 {{ selectedFileIds.size }} 张)
          </template>
        </span>
      </div>
      <div class="toolbar-right">
        <!-- Filename search -->
        <a-input-search
          v-model:value="searchKeyword"
          :placeholder="PLACEHOLDERS.SEARCH_FILE_NAME"
          style="width: 200px; margin-right: 16px;"
          allowClear
          @search="handleSearch"
          @change="handleSearchChange"
        />
        
        <!-- Sort selection -->
        <a-select
          v-model:value="sortOption"
          style="width: 160px; margin-right: 16px;"
          @change="handleSortChange"
        >
          <a-select-option :value="SORT_OPTIONS.CREATED_AT_DESC.value">{{ SORT_OPTIONS.CREATED_AT_DESC.label }}</a-select-option>
          <a-select-option :value="SORT_OPTIONS.CREATED_AT_ASC.value">{{ SORT_OPTIONS.CREATED_AT_ASC.label }}</a-select-option>
          <a-select-option :value="SORT_OPTIONS.FILE_SIZE_DESC.value">{{ SORT_OPTIONS.FILE_SIZE_DESC.label }}</a-select-option>
          <a-select-option :value="SORT_OPTIONS.FILE_SIZE_ASC.value">{{ SORT_OPTIONS.FILE_SIZE_ASC.label }}</a-select-option>
          <a-select-option :value="SORT_OPTIONS.FILENAME_ASC.value">{{ SORT_OPTIONS.FILENAME_ASC.label }}</a-select-option>
          <a-select-option :value="SORT_OPTIONS.FILENAME_DESC.value">{{ SORT_OPTIONS.FILENAME_DESC.label }}</a-select-option>
        </a-select>
        
        <!-- Tag filter -->
        <a-select
          v-model:value="selectedTagId"
          :placeholder="PLACEHOLDERS.FILTER_BY_TAG"
          style="width: 150px; margin-right: 16px;"
          allowClear
          @change="handleTagFilterChange"
        >
          <a-select-option :value="null">{{ PLACEHOLDERS.ALL_TAGS }}</a-select-option>
          <a-select-option 
            v-for="tag in availableTags" 
            :key="tag.id" 
            :value="tag.id"
          >
            <a-tag :color="tag.color || 'blue'" style="margin-right: 4px;" size="small">
              {{ tag.tagName }}
            </a-tag>
            <span style="color: #999;">({{ tag.usageCount }})</span>
          </a-select-option>
        </a-select>
        
        <ViewModeToggle v-model="viewMode" @change="handleViewModeChange" />
      </div>
    </div>

    <!-- Upload area -->
    <div v-if="shouldShowUploader" class="upload-section">
      <ImageUploader 
        :max-size="UPLOAD_CONFIG.MAX_SIZE" 
        :max-count="UPLOAD_CONFIG.MAX_COUNT"
        @success="handleUploadSuccess"
        @error="handleUploadError"
      />
    </div>

    <!-- Batch operation toolbar -->
    <BatchToolbar
      v-if="selectionMode"
      :is-all-selected="isAllSelected"
      :selected-count="selectedFileIds.size"
      :total-count="images.length"
      total-text-template="共 {count} 张图片"
      selected-text-template="已选 {count} 张"
      @select-all="handleSelectAll"
    >
      <template #actions>
        <a-button 
          type="primary" 
          danger 
          :disabled="selectedFileIds.size === 0"
          @click="handleBatchDelete"
        >
          {{ BUTTON_TEXTS.DELETE }} ({{ selectedFileIds.size }})
        </a-button>
      </template>
    </BatchToolbar>

    <!-- Image display area -->
    <div class="gallery-content">
      <!-- Image list - display according to view mode -->
      <div v-if="images.length > 0" class="images-container">
        <!-- Grid view -->
        <ImageMasonryView
          v-if="viewMode === 'grid'"
          :images="images"
          :loading="loading"
          :selection-mode="selectionMode"
          :selected-file-ids="selectedFileIds"
          @preview="handleImagePreview"
          @download="handleImageDownload"
          @rename="handleImageRename"
          @manage-tags="handleManageTags"
          @delete="handleImageDelete"
          @toggle-select="handleToggleSelect"
        />
        
        <!-- List view -->
        <ImageListView
          v-else-if="viewMode === 'list'"
          :images="images"
          :loading="loading"
          :selection-mode="selectionMode"
          :selected-file-ids="selectedFileIds"
          @preview="handleImagePreview"
          @download="handleImageDownload"
          @rename="handleImageRename"
          @manage-tags="handleManageTags"
          @delete="handleImageDelete"
          @toggle-select="handleToggleSelect"
        />
      </div>
    </div>

    <!-- Image preview modal -->
    <a-image-preview-group 
      :preview="{
        visible: previewVisible,
        onVisibleChange: (visible: boolean) => { previewVisible = visible },
        current: currentImageIndex
      }"
    >
      <a-image
        v-for="image in images"
        :key="image.fileId"
        :src="image.fileUrl"
        :alt="image.originalFilename"
        style="display: none"
      />
    </a-image-preview-group>

    <!-- Tag management modal -->
    <FileTagManager
      v-model:open="tagManagerVisible"
      :file-info="currentImageForTag"
      @tags-updated="handleTagsUpdated"
    />

  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import ImageUploader from '../components/file/ImageUploader.vue'
import ImageMasonryView from '../components/file/ImageMasonryView.vue'
import ImageListView from '../components/file/ImageListView.vue'
import ViewModeToggle from '../components/file/ViewModeToggle.vue'
import FileTagManager from '../components/file/FileTagManager.vue'
import BatchToolbar from '../components/common/BatchToolbar.vue'
import { listFiles, downloadFile, batchDeleteFiles, updateFileProperties } from '../api/file'
import { listTags } from '../api/tag'
import { BusinessType } from '../types/file'
import { PAGE_TITLES, PAGE_DESCRIPTIONS, BUTTON_TEXTS, MESSAGES, UPLOAD_CONFIG, PAGINATION_CONFIG, SORT_OPTIONS, PLACEHOLDERS, STYLE_CONFIG } from '../config/constants'
import type { FileInfoResponse } from '../types/file'
import type { ViewMode } from '../components/file/ViewModeToggle.vue'
import type { TagResponse } from '../types/tag'

// State management
const showUploader = ref(false)
const images = ref<FileInfoResponse[]>([])
const loading = ref(false)
const viewMode = ref<ViewMode>('grid')
const previewVisible = ref(false)
const previewImage = ref<FileInfoResponse | null>(null)
const currentImageIndex = ref(0)
const tagManagerVisible = ref(false)
const currentImageForTag = ref<FileInfoResponse | null>(null)
const availableTags = ref<TagResponse[]>([])
const selectedTagId = ref<number | null>(null)

// Batch operation state
const selectionMode = ref(false)
const selectedFileIds = ref<Set<number>>(new Set())

// Search and sort
const searchKeyword = ref<string>('')
const sortOption = ref<string>('createdAt-desc') // Default sort by upload time descending
let searchTimer: any = null

// Compute whether to show uploader
const shouldShowUploader = computed(() => {
  // If no images, always show uploader
  if (images.value.length === 0) return true
  // If images exist, controlled by user
  return showUploader.value
})

// Compute if all selected
const isAllSelected = computed(() => {
  return images.value.length > 0 && selectedFileIds.value.size === images.value.length
})

// formatFileSize is imported from utils/file.ts

// Load available tags
const loadTags = async () => {
  try {
    const response = await listTags()
    if (response.data.code === 200) {
      availableTags.value = response.data.data || []
    }
  } catch (error) {
    console.error('Failed to load tags:', error)
  }
}

// Load image list
const loadImages = async () => {
  try {
    loading.value = true
    
    // Parse sort option
    const [sortBy, sortOrder] = sortOption.value.split('-')
    
    // Use unified listFiles interface
    const response = await listFiles({
      businessType: BusinessType.IMAGE,
      tagId: selectedTagId.value || undefined,  // If tag selected, pass tag ID
      filename: searchKeyword.value || undefined,  // Filename search
      sortBy,  // Sort field
      sortOrder,  // Sort direction
      page: 1,
      pageSize: PAGINATION_CONFIG.DEFAULT_PAGE_SIZE
    })

    // API response structure: { code, message, data: { records, total, ... } }
    images.value = response.data.data?.records || []
  } catch (error: any) {
    console.error('Failed to load images:', error)
    message.error(MESSAGES.LOADING_IMAGES)
  } finally {
    loading.value = false
  }
}

// Handle upload success
const handleUploadSuccess = (newImages: any[]) => {
  // Add newly uploaded images to the front of the list
  images.value.unshift(...newImages)
  message.success(MESSAGES.UPLOAD_SUCCESS(newImages.length))
  
  // If no images before, now have images, can hide uploader
  // If images existed before, controlled by user preference
  if (images.value.length === newImages.length) {
    // First upload, keep uploader visible for continued uploads
    showUploader.value = false
  } else {
    // Images already exist, auto-collapse uploader
    showUploader.value = false
  }
}

// Handle upload error
const handleUploadError = (error: any) => {
  console.error('Upload failed:', error)
}

// Handle view mode change
const handleViewModeChange = (mode: ViewMode) => {
  // Save user preference to local storage
  localStorage.setItem('gallery-view-mode', mode)
}

// Handle search (debounced)
const handleSearchChange = () => {
  // Clear previous timer
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
    // Set new timer (execute search after 500ms)
    searchTimer = setTimeout(() => {
      loadImages()
    }, STYLE_CONFIG.DEBOUNCE_TIMEOUT)
}

// Immediate search (when Enter pressed or search button clicked)
const handleSearch = () => {
  // Clear debounce timer
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  // Execute search immediately
  loadImages()
}

// Handle sort change
const handleSortChange = () => {
  loadImages()
}

// Handle image operations
const handleImagePreview = (image: FileInfoResponse) => {
  // Find current image index in list
  currentImageIndex.value = images.value.findIndex(img => img.fileId === image.fileId)
  if (currentImageIndex.value === -1) {
    currentImageIndex.value = 0
  }
  
  previewImage.value = image
  previewVisible.value = true
}

const handleImageDownload = async (image: FileInfoResponse) => {
  try {
    message.loading({
      content: MESSAGES.DOWNLOAD_START,
      key: `download-${image.fileId}`,
      duration: 0
    })

    // Call backend download API
    const response = await downloadFile(image.fileId)
    
    // Get filename from response headers
    const contentDisposition = response.headers['content-disposition']
    let filename = image.originalFilename
    
    if (contentDisposition) {
      // Parse Content-Disposition header to get filename
      const filenameMatch = contentDisposition.match(/filename\*=UTF-8''(.+)/)
      if (filenameMatch && filenameMatch[1]) {
        filename = decodeURIComponent(filenameMatch[1])
      } else {
        const simpleMatch = contentDisposition.match(/filename="?(.+?)"?$/)
        if (simpleMatch && simpleMatch[1]) {
          filename = simpleMatch[1]
        }
      }
    }

    // Create Blob and download link
    const blob = new Blob([response.data])
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.style.display = 'none'
    
    // Trigger download
    document.body.appendChild(link)
    link.click()
    
    // Cleanup
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    message.success({
      content: MESSAGES.DOWNLOAD_SUCCESS(filename),
      key: `download-${image.fileId}`
    })

  } catch (error) {
    console.error('Failed to download image:', error)
    message.error({
      content: MESSAGES.DOWNLOAD_ERROR(error instanceof Error ? error.message : '未知错误'),
      key: `download-${image.fileId}`
    })
  }
}

const handleImageRename = async (image: FileInfoResponse) => {
  // Check if this is from drawer save (has updated properties)
  // We need to check against the current image in the list
  const currentImage = images.value.find(img => img.fileId === image.fileId)
  const hasFilenameChange = currentImage && currentImage.originalFilename !== image.originalFilename
  const hasVisibilityChange = currentImage && (currentImage.visibility || 'PRIVATE') !== (image.visibility || 'PRIVATE')
  const isDrawerSave = hasFilenameChange || hasVisibilityChange
  
  if (isDrawerSave) {
    // Direct update from drawer (filename and/or visibility)
    try {
      message.loading({
        content: '正在更新文件属性...',
        key: `update-${image.fileId}`,
        duration: 0
      })

      const updateData: { originalFilename?: string; visibility?: 'PRIVATE' | 'PUBLIC' } = {}
      if (hasFilenameChange) {
        updateData.originalFilename = image.originalFilename
      }
      if (hasVisibilityChange) {
        updateData.visibility = (image.visibility || 'PRIVATE') as 'PRIVATE' | 'PUBLIC'
      }

      const response = await updateFileProperties(image.fileId, updateData)
      
      if (response.data.code === 200) {
        // Reload image list to reflect changes
        await loadImages()
        
        const changes = []
        if (hasFilenameChange) changes.push(`文件名：${image.originalFilename}`)
        if (hasVisibilityChange) changes.push(`可见性：${image.visibility === 'PUBLIC' ? '公开' : '私有'}`)
        
        message.success({
          content: `更新成功：${changes.join('，')}`,
          key: `update-${image.fileId}`
        })
      } else {
        throw new Error(response.data.message || '更新失败')
      }
    } catch (error) {
      console.error('Failed to update file properties:', error)
      message.error({
        content: `更新失败: ${error instanceof Error ? error.message : '未知错误'}`,
        key: `update-${image.fileId}`
      })
    }
  }
}

// Handle tag filter change
const handleTagFilterChange = () => {
  loadImages()
}

// Manage tags
const handleManageTags = (image: FileInfoResponse) => {
  currentImageForTag.value = image
  tagManagerVisible.value = true
}

// Callback after tags updated
const handleTagsUpdated = (tags: TagResponse[]) => {
  // Update current image's tags
  if (currentImageForTag.value) {
    const imageIndex = images.value.findIndex(img => img.fileId === currentImageForTag.value!.fileId)
    if (imageIndex !== -1) {
      images.value[imageIndex].tags = tags.map(tag => ({
        tagId: tag.id,
        tagName: tag.tagName,
        color: tag.color
      }))
    }
  }
  
  // Reload tag list
  loadTags()
}

const handleImageDelete = async (image: FileInfoResponse) => {
  // Use Modal to confirm deletion
  Modal.confirm({
    title: '确认删除',
    content: MESSAGES.DELETE_CONFIRM(1).replace('选中的', `图片 "${image.originalFilename}"`).replace('张图片', ''),
    okText: BUTTON_TEXTS.CONFIRM_DELETE,
    okType: 'danger',
    cancelText: BUTTON_TEXTS.CANCEL,
    onOk: async () => {
      try {
        message.loading({
          content: '正在删除图片...',
          key: `delete-${image.fileId}`,
          duration: 0
        })

        // Call batch delete API (single file)
        const response = await batchDeleteFiles([image.fileId])
        
        if (response.data.code === 200 && response.data.data) {
          const result = response.data.data
          if (result.success > 0) {
            // Remove image from list
            images.value = images.value.filter(img => img.fileId !== image.fileId)
            
            message.success({
              content: MESSAGES.DELETE_SUCCESS(image.originalFilename),
              key: `delete-${image.fileId}`
            })
          } else {
            throw new Error('删除失败')
          }
        } else {
          throw new Error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('Failed to delete image:', error)
        message.error({
          content: `删除失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: `delete-${image.fileId}`
        })
      }
    }
  })
}

// Toggle selection mode
const toggleSelectionMode = () => {
  selectionMode.value = !selectionMode.value
  if (!selectionMode.value) {
    selectedFileIds.value.clear()
  }
}

// Toggle single file selection
const handleToggleSelect = (fileId: number) => {
  if (selectedFileIds.value.has(fileId)) {
    selectedFileIds.value.delete(fileId)
  } else {
    selectedFileIds.value.add(fileId)
  }
}

// Select all / deselect all
const handleSelectAll = (checked: boolean) => {
  if (checked) {
    images.value.forEach(img => selectedFileIds.value.add(img.fileId))
  } else {
    selectedFileIds.value.clear()
  }
}

// Batch delete
const handleBatchDelete = async () => {
  const count = selectedFileIds.value.size
  if (count === 0) {
    message.warning(MESSAGES.NO_SELECTION)
    return
  }

  Modal.confirm({
    title: '批量删除确认',
    content: MESSAGES.DELETE_CONFIRM(count),
    okText: BUTTON_TEXTS.CONFIRM_DELETE,
    okType: 'danger',
    cancelText: BUTTON_TEXTS.CANCEL,
    onOk: async () => {
      try {
        message.loading({
          content: '正在删除图片...',
          key: 'batch-delete',
          duration: 0
        })

        const fileIds = Array.from(selectedFileIds.value)
        const response = await batchDeleteFiles(fileIds)
        
        if (response.data.code === 200 && response.data.data) {
          const result = response.data.data
          
          // Remove successfully deleted images from list
          images.value = images.value.filter(img => !fileIds.includes(img.fileId))
          
          // Clear selection
          selectedFileIds.value.clear()
          selectionMode.value = false
          
          if (result.failed > 0) {
            message.warning({
              content: MESSAGES.BATCH_DELETE_PARTIAL(result.success, result.failed),
              key: 'batch-delete'
            })
          } else {
            message.success({
              content: MESSAGES.BATCH_DELETE_SUCCESS(result.success),
              key: 'batch-delete'
            })
          }
        } else {
          throw new Error(response.data.message || '批量删除失败')
        }
      } catch (error) {
        console.error('Failed to batch delete:', error)
        message.error({
          content: `批量删除失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: 'batch-delete'
        })
      }
    }
  })
}

// Initialize user preferences
const initUserPreferences = () => {
  const savedViewMode = localStorage.getItem('gallery-view-mode') as ViewMode
  if (savedViewMode && ['grid', 'list'].includes(savedViewMode)) {
    viewMode.value = savedViewMode
  }
}

// Load image list and user preferences on page mount
onMounted(() => {
  initUserPreferences()
  loadImages()
  loadTags()
})
</script>

<style scoped>
.image-gallery {
  padding: 0;
  max-width: 100%;
  margin: 0;
  width: 100%;
  margin-left: -20px;
  margin-right: -20px;
  width: calc(100% + 40px);
}

.gallery-header {
  text-align: center;
  margin-bottom: 48px;
  padding: 0 24px;
}

.gallery-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.gallery-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.gallery-content {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 48px 24px;
}

.empty-icon {
  margin-bottom: 24px;
}

.empty-state h3 {
  font-size: 20px;
  color: #374151;
  margin-bottom: 8px;
}

.empty-state p {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 24px;
}

.gallery-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 24px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

.upload-section {
  margin-bottom: 32px;
  padding: 24px;
  margin-left: 24px;
  margin-right: 24px;
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  margin-bottom: 16px;
  margin-left: 24px;
  margin-right: 24px;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
}

.batch-toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-toolbar-right {
  display: flex;
  gap: 12px;
}

.images-container {
  width: 100%;
  margin: 0;
  padding: 0;
}

.image-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  cursor: pointer;
}

.image-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.12);
  transform: translateY(-2px);
}

.image-card img {
  width: 100%;
  height: 200px;
  object-fit: cover;
  display: block;
}

.image-info {
  padding: 12px 16px;
}

.image-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin: 0 0 4px 0;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.image-size {
  font-size: 12px;
  color: #6b7280;
  margin: 0;
}
</style>
