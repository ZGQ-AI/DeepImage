<!--
  头像上传组件
  支持三种上传方式：文件选择、URL 输入、粘贴图片
  交互流程：先预览，确认后才上传
-->
<template>
  <div class="avatar-upload">
    <!-- 头像预览区域 -->
    <div class="avatar-preview-wrapper">
      <!-- 当前头像显示（已确认的头像） -->
      <div class="avatar-display" @click="handleClickAvatar">
        <a-image 
          v-if="confirmedImageUrl" 
          :src="confirmedImageUrl" 
          alt="avatar" 
          class="avatar-image"
          :preview="{
            mask: '点击查看大图'
          }"
        />
        <div v-else class="upload-placeholder">
          <PlusOutlined />
          <div class="upload-text">暂无头像</div>
        </div>
      </div>

      <!-- 操作按钮 -->
      <a-space>
        <a-button type="primary" size="small" @click="showUploadModal">
          <EditOutlined /> {{ confirmedImageUrl ? '更换头像' : '上传头像' }}
        </a-button>
        <a-button v-if="confirmedImageUrl" size="small" danger @click="handleClearAvatar">
          <DeleteOutlined /> 清除头像
        </a-button>
      </a-space>
    </div>

    <!-- 上传模态框 -->
    <a-modal
      v-model:open="uploadModalVisible"
      title="上传头像"
      :width="600"
      :confirm-loading="uploading"
      @ok="handleConfirmUpload"
      @cancel="handleCancelUpload"
    >
      <div class="upload-modal-content">
        <!-- 预览区域 -->
        <div class="preview-section">
          <div class="preview-label">预览</div>
          <div class="preview-wrapper">
            <a-image 
              v-if="previewImageUrl" 
              :src="previewImageUrl" 
              alt="preview" 
              class="preview-image"
              :preview="{
                mask: '查看大图'
              }"
            />
            <div v-else class="preview-placeholder">
              <PictureOutlined style="font-size: 48px; color: #ccc;" />
              <p>请选择图片</p>
            </div>
          </div>
        </div>

        <!-- 上传方式切换 -->
        <a-tabs v-model:activeKey="uploadTab" size="small">
          <a-tab-pane key="upload" tab="上传新头像">
            <!-- 统一的上传区域 -->
            <div class="unified-upload-area">
              <!-- 主要上传区域：智能识别粘贴内容 -->
              <div 
                ref="uploadZoneRef"
                class="upload-drop-zone"
                :class="{ 'drop-zone-active': isPasteAreaActive }"
                @paste="handleSmartPaste"
                @dragover.prevent="isPasteAreaActive = true"
                @dragleave="isPasteAreaActive = false"
                @drop.prevent="handleDrop"
                tabindex="0"
              >
                <div class="drop-zone-content">
                  <UploadOutlined style="font-size: 36px; color: #1890ff;" />
                  <div class="drop-zone-text">
                    <p class="main-text">拖拽图片到此处，或按 Ctrl+V (Cmd+V) 粘贴</p>
                    <p class="sub-text">支持粘贴图片文件或图片链接</p>
                    <p class="sub-text">
                      <a-button type="link" size="small" @click="handleClickUploadZone">
                        点击选择文件
                      </a-button>
                    </p>
                  </div>
                </div>
              </div>

              <!-- 隐藏的文件输入 -->
              <input
                ref="fileInputRef"
                type="file"
                accept="image/png,image/jpeg,image/jpg,image/webp"
                style="display: none"
                @change="handleFileChange"
              />
            </div>

            <!-- 上传提示 -->
            <div class="upload-tips">
              <InfoCircleOutlined style="margin-right: 4px; color: #1890ff;" />
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
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  PictureOutlined,
  UploadOutlined,
  InfoCircleOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons-vue'
import { uploadFile, listFilesByType } from '../../api/file'
import { BusinessType } from '../../types/file'
import type { FileInfoResponse } from '../../types/file'

interface Props {
  /** 当前头像 URL */
  modelValue?: string
  /** 最大文件大小（MB），默认 5MB */
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

// 状态
const uploadModalVisible = ref(false)
const uploading = ref(false)
const isPasteAreaActive = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)
const uploadZoneRef = ref<HTMLElement | null>(null)
const uploadTab = ref<'upload' | 'history'>('upload')

// 预览相关
const previewImageUrl = ref('') // 模态框中预览的图片 URL（本地 base64 或网络 URL）
const previewFile = ref<File | null>(null) // 待上传的文件对象
const previewSource = ref<'file' | 'url' | 'history'>('file') // 预览来源
const selectedHistoryFileId = ref<number>(0) // 选中的历史头像 ID

// 历史头像相关
const historyAvatars = ref<FileInfoResponse[]>([])
const loadingHistory = ref(false)

// 已确认的头像 URL（父组件传入的）
const confirmedImageUrl = computed(() => props.modelValue)

// 计算属性
const maxSizeMB = computed(() => props.maxSize)
const maxSizeBytes = computed(() => props.maxSize * 1024 * 1024)

// 按日期分组的历史头像
const groupedHistoryAvatars = computed(() => {
  const groups: Array<{ date: string; label: string; avatars: FileInfoResponse[] }> = []
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 24 * 60 * 60 * 1000)
  const weekAgo = new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000)
  const monthAgo = new Date(today.getTime() - 30 * 24 * 60 * 60 * 1000)

  // 按日期分组
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

  // 构建分组数据
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

// 常量定义
const CONSTANTS = {
  AUTO_FOCUS_DELAY: 100,     // DOM 更新后自动聚焦的延迟时间
  IMAGE_LOAD_TIMEOUT: 10000,  // 图片加载超时时间（10秒）
}

// 监听上传标签页切换，加载历史头像
watch(uploadTab, (newTab) => {
  if (newTab === 'history' && historyAvatars.value.length === 0) {
    loadHistoryAvatars()
  }
})

/**
 * 将文件读取为 Data URL
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
 * 验证文件
 */
function validateFile(file: File): boolean {
  // 验证文件类型
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }

  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    message.error('只支持 JPG、PNG、WEBP 格式的图片！')
    return false
  }

  // 验证文件大小
  const isLtMaxSize = file.size <= maxSizeBytes.value
  if (!isLtMaxSize) {
    message.error(`图片大小不能超过 ${maxSizeMB.value}MB！`)
    return false
  }

  return true
}

/**
 * 加载历史头像列表
 */
async function loadHistoryAvatars() {
  loadingHistory.value = true
  try {
    const { data } = await listFilesByType({
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
    console.error('加载历史头像失败:', error)
    message.error('加载历史头像失败')
  } finally {
    loadingHistory.value = false
  }
}

/**
 * 选择历史头像
 */
function selectHistoryAvatar(avatar: FileInfoResponse) {
  previewImageUrl.value = avatar.fileUrl
  previewFile.value = null
  previewSource.value = 'history'
  selectedHistoryFileId.value = avatar.fileId
  message.success('已选择此头像，请确认后应用')
}

/**
 * 显示上传模态框
 */
function showUploadModal() {
  uploadModalVisible.value = true
  // 重置状态
  previewImageUrl.value = ''
  previewFile.value = null
  uploadTab.value = 'upload'
  
  // 等待 DOM 更新后自动聚焦到上传区域，使其可以直接粘贴
  setTimeout(() => {
    uploadZoneRef.value?.focus()
  }, CONSTANTS.AUTO_FOCUS_DELAY)
}

/**
 * 点击头像区域（仅用于查看大图，不触发上传）
 */
function handleClickAvatar(event: MouseEvent) {
  // 不做任何操作，让 a-image 的预览功能生效
  event.stopPropagation()
}

/**
 * 点击"选择文件"按钮
 */
function handleClickUploadZone(event: MouseEvent) {
  event.stopPropagation() // 阻止事件冒泡，避免影响其他交互
  fileInputRef.value?.click()
}

/**
 * 处理文件选择
 */
async function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  
  if (!file) return

  if (!validateFile(file)) {
    // 清空 input
    target.value = ''
    return
  }

  try {
    // 创建本地预览
    const dataUrl = await readFileAsDataURL(file)

    previewImageUrl.value = dataUrl
    previewFile.value = file
    previewSource.value = 'file'
    message.success('图片已加载，请确认后上传')
  } catch (error) {
    console.error('读取文件失败:', error)
    message.error('读取文件失败')
  } finally {
    // 清空 input，允许重复选择同一文件
    target.value = ''
  }
}

/**
 * 确认上传
 */
async function handleConfirmUpload() {
  if (!previewImageUrl.value) {
    message.warning('请先选择图片')
    return
  }

  uploading.value = true

  try {
    if (previewSource.value === 'url') {
      // URL 来源，直接使用 URL
      emit('update:modelValue', previewImageUrl.value)
      emit('upload-success', previewImageUrl.value, 0) // fileId 为 0 表示外部 URL
      message.success('头像设置成功！')
      uploadModalVisible.value = false
    } else if (previewSource.value === 'history') {
      // 历史头像，直接使用
      emit('update:modelValue', previewImageUrl.value)
      emit('upload-success', previewImageUrl.value, selectedHistoryFileId.value)
      message.success('头像已更换！')
      uploadModalVisible.value = false
    } else if (previewFile.value) {
      // 文件来源，上传到服务器
      const { data } = await uploadFile(previewFile.value, BusinessType.AVATAR)

      if (data.code === 200 && data.data) {
        const response = data.data
        let avatarUrl = response.fileUrl

        // 如果返回了 thumbnailUrl，可以优先使用缩略图
        if (response.thumbnailUrl) {
          avatarUrl = response.thumbnailUrl
        }

        emit('update:modelValue', avatarUrl)
        emit('upload-success', avatarUrl, response.fileId)
        message.success('头像上传成功！')
        uploadModalVisible.value = false
        
        // 刷新历史头像列表
        if (historyAvatars.value.length > 0) {
          loadHistoryAvatars()
        }
      } else {
        throw new Error(data.message || '上传失败')
      }
    }
  } catch (error) {
    console.error('上传失败:', error)
    const err = error as Error
    message.error(err.message || '上传失败，请重试')
    emit('upload-error', err)
  } finally {
    uploading.value = false
  }
}

/**
 * 取消上传
 */
function handleCancelUpload() {
  uploadModalVisible.value = false
  previewImageUrl.value = ''
  previewFile.value = null
}

/**
 * 智能粘贴处理：自动识别图片文件或图片URL
 */
async function handleSmartPaste(event: ClipboardEvent) {
  const items = event.clipboardData?.items
  const text = event.clipboardData?.getData('text')

  // 优先级1: 检查是否有图片文件
  if (items) {
    for (let i = 0; i < items.length; i++) {
      const item = items[i]
      
      if (item.type.startsWith('image/')) {
        event.preventDefault()
        const file = item.getAsFile()
        if (!file) {
          message.error('无法获取图片文件')
          return
        }

        if (!validateFile(file)) {
          return
        }

        try {
          // 创建本地预览
          const dataUrl = await readFileAsDataURL(file)

          previewImageUrl.value = dataUrl
          previewFile.value = file
          previewSource.value = 'file'
          message.success('图片已加载，请确认后上传')
        } catch (error) {
          console.error('处理粘贴图片失败:', error)
          message.error('处理粘贴图片失败')
        }
        return
      }
    }
  }

  // 优先级2: 检查是否是URL链接
  if (text && text.trim()) {
    const url = text.trim()
    
    // 简单验证是否是URL
    if (url.startsWith('http://') || url.startsWith('https://')) {
      event.preventDefault()
      
      try {
        // 验证 URL 格式
        new URL(url)
        
        // 验证图片 URL 是否可访问
        const img = new Image()
        img.crossOrigin = 'anonymous'

        await new Promise((resolve, reject) => {
          img.onload = resolve
          img.onerror = reject
          img.src = url
          setTimeout(() => reject(new Error('加载超时')), CONSTANTS.IMAGE_LOAD_TIMEOUT)
        })

        previewImageUrl.value = url
        previewFile.value = null
        previewSource.value = 'url'
        message.success('图片链接已加载，请确认后上传')
      } catch (error) {
        console.error('URL 验证失败:', error)
        message.error('无法加载该图片链接，请检查 URL 是否正确')
      }
      return
    }
  }

  // 如果都不是，提示用户
  message.warning('请粘贴图片文件或图片链接')
}

/**
 * 处理拖拽上传（仅预览，不上传）
 */
async function handleDrop(event: DragEvent) {
  isPasteAreaActive.value = false

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) {
    return
  }

  const file = files[0]
  if (!validateFile(file)) {
    return
  }

  try {
    // 创建本地预览
    const dataUrl = await readFileAsDataURL(file)

    previewImageUrl.value = dataUrl
    previewFile.value = file
    previewSource.value = 'file'
    message.success('图片已加载，请确认后上传')
  } catch (error) {
    console.error('处理拖拽图片失败:', error)
    message.error('处理拖拽图片失败')
  }
}

/**
 * 清除头像
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

/* 头像预览区域 */
.avatar-preview-wrapper {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

/* 头像显示区域 */
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

/* a-image 组件样式 */
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

/* 上传模态框 */
.upload-modal-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 预览区域 */
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

/* 统一上传区域 */
.unified-upload-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 上传拖拽区域 */
.upload-drop-zone {
  min-height: 140px;
  padding: 32px 24px;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  background: #fafafa;
  outline: none;
}

.upload-drop-zone:hover,
.upload-drop-zone:focus {
  border-color: #1890ff;
  background: #f0f5ff;
}

.drop-zone-active {
  border-color: #1890ff;
  background: #e6f7ff;
  transform: scale(1.01);
}

.drop-zone-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}

.drop-zone-text {
  text-align: center;
}

.drop-zone-text .main-text {
  margin: 0 0 4px 0;
  font-size: 15px;
  color: #333;
  font-weight: 500;
}

.drop-zone-text .sub-text {
  margin: 0;
  font-size: 13px;
  color: #999;
}

/* 上传提示 */
.upload-tips {
  padding: 12px;
  background: #f0f7ff;
  border-radius: 6px;
  font-size: 13px;
  color: #666;
  text-align: center;
  border: 1px solid #d6e4ff;
}

/* 历史头像区域 */
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
  padding-bottom: 100%; /* 1:1 正方形 */
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

