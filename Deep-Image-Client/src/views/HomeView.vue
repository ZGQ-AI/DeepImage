<template>
  <div class="home-view">
    <div class="home-header">
      <h1>DeepImage 首页</h1>
      <p class="home-description">发现所有用户分享的公开照片</p>
    </div>

    <!-- Public photos gallery -->
    <div class="gallery-container">
      <ImageMasonryView
        v-if="images.length > 0"
        :images="images"
        :loading="loading"
        :selection-mode="false"
        @preview="handleImagePreview"
        @download="handleImageDownload"
        @rename="handleImageRename"
        @manageTags="handleManageTags"
        @delete="handleImageDelete"
      />

      <!-- Loading state -->
      <div v-if="loading && images.length === 0" class="loading-container">
        <a-spin size="large" />
        <p>加载中...</p>
      </div>

      <!-- Empty state -->
      <div v-if="!loading && images.length === 0" class="empty-container">
        <PictureOutlined :style="{ fontSize: '64px', color: '#d9d9d9' }" />
        <h3>暂无公开照片</h3>
        <p>目前还没有用户分享公开照片，请稍后再来看看</p>
      </div>

      <!-- Load more button -->
      <div v-if="images.length > 0 && hasMore" class="load-more-container">
        <a-button type="primary" :loading="loading" @click="loadMore">
          加载更多
        </a-button>
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

    <!-- Image properties modal -->
    <ImagePropertiesModal
      v-model:open="propertiesModalVisible"
      :file-detail="currentFileDetail"
      :loading="updatingProperties"
      @confirm="handlePropertiesConfirm"
    />

    <!-- Tag manager modal -->
    <FileTagManager
      v-model:open="tagManagerVisible"
      :file-id="currentImageForTag?.fileId"
      @tags-updated="handleTagsUpdated"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { PictureOutlined } from '@ant-design/icons-vue'
import ImageMasonryView from '../components/file/ImageMasonryView.vue'
import ImagePropertiesModal from '../components/common/ImagePropertiesModal.vue'
import FileTagManager from '../components/file/FileTagManager.vue'
import { listPublicFiles, downloadFile, batchDeleteFiles, updateFileProperties, getFileDetail } from '../api/file'
import type { FileInfoResponse } from '../types/file'
import type { PageResponse } from '../types/file'
import type { FileDetailResponse } from '../types/file'
import type { TagResponse } from '../types/tag'

// State management
const images = ref<FileInfoResponse[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const currentImageIndex = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

// Properties modal state
const propertiesModalVisible = ref(false)
const currentFileDetail = ref<FileDetailResponse | null>(null)
const currentImageForRename = ref<FileInfoResponse | null>(null)
const updatingProperties = ref(false)

// Tag manager state
const tagManagerVisible = ref(false)
const currentImageForTag = ref<FileInfoResponse | null>(null)

// Load public photos
const loadPublicPhotos = async (page = 1, append = false) => {
  try {
    loading.value = true
    
    const response = await listPublicFiles(page, pageSize.value)
    
    if (response.data.code === 200 && response.data.data) {
      const pageData: PageResponse<FileInfoResponse> = response.data.data
      const records = pageData.records || []
      
      if (append) {
        images.value = [...images.value, ...records]
      } else {
        images.value = records
      }
      
      // Check if there are more pages
      const total = pageData.total || 0
      const currentTotal = images.value.length
      hasMore.value = currentTotal < total
      
      currentPage.value = page
    } else {
      message.error('加载公开照片失败')
    }
  } catch (error: any) {
    console.error('Failed to load public photos:', error)
    message.error('加载公开照片失败：' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// Load more photos
const loadMore = () => {
  if (!loading.value && hasMore.value) {
    loadPublicPhotos(currentPage.value + 1, true)
  }
}

// Handle image preview
const handleImagePreview = (image: FileInfoResponse) => {
  // Find current image index in list
  currentImageIndex.value = images.value.findIndex(img => img.fileId === image.fileId)
  if (currentImageIndex.value === -1) {
    currentImageIndex.value = 0
  }
  
  previewVisible.value = true
}

// Handle image download
const handleImageDownload = async (image: FileInfoResponse) => {
  try {
    message.loading({
      content: '正在下载...',
      key: `download-${image.fileId}`,
      duration: 0
    })

    const response = await downloadFile(image.fileId)
    
    // Get filename from response headers
    const contentDisposition = response.headers['content-disposition']
    let filename = image.originalFilename
    
    if (contentDisposition) {
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
    
    document.body.appendChild(link)
    link.click()
    
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    message.success({
      content: `下载成功: ${filename}`,
      key: `download-${image.fileId}`
    })
  } catch (error) {
    console.error('Failed to download image:', error)
    message.error({
      content: `下载失败: ${error instanceof Error ? error.message : '未知错误'}`,
      key: `download-${image.fileId}`
    })
  }
}

// Handle image rename
const handleImageRename = async (image: FileInfoResponse) => {
  currentImageForRename.value = image
  try {
    const response = await getFileDetail(image.fileId, true)
    if (response.data.code === 200 && response.data.data) {
      currentFileDetail.value = response.data.data
      propertiesModalVisible.value = true
    } else {
      message.error('获取文件详情失败')
    }
  } catch (error) {
    console.error('Failed to get file detail:', error)
    message.error('获取文件详情失败')
  }
}

// Handle properties confirm
const handlePropertiesConfirm = async (updateData: { originalFilename?: string; visibility?: string }) => {
  if (!currentImageForRename.value) return
  
  const image = currentImageForRename.value
  
  try {
    updatingProperties.value = true
    message.loading({
      content: '正在更新文件属性...',
      key: `update-props-${image.fileId}`,
      duration: 0
    })

    const response = await updateFileProperties(image.fileId, {
      originalFilename: updateData.originalFilename,
      visibility: updateData.visibility as 'PRIVATE' | 'PUBLIC' | undefined
    })
    
    if (response.data.code === 200 && response.data.data) {
      propertiesModalVisible.value = false
      
      // Reload public photos to reflect changes
      await loadPublicPhotos(currentPage.value, false)
      
      message.success({
        content: '文件属性更新成功',
        key: `update-props-${image.fileId}`
      })
    } else {
      throw new Error(response.data.message || '更新失败')
    }
  } catch (error) {
    console.error('Failed to update file properties:', error)
    message.error({
      content: `更新失败: ${error instanceof Error ? error.message : '未知错误'}`,
      key: `update-props-${image.fileId}`
    })
  } finally {
    updatingProperties.value = false
  }
}

// Handle manage tags
const handleManageTags = (image: FileInfoResponse) => {
  currentImageForTag.value = image
  tagManagerVisible.value = true
}

// Handle tags updated
const handleTagsUpdated = (tags: TagResponse[]) => {
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
}

// Handle image delete
const handleImageDelete = async (image: FileInfoResponse) => {
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除图片 "${image.originalFilename}" 吗？`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        message.loading({
          content: '正在删除图片...',
          key: `delete-${image.fileId}`,
          duration: 0
        })

        const response = await batchDeleteFiles([image.fileId])
        
        if (response.data.code === 200 && response.data.data) {
          const result = response.data.data
          if (result.success > 0) {
            // Remove image from list
            images.value = images.value.filter(img => img.fileId !== image.fileId)
            
            message.success({
              content: `删除成功: ${image.originalFilename}`,
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

// Load photos on mount
onMounted(() => {
  loadPublicPhotos(1, false)
})
</script>

<style scoped>
.home-view {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.home-header {
  text-align: center;
  margin-bottom: 48px;
}

.home-header h1 {
  font-size: 36px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 12px;
}

.home-description {
  font-size: 18px;
  color: #6b7280;
  margin: 0;
}

.gallery-container {
  min-height: 400px;
}

.loading-container,
.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 24px;
  text-align: center;
}

.loading-container p {
  margin-top: 16px;
  color: #6b7280;
}

.empty-container h3 {
  font-size: 20px;
  color: #374151;
  margin: 24px 0 8px;
}

.empty-container p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.load-more-container {
  display: flex;
  justify-content: center;
  padding: 32px 0;
}

@media (max-width: 768px) {
  .home-view {
    padding: 16px;
  }

  .home-header h1 {
    font-size: 28px;
  }

  .home-description {
    font-size: 16px;
  }
}
</style>
