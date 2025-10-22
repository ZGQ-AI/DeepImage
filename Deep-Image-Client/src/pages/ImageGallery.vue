<template>
  <div class="image-gallery">
    <div class="gallery-header">
      <h1>我的图库</h1>
      <p class="gallery-description">管理您的图片收藏</p>
    </div>

    <!-- 当有图片时显示工具栏 -->
    <div v-if="images.length > 0" class="gallery-toolbar">
      <div class="toolbar-left">
        <a-button type="primary" @click="showUploader = !showUploader">
          <UploadOutlined />
          {{ showUploader ? '收起上传' : '上传更多' }}
        </a-button>
        <a-divider type="vertical" />
        <span class="toolbar-label">共 {{ images.length }} 张图片</span>
      </div>
      <div class="toolbar-right">
        <!-- 文件名搜索 -->
        <a-input-search
          v-model:value="searchKeyword"
          placeholder="搜索文件名"
          style="width: 200px; margin-right: 16px;"
          allowClear
          @search="handleSearch"
          @change="handleSearchChange"
        />
        
        <!-- 排序选择 -->
        <a-select
          v-model:value="sortOption"
          style="width: 160px; margin-right: 16px;"
          @change="handleSortChange"
        >
          <a-select-option value="createdAt-desc">上传时间 (新→旧)</a-select-option>
          <a-select-option value="createdAt-asc">上传时间 (旧→新)</a-select-option>
          <a-select-option value="fileSize-desc">文件大小 (大→小)</a-select-option>
          <a-select-option value="fileSize-asc">文件大小 (小→大)</a-select-option>
          <a-select-option value="originalFilename-asc">文件名 (A→Z)</a-select-option>
          <a-select-option value="originalFilename-desc">文件名 (Z→A)</a-select-option>
        </a-select>
        
        <!-- 标签筛选 -->
        <a-select
          v-model:value="selectedTagId"
          placeholder="按标签筛选"
          style="width: 150px; margin-right: 16px;"
          allowClear
          @change="handleTagFilterChange"
        >
          <a-select-option :value="null">全部标签</a-select-option>
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

    <!-- 上传区域 -->
    <div v-if="shouldShowUploader" class="upload-section">
      <ImageUploader 
        :max-size="10" 
        :max-count="20"
        @success="handleUploadSuccess"
        @error="handleUploadError"
      />
    </div>

    <!-- 图片展示区域 -->
    <div class="gallery-content">
      <!-- 图片列表 - 根据视图模式显示 -->
      <div v-if="images.length > 0" class="images-container">
        <!-- 网格视图 -->
        <ImageGridView
          v-if="viewMode === 'grid'"
          :images="images"
          :loading="loading"
          @preview="handleImagePreview"
          @download="handleImageDownload"
          @rename="handleImageRename"
          @manage-tags="handleManageTags"
          @delete="handleImageDelete"
        />
        
        <!-- 列表视图 -->
        <ImageListView
          v-else-if="viewMode === 'list'"
          :images="images"
          :loading="loading"
          @preview="handleImagePreview"
          @download="handleImageDownload"
          @rename="handleImageRename"
          @manage-tags="handleManageTags"
          @delete="handleImageDelete"
        />
      </div>
    </div>

    <!-- 图片预览弹窗 -->
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

    <!-- 标签管理弹窗 -->
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
import { PictureOutlined, UploadOutlined } from '@ant-design/icons-vue'
import ImageUploader from '../components/file/ImageUploader.vue'
import ImageGridView from '../components/file/ImageGridView.vue'
import ImageListView from '../components/file/ImageListView.vue'
import ViewModeToggle from '../components/file/ViewModeToggle.vue'
import FileTagManager from '../components/file/FileTagManager.vue'
import { listFiles, downloadFile, deleteFile, renameFile } from '../api/file'
import { listTags } from '../api/tag'
import { BusinessType } from '../types/file'
import type { FileInfoResponse } from '../types/file'
import type { ViewMode } from '../components/file/ViewModeToggle.vue'
import type { TagResponse } from '../types/tag'

// 状态管理
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

// 搜索和排序
const searchKeyword = ref<string>('')
const sortOption = ref<string>('createdAt-desc') // 默认按上传时间降序
let searchTimer: any = null

// 计算是否应该显示上传器
const shouldShowUploader = computed(() => {
  // 如果没有图片，总是显示上传器
  if (images.value.length === 0) return true
  // 如果有图片，则根据用户手动控制
  return showUploader.value
})

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 加载可用标签
const loadTags = async () => {
  try {
    const response = await listTags()
    if (response.data.code === 200) {
      availableTags.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载标签失败:', error)
  }
}

// 加载图片列表
const loadImages = async () => {
  try {
    loading.value = true
    
    // 解析排序选项
    const [sortBy, sortOrder] = sortOption.value.split('-')
    
    // 使用统一的 listFiles 接口
    const response = await listFiles({
      businessType: BusinessType.IMAGE,
      tagId: selectedTagId.value || undefined,  // 如果有选中的标签，传入标签ID
      filename: searchKeyword.value || undefined,  // 文件名搜索
      sortBy,  // 排序字段
      sortOrder,  // 排序方向
      page: 1,
      pageSize: 100
    })

    // API 返回的数据结构：{ code, message, data: { records, total, ... } }
    images.value = response.data.data?.records || []
  } catch (error: any) {
    console.error('加载图片失败:', error)
    message.error('加载图片失败')
  } finally {
    loading.value = false
  }
}

// 上传成功处理
const handleUploadSuccess = (newImages: any[]) => {
  // 将新上传的图片添加到列表前面
  images.value.unshift(...newImages)
  message.success(`成功上传 ${newImages.length} 张图片`)
  
  // 如果之前没有图片，现在有了图片，可以隐藏上传器
  // 如果之前就有图片，则按用户意愿控制
  if (images.value.length === newImages.length) {
    // 这是第一次上传，保持上传器显示，方便继续上传
    showUploader.value = false
  } else {
    // 已经有图片了，自动收起上传器
    showUploader.value = false
  }
}

// 上传失败处理
const handleUploadError = (error: any) => {
  console.error('上传失败:', error)
}

// 视图模式切换处理
const handleViewModeChange = (mode: ViewMode) => {
  // 保存用户偏好到本地存储
  localStorage.setItem('gallery-view-mode', mode)
}

// 搜索处理（防抖）
const handleSearchChange = () => {
  // 清除之前的定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  
  // 设置新的定时器（500ms 后执行搜索）
  searchTimer = setTimeout(() => {
    loadImages()
  }, 500)
}

// 立即搜索（按下回车或点击搜索按钮时）
const handleSearch = () => {
  // 清除防抖定时器
  if (searchTimer) {
    clearTimeout(searchTimer)
  }
  // 立即执行搜索
  loadImages()
}

// 排序变化处理
const handleSortChange = () => {
  loadImages()
}

// 图片操作处理
const handleImagePreview = (image: FileInfoResponse) => {
  // 找到当前图片在列表中的索引
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
      content: '正在下载图片...',
      key: `download-${image.fileId}`,
      duration: 0
    })

    // 调用后端下载接口
    const response = await downloadFile(image.fileId)
    
    // 从响应头获取文件名
    const contentDisposition = response.headers['content-disposition']
    let filename = image.originalFilename
    
    if (contentDisposition) {
      // 解析 Content-Disposition 头获取文件名
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

    // 创建 Blob 和下载链接
    const blob = new Blob([response.data])
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = filename
    link.style.display = 'none'
    
    // 触发下载
    document.body.appendChild(link)
    link.click()
    
    // 清理
    document.body.removeChild(link)
    URL.revokeObjectURL(url)
    
    message.success({
      content: `图片 "${filename}" 下载完成`,
      key: `download-${image.fileId}`
    })

  } catch (error) {
    console.error('下载图片失败:', error)
    message.error({
      content: `下载失败: ${error instanceof Error ? error.message : '未知错误'}`,
      key: `download-${image.fileId}`
    })
  }
}

const handleImageRename = async (image: FileInfoResponse) => {
  // 获取文件名和扩展名
  const lastDotIndex = image.originalFilename.lastIndexOf('.')
  const extension = lastDotIndex !== -1 ? image.originalFilename.substring(lastDotIndex) : ''
  const nameWithoutExt = lastDotIndex !== -1 ? image.originalFilename.substring(0, lastDotIndex) : image.originalFilename
  
  // 使用 Modal.confirm 的简化方式
  let inputValue = nameWithoutExt
  
  Modal.confirm({
    title: '重命名图片',
    content: `请输入新的文件名（扩展名: ${extension || '无'}）`,
    okText: '确认',
    cancelText: '取消',
    onOk: async () => {
      // 从DOM获取输入值
      const input = document.querySelector('.ant-modal input') as HTMLInputElement
      if (input) {
        inputValue = input.value
      }
      
      const finalName = (inputValue.trim() + extension).trim()
      
      // 验证文件名
      if (!finalName || finalName === extension) {
        message.error('文件名不能为空')
        return Promise.reject()
      }
      
      if (finalName === image.originalFilename) {
        message.info('文件名未改变')
        return
      }
      
      try {
        message.loading({
          content: '正在重命名...',
          key: `rename-${image.fileId}`,
          duration: 0
        })

        // 调用重命名接口
        const response = await renameFile(image.fileId, finalName)
        
        if (response.data.code === 200 && response.data.data) {
          // 更新列表中的图片信息
          const index = images.value.findIndex(img => img.fileId === image.fileId)
          if (index !== -1) {
            images.value[index] = response.data.data
          }
          
          message.success({
            content: `重命名成功：${finalName}`,
            key: `rename-${image.fileId}`
          })
        } else {
          throw new Error(response.data.message || '重命名失败')
        }
      } catch (error) {
        console.error('重命名图片失败:', error)
        message.error({
          content: `重命名失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: `rename-${image.fileId}`
        })
        return Promise.reject()
      }
    }
  })
  
  // 等待DOM更新后添加输入框并聚焦
  setTimeout(() => {
    const modalContent = document.querySelector('.ant-modal-confirm-body .ant-modal-confirm-content')
    if (modalContent) {
      // 清空原内容并添加自定义输入框
      modalContent.innerHTML = `
        <div>
          <p style="margin-bottom: 8px;">请输入新的文件名：</p>
          <input 
            type="text" 
            class="ant-input rename-input" 
            value="${nameWithoutExt}"
            placeholder="请输入文件名"
            style="width: 100%;"
          />
          <p style="margin-top: 8px; color: #999; font-size: 12px;">
            文件扩展名: ${extension || '无'}
          </p>
        </div>
      `
      
      const input = modalContent.querySelector('input') as HTMLInputElement
      if (input) {
        input.focus()
        input.select()
        
        // 支持回车键确认
        input.addEventListener('keyup', (e) => {
          if (e.key === 'Enter') {
            const okButton = document.querySelector('.ant-modal-confirm-btns .ant-btn-primary') as HTMLButtonElement
            okButton?.click()
          }
        })
      }
    }
  }, 100)
}

// 标签筛选变化
const handleTagFilterChange = () => {
  loadImages()
}

// 管理标签
const handleManageTags = (image: FileInfoResponse) => {
  currentImageForTag.value = image
  tagManagerVisible.value = true
}

// 标签更新后回调
const handleTagsUpdated = (tags: TagResponse[]) => {
  // 更新当前图片的标签
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
  
  // 重新加载标签列表
  loadTags()
}

const handleImageDelete = async (image: FileInfoResponse) => {
  // 使用 Modal 确认删除
  Modal.confirm({
    title: '确认删除',
    content: `确定要删除图片 "${image.originalFilename}" 吗？此操作可以恢复。`,
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

        // 调用删除接口
        const response = await deleteFile(image.fileId)
        
        if (response.data.code === 200 && response.data.data) {
          // 从列表中移除该图片
          images.value = images.value.filter(img => img.fileId !== image.fileId)
          
          message.success({
            content: `图片 "${image.originalFilename}" 删除成功`,
            key: `delete-${image.fileId}`
          })
        } else {
          throw new Error(response.data.message || '删除失败')
        }
      } catch (error) {
        console.error('删除图片失败:', error)
        message.error({
          content: `删除失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: `delete-${image.fileId}`
        })
      }
    }
  })
}

// 初始化用户偏好设置
const initUserPreferences = () => {
  const savedViewMode = localStorage.getItem('gallery-view-mode') as ViewMode
  if (savedViewMode && ['grid', 'list'].includes(savedViewMode)) {
    viewMode.value = savedViewMode
  }
}

// 页面加载时获取图片列表和用户偏好
onMounted(() => {
  initUserPreferences()
  loadImages()
  loadTags()
})
</script>

<style scoped>
.image-gallery {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.gallery-header {
  text-align: center;
  margin-bottom: 48px;
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
  padding: 0 8px;
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
  background: #fafafa;
  border-radius: 12px;
  border: 1px solid #e5e7eb;
}

.images-container {
  width: 100%;
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
