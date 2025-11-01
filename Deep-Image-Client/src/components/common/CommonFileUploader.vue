<!--
  Common File Uploader Component
  Supports three upload methods: click to select, drag and drop, paste
  Provides flexible configuration to adapt to different business scenarios
-->
<template>
  <div class="common-file-uploader" :class="[`mode-${mode}`, { disabled }]">
    <!-- Upload area -->
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
        <!-- Upload icon -->
        <div class="upload-icon">
          <UploadOutlined :style="{ fontSize: '36px', color: '#1890ff' }" />
        </div>

        <!-- Upload text hint -->
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

        <!-- Click to select file button - main operation entry -->
        <a-button type="link" size="small" @click.stop="handleClickSelect">
          点击选择文件
        </a-button>

        <!-- Format hint -->
        <p class="format-hint">
          支持 {{ getFormatText() }}，最大 {{ maxSize }}MB
        </p>
      </div>
    </div>

    <!-- Hidden file input -->
    <input
      ref="fileInputRef"
      type="file"
      :accept="accept"
      :multiple="multiple"
      style="display: none"
      @change="handleFileInputChange"
    />

    <!-- File list (optional) -->
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
import { formatFileSize } from '@/utils/file'
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

// Click upload area
const handleClickZone = () => {
  if (props.disabled) return
  // Focus area for paste convenience
  uploadZoneRef.value?.focus()
}

// Click select file button
const handleClickSelect = () => {
  if (props.disabled) return
  fileInputRef.value?.click()
}

// File input change
const handleFileInputChange = (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  processFiles(Array.from(files))
  
  // Clear input to allow selecting the same file
  target.value = ''
}

// Drag over
const handleDragOver = () => {
  if (props.disabled || !props.enableDragDrop) return
  isDragActive.value = true
}

// Drag leave
const handleDragLeave = () => {
  if (!props.enableDragDrop) return
  isDragActive.value = false
}

// Drop
const handleDrop = (event: DragEvent) => {
  if (props.disabled || !props.enableDragDrop) return
  isDragActive.value = false

  const files = event.dataTransfer?.files
  if (!files || files.length === 0) return

  processFiles(Array.from(files))
}

// Paste handling
const handlePaste = async (event: ClipboardEvent) => {
  if (props.disabled || !props.enablePaste) return

  const items = event.clipboardData?.items
  const text = event.clipboardData?.getData('text')

  // Prioritize processing image files
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

  // If URL input is enabled, process image links
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

// Process file list
const processFiles = (files: File[]) => {
  // Validate file count
  if (!props.multiple && files.length > 1) {
    message.warning('只能选择一个文件')
    files = [files[0]]
  }

  // For single select mode, replace directly; for multiple select mode, check total limit
  if (props.multiple && props.maxCount && fileList.value.length + files.length > props.maxCount) {
    message.warning(`最多只能上传 ${props.maxCount} 个文件`)
    return
  }

  // Validate each file
  const validFiles: File[] = []
  for (const file of files) {
    if (validateFile(file)) {
      validFiles.push(file)
    }
  }

  if (validFiles.length === 0) return

  // Convert to UploadFile format
  const uploadFiles: UploadFile[] = validFiles.map(file => ({
    uid: `${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
    name: file.name,
    size: file.size,
    type: file.type,
    status: 'done',
    originFileObj: file,
    thumbUrl: file.type.startsWith('image/') ? URL.createObjectURL(file) : undefined
  }))

  // Update file list
  if (props.multiple) {
    fileList.value = [...fileList.value, ...uploadFiles]
  } else {
    fileList.value = uploadFiles
  }

  // Emit events
  emit('file-select', validFiles)
  emit('change', fileList.value)
}

// File validation
const validateFile = (file: File): boolean => {
  // Type validation
  if (props.accept !== '*' && !matchAccept(file.type, props.accept)) {
    message.error(`不支持的文件类型: ${file.type}`)
    return false
  }

  // Size validation
  const maxBytes = props.maxSize * 1024 * 1024
  if (file.size > maxBytes) {
    message.error(`文件大小不能超过 ${props.maxSize}MB`)
    return false
  }

  return true
}

// Match accept type
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

// Check if URL is an image URL
const isImageUrl = (url: string): boolean => {
  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    return false
  }
  const imageExts = ['.jpg', '.jpeg', '.png', '.gif', '.webp', '.bmp', '.svg']
  return imageExts.some(ext => url.toLowerCase().includes(ext))
}

// Convert URL to file
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

// Remove file
const handleRemoveFile = (file: UploadFile) => {
  const index = fileList.value.findIndex(f => f.uid === file.uid)
  if (index > -1) {
    fileList.value.splice(index, 1)
    emit('file-remove', file)
    emit('change', fileList.value)
    
    // Release Blob URL
    if (file.thumbUrl && file.thumbUrl.startsWith('blob:')) {
      URL.revokeObjectURL(file.thumbUrl)
    }
  }
}

// formatFileSize is imported from utils/file.ts

// Expose methods to parent component
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

/* File list */
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

