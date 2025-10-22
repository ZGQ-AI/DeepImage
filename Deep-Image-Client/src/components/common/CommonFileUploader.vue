<!--
  通用文件上传组件
  支持三种上传方式：点击选择、拖拽、粘贴
  提供灵活的配置以适应不同业务场景
-->
<template>
  <div class="common-file-uploader" :class="[`mode-${mode}`, { disabled }]">
    <!-- 上传区域 -->
    <div
      ref="uploadZoneRef"
      class="upload-zone"
      :class="{
        'drag-active': isDragActive,
        'paste-active': isPasteFocused
      }"
      :tabindex="disabled ? -1 : 0"
      @click="handleClickZone"
      @paste="handlePaste"
      @dragover.prevent="handleDragOver"
      @dragleave="handleDragLeave"
      @drop.prevent="handleDrop"
      @focus="isPasteFocused = true"
      @blur="isPasteFocused = false"
    >
      <div class="upload-content">
        <!-- 上传图标 -->
        <div class="upload-icon">
          <UploadOutlined :style="{ fontSize: '36px', color: '#1890ff' }" />
        </div>

        <!-- 上传文字提示 -->
        <div class="upload-text">
          <p class="main-text">
            {{ uploadText || getDefaultUploadText() }}
          </p>
          <p v-if="enableDragDrop || enablePaste" class="sub-text">
            <template v-if="enableDragDrop">拖拽文件到此处</template>
            <template v-if="enableDragDrop && enablePaste">，或</template>
            <template v-if="enablePaste">按 <kbd>Ctrl+V</kbd> (<kbd>⌘+V</kbd>) 粘贴</template>
          </p>
        </div>

        <!-- 点击选择文件按钮 - 主要操作入口 -->
        <a-button type="link" size="small" @click.stop="handleClickSelect">
          点击选择文件
        </a-button>

        <!-- 格式提示 -->
        <p class="format-hint">
          支持 {{ getFormatText() }}，最大 {{ maxSize }}MB
        </p>
      </div>
    </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      :accept="accept"
      :multiple="multiple"
      style="display: none"
      @change="handleFileInputChange"
    />

    <!-- 文件列表（可选） -->
    <div v-if="fileList.length > 0 && listType" class="file-list">
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
            v-if="listType === 'picture' && (file.thumbUrl || file.url)"
            :src="file.thumbUrl || file.url"
            :alt="file.name"
            class="file-thumb"
          />
          <FileOutlined v-else class="file-icon" />
          <div class="file-details">
            <div class="file-name">{{ file.name }}</div>
            <div class="file-size">{{ formatFileSize(file.size) }}</div>
          </div>
        </div>
        <div class="file-actions">
          <CheckCircleOutlined v-if="file.status === 'done'" class="status-icon success" />
          <CloseCircleOutlined v-else-if="file.status === 'error'" class="status-icon error" />
          <LoadingOutlined v-else-if="file.status === 'uploading'" class="status-icon loading" />
          <a-button
            v-if="!disabled"
            type="text"
            size="small"
            @click="handleRemoveFile(file)"
          >
            <DeleteOutlined />
          </a-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  UploadOutlined,
  FileOutlined,
  DeleteOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  LoadingOutlined
} from '@ant-design/icons-vue'
import type { CommonFileUploaderProps, CommonFileUploaderEmits, UploadFile } from '@/types/uploader'

// Props
const props = withDefaults(defineProps<CommonFileUploaderProps>(), {
  accept: 'image/*',
  maxSize: 10,
  multiple: false,
  maxCount: 1,
  mode: 'full',
  enableDragDrop: true,
  enablePaste: true,
  enableUrlInput: false,
  disabled: false,
  listType: undefined
})

// Emits
const emit = defineEmits<CommonFileUploaderEmits>()

// Refs
const uploadZoneRef = ref<HTMLDivElement>()
const fileInputRef = ref<HTMLInputElement>()
const fileList = ref<UploadFile[]>([])
const isDragActive = ref(false)
const isPasteFocused = ref(false)

// Computed
const getDefaultUploadText = () => {
  if (props.mode === 'compact') {
    return '选择文件上传'
  }
  return props.multiple ? '选择文件批量上传' : '选择文件上传'
}

const getFormatText = () => {
  if (props.accept === 'image/*') {
    return 'JPG、PNG、GIF、WebP'
  }
  return props.accept.replace('*/', '').toUpperCase()
}

// 点击上传区域
const handleClickZone = () => {
  if (props.disabled) return
  // 点击区域聚焦，方便粘贴
  uploadZoneRef.value?.focus()
}

// 点击选择文件按钮
const handleClickSelect = () => {
  if (props.disabled) return
  fileInputRef.value?.click()
}

// 文件输入变化
const handleFileInputChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  processFiles(Array.from(files))
  
  // 清空 input，允许选择相同文件
  target.value = ''
}

// 拖拽悬停
const handleDragOver = () => {
  if (props.disabled || !props.enableDragDrop) return
  isDragActive.value = true
}

// 拖拽离开
const handleDragLeave = () => {
  if (!props.enableDragDrop) return
  isDragActive.value = false
}

// 拖拽释放
const handleDrop = (event: DragEvent) => {
  if (props.disabled || !props.enableDragDrop) return
  isDragActive.value = false

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  processFiles(Array.from(files))
}

// 粘贴处理
const handlePaste = async (event: ClipboardEvent) => {
  if (props.disabled || !props.enablePaste) return

  const items = event.clipboardData?.items
  const text = event.clipboardData?.getData('text')

  // 优先处理图片文件
  if (items) {
    for (let i = 0; i < items.length; i++) {
      const item = items[i]
      if (item.type.startsWith('image/')) {
        event.preventDefault()
        const file = item.getAsFile()
        if (file) {
          processFiles([file])
          return
        }
      }
    }
  }

  // 如果启用了 URL 输入，处理图片链接
  if (props.enableUrlInput && text && isImageUrl(text)) {
    event.preventDefault()
    try {
      const file = await convertUrlToFile(text)
      if (file) {
        processFiles([file])
      }
    } catch (error) {
      message.error('无法加载图片 URL')
    }
  }
}

// 处理文件列表
const processFiles = (files: File[]) => {
  // 验证文件数量
  if (!props.multiple && files.length > 1) {
    message.warning('只能选择一个文件')
    files = [files[0]]
  }

  // 对于单选模式，直接替换；对于多选模式，检查总数限制
  if (props.multiple && props.maxCount && fileList.value.length + files.length > props.maxCount) {
    message.warning(`最多只能上传 ${props.maxCount} 个文件`)
    return
  }

  // 验证每个文件
  const validFiles: File[] = []
  for (const file of files) {
    if (validateFile(file)) {
      validFiles.push(file)
    }
  }

  if (validFiles.length === 0) return

  // 转换为 UploadFile 格式
  const uploadFiles: UploadFile[] = validFiles.map(file => ({
    uid: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    name: file.name,
    size: file.size,
    type: file.type,
    status: 'done',
    originFileObj: file,
    thumbUrl: file.type.startsWith('image/') ? URL.createObjectURL(file) : undefined
  }))

  // 更新文件列表
  if (props.multiple) {
    fileList.value = [...fileList.value, ...uploadFiles]
  } else {
    fileList.value = uploadFiles
  }

  // 触发事件
  emit('file-select', validFiles)
  emit('change', fileList.value)
}

// 文件验证
const validateFile = (file: File): boolean => {
  // 类型验证
  if (props.accept !== '*' && !matchAccept(file.type, props.accept)) {
    message.error(`不支持的文件类型: ${file.type}`)
    return false
  }

  // 大小验证
  const maxBytes = props.maxSize * 1024 * 1024
  if (file.size > maxBytes) {
    message.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }

  return true
}

// 匹配 accept 类型
const matchAccept = (fileType: string, accept: string): boolean => {
  const acceptTypes = accept.split(',').map(t => t.trim())
  return acceptTypes.some(type => {
    if (type.endsWith('/*')) {
      const prefix = type.slice(0, -2)
      return fileType.startsWith(prefix + '/')
    }
    return fileType === type
  })
}

// 判断是否是图片 URL
const isImageUrl = (url: string): boolean => {
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    return false
  }
  const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg']
  return imageExts.some(ext => url.toLowerCase().includes(ext))
}

// URL 转文件
const convertUrlToFile = async (url: string): Promise<File | null> => {
  try {
    const response = await fetch(url, { mode: 'cors' })
    const blob = await response.blob()
    const filename = url.split('/').pop() || 'pasted-image.png'
    return new File([blob], filename, { type: blob.type })
  } catch (error) {
    console.error('Failed to convert URL to file:', error)
    return null
  }
}

// 移除文件
const handleRemoveFile = (file: UploadFile) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
    emit('file-remove', file)
    emit('change', fileList.value)
    
    // 释放 Blob URL
    if (file.thumbUrl && file.thumbUrl.startsWith('blob:')) {
      URL.revokeObjectURL(file.thumbUrl)
    }
  }
}

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 暴露方法给父组件
defineExpose({
  clearFiles: () => {
    fileList.value.forEach(file => {
      if (file.thumbUrl && file.thumbUrl.startsWith('blob:')) {
        URL.revokeObjectURL(file.thumbUrl)
      }
    })
    fileList.value = []
  },
  getFiles: () => fileList.value,
  focus: () => {
    uploadZoneRef.value?.focus()
  }
})
</script>

<style scoped>
.common-file-uploader {
  width: 100%;
}

.upload-zone {
  position: relative;
  border: 2px dashed #d9d9d9;
  border-radius: 8px;
  background-color: #fafafa;
  cursor: pointer;
  transition: all 0.3s ease;
  outline: none;
}

.upload-zone:hover {
  border-color: #40a9ff;
  background-color: #f0f8ff;
}

.upload-zone.drag-active {
  border-color: #1890ff;
  background-color: #e6f7ff;
  border-style: solid;
}

.upload-zone.paste-active {
  border-color: #1890ff;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2);
}

.upload-zone.disabled {
  opacity: 0.6;
  cursor: not-allowed;
  pointer-events: none;
}

.upload-content {
  padding: 32px 16px;
  text-align: center;
}

.mode-compact .upload-content {
  padding: 24px 16px;
}

.upload-icon {
  margin-bottom: 12px;
}

.upload-text {
  margin-bottom: 16px;
}

.main-text {
  font-size: 16px;
  color: #595959;
  margin: 0 0 8px 0;
}

.mode-compact .main-text {
  font-size: 14px;
}

.sub-text {
  font-size: 14px;
  color: #8c8c8c;
  margin: 0;
}

.mode-compact .sub-text {
  font-size: 12px;
}

.sub-text kbd {
  padding: 2px 4px;
  font-size: 12px;
  color: #595959;
  background-color: #fafafa;
  border: 1px solid #d9d9d9;
  border-radius: 3px;
}

.format-hint {
  font-size: 12px;
  color: #8c8c8c;
  margin: 12px 0 0 0;
}

/* 文件列表 */
.file-list {
  margin-top: 16px;
}

.file-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 8px 12px;
  border: 1px solid #d9d9d9;
  border-radius: 4px;
  margin-bottom: 8px;
  transition: all 0.3s;
}

.file-item:hover {
  background-color: #fafafa;
}

.file-item.file-uploading {
  border-color: #1890ff;
  background-color: #e6f7ff;
}

.file-item.file-done {
  border-color: #52c41a;
}

.file-item.file-error {
  border-color: #ff4d4f;
  background-color: #fff1f0;
}

.file-info {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 0;
}

.file-thumb {
  width: 40px;
  height: 40px;
  object-fit: cover;
  border-radius: 4px;
  margin-right: 12px;
}

.file-icon {
  font-size: 24px;
  color: #1890ff;
  margin-right: 12px;
}

.file-details {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  color: #262626;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-size {
  font-size: 12px;
  color: #8c8c8c;
  margin-top: 2px;
}

.file-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: 12px;
}

.status-icon {
  font-size: 16px;
}

.status-icon.success {
  color: #52c41a;
}

.status-icon.error {
  color: #ff4d4f;
}

.status-icon.loading {
  color: #1890ff;
}
</style>

