<!--
  Avatar Upload Component
  Supports three upload methods: file selection, URL input, paste image
  Interaction flow: preview first, upload after confirmation
-->
<template>
  <div class="avatar-upload">
    <!-- Avatar preview area -->
    <div class="avatar-preview-wrapper">
      <!-- Current avatar display (confirmed avatar) -->
      <div class="avatar-display" @click="handleClickAvatar">
        <a-image
          v-if="confirmedImageUrl"
          :src="confirmedImageUrl"
          alt="avatar"
          class="avatar-image"
          :preview="{
            mask: '点击查看大图',
          }"
        />
        <div v-else class="upload-placeholder">
          <PlusOutlined />
          <div class="upload-text">暂无头像</div>
        </div>
      </div>

      <!-- Action buttons -->
      <a-space>
        <a-button type="primary" size="small" @click="showUploadModal">
          <EditOutlined /> {{ confirmedImageUrl ? '更换头像' : '上传头像' }}
        </a-button>
        <a-button v-if="confirmedImageUrl" size="small" danger @click="handleClearAvatar">
          <DeleteOutlined /> 清除头像
        </a-button>
      </a-space>
    </div>

    <!-- Upload modal -->
    <a-modal
      v-model:open="uploadModalVisible"
      title="上传头像"
      :width="600"
      :confirm-loading="uploading"
      @ok="handleConfirmUpload"
      @cancel="handleCancelUpload"
    >
      <div class="upload-modal-content">
        <!-- Preview area -->
        <div class="preview-section">
          <div class="preview-label">预览</div>
          <div class="preview-wrapper">
            <a-image
              v-if="previewImageUrl"
              :src="previewImageUrl"
              alt="preview"
              class="preview-image"
              :preview="{
                mask: '查看大图',
              }"
            />
            <div v-else class="preview-placeholder">
              <PictureOutlined style="font-size: 48px; color: #ccc" />
              <p>请选择图片</p>
            </div>
          </div>
        </div>

        <!-- Upload method switch -->
        <a-tabs v-model:activeKey="uploadTab" size="small">
          <a-tab-pane key="upload" tab="上传新头像">
            <!-- Use common file uploader component -->
            <CommonFileUploader
              ref="commonUploaderRef"
              :max-size="maxSize"
              :multiple="false"
              :enable-drag-drop="true"
              :enable-paste="true"
              :enable-url-input="true"
              mode="compact"
              accept="image/*"
              upload-text="选择头像图片"
              @file-select="handleFileSelect"
            />

            <!-- Upload tips -->
            <div class="upload-tips">
              <InfoCircleOutlined style="margin-right: 4px; color: #1890ff" />
              支持 JPG、PNG、WEBP 格式 · 大小不超过 {{ maxSizeMB }}MB · 建议尺寸 400x400 像素
            </div>
          </a-tab-pane>

          <a-tab-pane key="history" tab="历史头像">
            <div class="history-avatars-wrapper">
              <a-spin :spinning="loadingHistory">
                <div v-if="historyAvatars.length > 0" class="history-sections">
                  <div
                    v-for="group in groupedHistoryAvatars"
                    :key="group.date"
                    class="history-group"
                  >
                    <div class="history-group-title">{{ group.label }}</div>
                    <div class="history-grid">
                      <div
                        v-for="avatar in group.avatars"
                        :key="avatar.fileId"
                        class="history-avatar-item"
                        :class="{ 'history-avatar-selected': previewImageUrl === avatar.fileUrl }"
                        @click="selectHistoryAvatar(avatar)"
                      >
                        <img
                          :src="avatar.thumbnailUrl || avatar.fileUrl"
                          :alt="avatar.originalFilename"
                          class="history-avatar-img"
                        />
                        <div class="history-avatar-overlay">
                          <CheckCircleOutlined v-if="previewImageUrl === avatar.fileUrl" />
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <a-empty v-else description="暂无历史头像" />
              </a-spin>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>

      <template #footer>
        <a-space>
          <a-button @click="handleCancelUpload">取消</a-button>
          <a-button
            type="primary"
            :loading="uploading"
            :disabled="!previewImageUrl"
            @click="handleConfirmUpload"
          >
            确认上传
          </a-button>
        </a-space>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PictureOutlined,
  InfoCircleOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import CommonFileUploader from '../common/CommonFileUploader.vue'
import { uploadFile, listFiles } from '../../api/file'
import { BusinessType } from '../../types/file'
import type { FileInfoResponse } from '../../types/file'

interface Props {
  /** Current avatar URL */
  modelValue?: string
  /** Maximum file size (MB), default 5MB */
  maxSize?: number
}

interface Emits {
  (e: 'update:modelValue', value: string): void
  (e: 'upload-success', url: string, fileId: number): void
  (e: 'upload-error', error: Error): void
}

const props = withDefaults(defineProps<Props>(), {
  modelValue: '',
  maxSize: 5,
})

const emit = defineEmits<Emits>()

// State
const uploadModalVisible = ref(false)
const uploading = ref(false)
const uploadTab = ref<'upload' | 'history'>('upload')
const commonUploaderRef = ref<InstanceType<typeof CommonFileUploader>>()

// Preview related
const previewImageUrl = ref('') // Preview image URL in modal (local base64 or network URL)
const previewFile = ref<File | null>(null) // File object to upload
const previewSource = ref<'file' | 'url' | 'history'>('file') // Preview source
const selectedHistoryFileId = ref<number>(0) // Selected history avatar ID

// History avatar related
const historyAvatars = ref<FileInfoResponse[]>([])
const loadingHistory = ref(false)

// Confirmed avatar URL (passed from parent component)
const confirmedImageUrl = computed(() => props.modelValue)

// Computed properties
const maxSizeMB = computed(() => props.maxSize)
const maxSizeBytes = computed(() => props.maxSize * 1024 * 1024)

// History avatars grouped by date
const groupedHistoryAvatars = computed(() => {
  const groups: Array<{ date: string; label: string; avatars: FileInfoResponse[] }> = []
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)

  // Group by date
  const todayAvatars: FileInfoResponse[] = []
  const yesterdayAvatars: FileInfoResponse[] = []
  const thisWeekAvatars: FileInfoResponse[] = []
  const thisMonthAvatars: FileInfoResponse[] = []
  const olderAvatars: FileInfoResponse[] = []

  historyAvatars.value.forEach((avatar) => {
    const createdDate = new Date(avatar.createdAt)

    if (createdDate >= today) {
      todayAvatars.push(avatar)
    } else if (createdDate >= yesterday) {
      yesterdayAvatars.push(avatar)
    } else if (createdDate >= weekAgo) {
      thisWeekAvatars.push(avatar)
    } else if (createdDate >= monthAgo) {
      thisMonthAvatars.push(avatar)
    } else {
      olderAvatars.push(avatar)
    }
  })

  // Build group data
  if (todayAvatars.length > 0) {
    groups.push({ date: 'today', label: '今天', avatars: todayAvatars })
  }
  if (yesterdayAvatars.length > 0) {
    groups.push({ date: 'yesterday', label: '昨天', avatars: yesterdayAvatars })
  }
  if (thisWeekAvatars.length > 0) {
    groups.push({ date: 'thisWeek', label: '本周', avatars: thisWeekAvatars })
  }
  if (thisMonthAvatars.length > 0) {
    groups.push({ date: 'thisMonth', label: '本月', avatars: thisMonthAvatars })
  }
  if (olderAvatars.length > 0) {
    groups.push({ date: 'older', label: '更早', avatars: olderAvatars })
  }

  return groups
})


// Watch upload tab switch, load history avatars
watch(uploadTab, (newTab) => {
  if (newTab === 'history' && historyAvatars.value.length === 0) {
    loadHistoryAvatars()
  }
})

/**
 * Read file as Data URL
 */
async function readFileAsDataURL(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target?.result as string)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

/**
 * Handle files selected by common uploader component
 */
async function handleFileSelect(files: File[]) {
  const file = files[0] // Avatar upload only processes the first file
  if (!file) return

  try {
    // Create local preview
    const dataUrl = await readFileAsDataURL(file)
    
    previewImageUrl.value = dataUrl
    previewFile.value = file
    previewSource.value = 'file'
    message.success('图片已加载，请确认后上传')
  } catch (error) {
    console.error('Failed to read file:', error)
    message.error('读取文件失败')
  }
}

/**
 * Load history avatar list
 */
async function loadHistoryAvatars() {
  loadingHistory.value = true
  try {
    const { data } = await listFiles({
      businessType: BusinessType.AVATAR,
      page: 1,
      pageSize: 20,
    })

    if (data.code === 200 && data.data) {
      historyAvatars.value = data.data.records
    } else {
      throw new Error(data.message || '获取历史头像失败')
    }
  } catch (error) {
    console.error('Failed to load history avatars:', error)
    message.error('加载历史头像失败')
  } finally {
    loadingHistory.value = false
  }
}

/**
 * Select history avatar
 */
function selectHistoryAvatar(avatar: FileInfoResponse) {
  previewImageUrl.value = avatar.fileUrl
  previewFile.value = null
  previewSource.value = 'history'
  selectedHistoryFileId.value = avatar.fileId
  message.success('已选择此头像，请确认后应用')
}

/**
 * Show upload modal
 */
function showUploadModal() {
  uploadModalVisible.value = true
  // Reset state
  previewImageUrl.value = ''
  previewFile.value = null
  uploadTab.value = 'upload'
  
  // Auto focus upload area after DOM update, allowing direct paste
  nextTick(() => {
    setTimeout(() => {
      commonUploaderRef.value?.focus()
    }, 300) // Increase delay to ensure Modal is fully rendered
  })
}

/**
 * Click avatar area (only for viewing large image, does not trigger upload)
 */
function handleClickAvatar(event: MouseEvent) {
  // Do nothing, let a-image preview feature work
  event.stopPropagation()
}


/**
 * Confirm upload
 */
async function handleConfirmUpload() {
  if (!previewImageUrl.value) {
    message.warning('请先选择图片')
    return
  }

  uploading.value = true

  try {
    if (previewSource.value === 'url') {
      // URL source, use URL directly
      emit('update:modelValue', previewImageUrl.value)
      emit('upload-success', previewImageUrl.value, 0) // fileId 0 means external URL
      message.success('头像设置成功！')
      uploadModalVisible.value = false
    } else if (previewSource.value === 'history') {
      // History avatar, use directly
      emit('update:modelValue', previewImageUrl.value)
      emit('upload-success', previewImageUrl.value, selectedHistoryFileId.value)
      message.success('头像已更换！')
      uploadModalVisible.value = false
    } else if (previewFile.value) {
      // File source, upload to server
      const { data } = await uploadFile(previewFile.value, BusinessType.AVATAR)

      if (data.code === 200 && data.data) {
        const response = data.data
        let avatarUrl = response.fileUrl

        // If thumbnailUrl is returned, prefer using thumbnail
        if (response.thumbnailUrl) {
          avatarUrl = response.thumbnailUrl
        }

        emit('update:modelValue', avatarUrl)
        emit('upload-success', avatarUrl, response.fileId)
        message.success('头像上传成功！')
        uploadModalVisible.value = false

        // Refresh history avatar list
        if (historyAvatars.value.length > 0) {
          loadHistoryAvatars()
        }
      } else {
        throw new Error(data.message || '上传失败')
      }
    }
  } catch (error) {
    console.error('Upload failed:', error)
    const err = error as Error
    message.error(err.message || '上传失败，请重试')
    emit('upload-error', err)
  } finally {
    uploading.value = false
  }
}

/**
 * Cancel upload
 */
function handleCancelUpload() {
  uploadModalVisible.value = false
  previewImageUrl.value = ''
  previewFile.value = null
}


/**
 * Clear avatar
 */
function handleClearAvatar() {
  emit('update:modelValue', '')
  message.success('头像已清除')
}
</script>

<style scoped>
.avatar-upload {
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  max-width: 480px;
}

/* Avatar preview area */
.avatar-preview-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

/* Avatar display area */
.avatar-display {
  width: 128px;
  height: 128px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #d9d9d9;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.3s;
}

.avatar-display:hover {
  border-color: #1890ff;
}

/* a-image component styles */
:deep(.avatar-image) {
  width: 128px !important;
  height: 128px !important;
}

:deep(.avatar-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

:deep(.avatar-image .ant-image-mask) {
  border-radius: 50%;
}

.upload-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #999;
}

.upload-placeholder .anticon {
  font-size: 32px;
}

.upload-text {
  font-size: 14px;
  color: #666;
}

/* Upload modal */
.upload-modal-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* Preview area */
.preview-section {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.preview-label {
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.preview-wrapper {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  border: 2px solid #d9d9d9;
  background: #fafafa;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto;
}

:deep(.preview-image) {
  width: 200px !important;
  height: 200px !important;
}

:deep(.preview-image img) {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #ccc;
}

.preview-placeholder p {
  margin: 0;
  font-size: 14px;
}


/* Upload tips */
.upload-tips {
  padding: 12px;
  background: #f0f7ff;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  text-align: center;
  border: 1px solid #d6e4ff;
}

/* History avatar area */
.history-avatars-wrapper {
  min-height: 300px;
  padding: 16px 0;
}

.history-sections {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.history-group {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.history-group-title {
  font-size: 14px;
  font-weight: 500;
  color: #333;
  padding-bottom: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.history-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
}

.history-avatar-item {
  position: relative;
  width: 100%;
  padding-bottom: 100%; /* 1:1 square */
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid #d9d9d9;
  transition: all 0.3s;
}

.history-avatar-item:hover {
  border-color: #1890ff;
  transform: scale(1.05);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.history-avatar-selected {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.history-avatar-img {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.history-avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(24, 144, 255, 0.1);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.history-avatar-selected .history-avatar-overlay {
  opacity: 1;
}

.history-avatar-overlay .anticon {
  font-size: 32px;
  color: #1890ff;
}
</style>
