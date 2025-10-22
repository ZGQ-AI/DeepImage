<template>
  <div class="image-uploader">
    <!-- 增强的拖拽上传区域 - 支持粘贴 -->
    <div
      ref="uploadZoneRef"
      class="enhanced-upload-zone"
      :class="{ 'drop-zone-active': isDragActive, 'paste-zone-focused': isPasteFocused }"
      @click="handleClickUploadZone"
      @paste="handleSmartPaste"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave" 
      @drop.prevent="handleEnhancedDrop"
      @focus="isPasteFocused = true"
      @blur="isPasteFocused = false"
      tabindex="0"
    >
      <div class="upload-content">
        <div class="upload-icon">
          <CloudUploadOutlined :style="{ fontSize: '48px', color: '#1890ff' }" />
        </div>
        <p class="upload-text">点击、拖拽或粘贴图片到此区域上传</p>
        <p class="upload-hint">
          支持 <kbd>Ctrl+V</kbd> (Mac: <kbd>⌘+V</kbd>) 粘贴图片文件或图片链接
        </p>
        <p class="upload-format">
          支持 JPG、PNG、GIF、WebP 格式，最大 {{ maxSize }}MB
        </p>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      accept="image/*"
      multiple
      style="display: none"
      @change="handleFileChange"
    />

    <!-- 传统的文件列表显示 -->
    <div v-if="fileList.length > 0" class="file-list">
      <div
        v-for="file in fileList"
        :key="file.uid"
        class="file-item"
        :class="{ 
          'file-uploading': file.status === 'uploading',
          'file-done': file.status === 'done',
          'file-error': file.status === 'error'
        }"
      >
        <div class="file-info">
          <img 
            v-if="file.thumbUrl || file.url" 
            :src="file.thumbUrl || file.url" 
            :alt="file.name"
            class="file-thumb"
          />
          <div class="file-details">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-size">{{ formatFileSize(file.size || 0) }}</div>
          </div>
        </div>
        <div class="file-status">
          <LoadingOutlined v-if="file.status === 'uploading'" />
          <CheckCircleOutlined v-else-if="file.status === 'done'" style="color: #52c41a" />
          <CloseCircleOutlined v-else-if="file.status === 'error'" style="color: #ff4d4f" />
        </div>
        <a-button 
          type="text" 
          size="small" 
          @click="removeFile(file)"
          :disabled="file.status === 'uploading'"
        >
          <DeleteOutlined />
        </a-button>
      </div>
    </div>

    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <a-progress
        :percent="uploadProgress"
        :status="uploadProgress === 100 ? 'success' : 'active'"
        :show-info="true"
      />
      <p class="progress-text">
        正在上传 {{ currentFile }}...
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { 
  CloudUploadOutlined, 
  LoadingOutlined, 
  CheckCircleOutlined, 
  CloseCircleOutlined,
  DeleteOutlined 
} from '@ant-design/icons-vue'
import type { UploadFile } from 'ant-design-vue'
import { uploadFile } from '../../api/file'
import { BusinessType } from '../../types/file'

// Props
interface Props {
  maxSize?: number // 最大文件大小，单位MB
  maxCount?: number // 最大文件数量
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 10,
  maxCount: 20
})

// Emits
const emit = defineEmits<{
  success: [files: any[]]
  error: [error: any]
}>()

// State
const fileList = ref<UploadFile[]>([])
const uploading = ref(false)
const uploadProgress = ref(0)
const currentFile = ref('')
const isDragActive = ref(false)
const isPasteFocused = ref(false)
const uploadZoneRef = ref<HTMLElement>()
const fileInputRef = ref<HTMLInputElement>()

// Computed
const uploadUrl = computed(() => {
  // 这里返回上传接口地址，但我们使用custom-request
  return '/api/files/upload'
})




// 清空文件列表
const clearFiles = () => {
  fileList.value = []
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 生成唯一ID
const generateUID = (): string => {
  return Date.now() + '-' + Math.random().toString(36).substr(2, 9)
}

// 将文件读取为 Data URL
const readFileAsDataURL = (file: File): Promise<string> => {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = (e) => resolve(e.target?.result as string)
    reader.onerror = reject
    reader.readAsDataURL(file)
  })
}

// 验证文件
const validateFile = (file: File): boolean => {
  const isValidType = file.type.startsWith('image/')
  if (!isValidType) {
    message.error('只能上传图片文件！')
    return false
  }

  const allowedTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif', 'image/webp']
  if (!allowedTypes.includes(file.type)) {
    message.error('只支持 JPG、PNG、GIF、WebP 格式的图片！')
    return false
  }

  const isValidSize = file.size / 1024 / 1024 < props.maxSize
  if (!isValidSize) {
    message.error(`图片大小不能超过 ${props.maxSize}MB！`)
    return false
  }

  if (fileList.value.length >= props.maxCount) {
    message.error(`最多只能上传 ${props.maxCount} 张图片！`)
    return false
  }

  return true
}

// 处理点击上传区域
const handleClickUploadZone = () => {
  fileInputRef.value?.click()
}

// 处理文件选择
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files

  if (!files || files.length === 0) return

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (validateFile(file)) {
      await addFileToList(file)
    }
  }

  // 清空 input
  target.value = ''
}

// 添加文件到列表并开始上传
const addFileToList = async (file: File) => {
  const uid = generateUID()
  
  // 创建缩略图
  let thumbUrl = ''
  try {
    thumbUrl = await readFileAsDataURL(file)
  } catch (error) {
    console.error('创建缩略图失败:', error)
  }

  const uploadFile: UploadFile = {
    uid,
    name: file.name,
    size: file.size,
    type: file.type,
    status: 'uploading',
    originFileObj: file as any,
    thumbUrl
  }

  fileList.value.push(uploadFile)

  // 开始上传
  handleUploadSingle(uploadFile)
}

// 单个文件上传
const handleUploadSingle = async (fileItem: UploadFile) => {
  const file = fileItem.originFileObj as File
  
  try {
    uploading.value = true
    currentFile.value = file.name

    // 调用上传API
    const response = await uploadFile(file, BusinessType.IMAGE)
    
    // 后端返回格式：{ code: number, message: string, data: T }
    if (response.data.code === 200 && response.data.data) {
      message.success(`${file.name} 上传成功`)
      
      // 更新文件状态
      fileItem.status = 'done'
      fileItem.response = response.data.data
      fileItem.url = response.data.data.fileUrl

      emit('success', [response.data.data])
    } else {
      throw new Error(response.data.message || '上传失败')
    }
  } catch (error: any) {
    message.error(`${file.name} 上传失败: ${error.message}`)
    fileItem.status = 'error'
    emit('error', error)
  } finally {
    uploading.value = false
    currentFile.value = ''
  }
}

// 智能粘贴处理
const handleSmartPaste = async (event: ClipboardEvent) => {
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

        if (validateFile(file)) {
          await addFileToList(file)
          message.success('已粘贴图片，开始上传')
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

        // 下载图片并转换为文件
        const response = await fetch(url)
        if (!response.ok) {
          throw new Error('无法下载图片')
        }

        const blob = await response.blob()
        if (!blob.type.startsWith('image/')) {
          throw new Error('URL 不是有效的图片')
        }

        // 从URL提取文件名
        const urlPath = new URL(url).pathname
        const filename = urlPath.split('/').pop() || 'image.jpg'
        
        const file = new File([blob], filename, { type: blob.type })

        if (validateFile(file)) {
          await addFileToList(file)
          message.success('已粘贴图片链接，开始上传')
        }
      } catch (error) {
        console.error('URL 处理失败:', error)
        message.error('无法加载该图片链接，请检查 URL 是否正确')
      }
      return
    }
  }

  // 如果都不是，提示用户
  message.warning('请粘贴图片文件或图片链接')
}

// 处理拖拽悬停
const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  isDragActive.value = true
}

// 处理拖拽离开
const handleDragLeave = () => {
  isDragActive.value = false
}

// 处理增强的拖拽放置
const handleEnhancedDrop = async (event: DragEvent) => {
  isDragActive.value = false

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  for (let i = 0; i < files.length; i++) {
    const file = files[i]
    if (validateFile(file)) {
      await addFileToList(file)
    }
  }
}

// 移除文件
const removeFile = (fileItem: UploadFile) => {
  const index = fileList.value.findIndex(f => f.uid === fileItem.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
  }
}

// 自动聚焦到上传区域
const focusUploadZone = () => {
  setTimeout(() => {
    uploadZoneRef.value?.focus()
  }, 100)
}

// 组件挂载时自动聚焦
onMounted(() => {
  focusUploadZone()
})

// 暴露方法给父组件
defineExpose({
  clearFiles,
  focusUploadZone
})
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

/* 增强的上传区域 */
.enhanced-upload-zone {
  min-height: 200px;
  padding: 40px 20px;
  border: 2px dashed #d9d9d9;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
  outline: none;
  position: relative;
}

.enhanced-upload-zone:hover,
.enhanced-upload-zone:focus {
  border-color: #1890ff;
  background: #f0f8ff;
}

.paste-zone-focused {
  border-color: #1890ff;
  background: #f0f8ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.drop-zone-active {
  border-color: #1890ff;
  background: #e6f7ff;
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.15);
}

.upload-content {
  text-align: center;
}

.upload-icon {
  margin-bottom: 16px;
}

.upload-text {
  font-size: 16px;
  color: #1f2937;
  margin: 0 0 8px 0;
  font-weight: 500;
}

.upload-hint {
  font-size: 14px;
  color: #6b7280;
  margin: 0 0 8px 0;
}

.upload-hint kbd {
  background: #f0f0f0;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  padding: 2px 6px;
  font-size: 12px;
  font-family: monospace;
}

.upload-format {
  font-size: 13px;
  color: #999;
  margin: 0;
}

/* 文件列表 */
.file-list {
  margin-top: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: white;
  max-height: 300px;
  overflow-y: auto;
}

.file-item {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #f0f0f0;
  transition: background-color 0.2s;
}

.file-item:last-child {
  border-bottom: none;
}

.file-item:hover {
  background: #f8f9fa;
}

.file-uploading {
  background: #f0f8ff;
}

.file-done {
  background: #f6ffed;
}

.file-error {
  background: #fff2f0;
}

.file-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 12px;
}

.file-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 6px;
  border: 1px solid #e8e8e8;
}

.file-details {
  flex: 1;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
  margin-bottom: 2px;
  word-break: break-all;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.file-size {
  font-size: 12px;
  color: #6b7280;
}

.file-status {
  margin-right: 8px;
  font-size: 16px;
}

.upload-progress {
  margin-top: 24px;
  padding: 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.progress-text {
  margin: 8px 0 0 0;
  font-size: 14px;
  color: #6b7280;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .enhanced-upload-zone {
    min-height: 160px;
    padding: 30px 16px;
  }
  
  .upload-text {
    font-size: 15px;
  }
  
  .upload-hint {
    font-size: 13px;
  }
  
  .file-item {
    padding: 10px 12px;
  }
  
  .file-thumb {
    width: 32px;
    height: 32px;
  }
}
</style>
