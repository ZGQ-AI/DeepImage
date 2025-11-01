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
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { PictureOutlined } from '@ant-design/icons-vue'
import ImageMasonryView from '../components/file/ImageMasonryView.vue'
import { listPublicFiles } from '../api/file'
import type { FileInfoResponse } from '../types/file'
import type { PageResponse } from '../types/file'

// State management
const images = ref<FileInfoResponse[]>([])
const loading = ref(false)
const previewVisible = ref(false)
const currentImageIndex = ref(0)
const currentPage = ref(1)
const pageSize = ref(20)
const hasMore = ref(true)

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
