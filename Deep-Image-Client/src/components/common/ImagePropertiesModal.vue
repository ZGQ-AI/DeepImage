<!--
  Image Properties Edit Modal Component
  Used for editing file properties (name, visibility, etc.)
-->
<template>
  <a-modal
    v-model:open="visible"
    title="编辑图片属性"
    :confirm-loading="loading"
    @ok="handleConfirm"
    @cancel="handleCancel"
    :width="500"
  >
    <div class="properties-modal-content">
      <!-- File Information Display (Read-only) -->
      <div class="info-section">
        <h4 style="margin-bottom: 12px; font-size: 14px; font-weight: 500;">文件信息</h4>
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="文件大小">
            {{ formatFileSize(fileDetail?.fileSize || 0) }}
          </a-descriptions-item>
          <a-descriptions-item label="文件后缀">
            {{ fileDetail?.fileExtension || '无' }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatTime(fileDetail?.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatTime(fileDetail?.updatedAt) }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- Editable Properties -->
      <div class="edit-section" style="margin-top: 16px;">
        <h4 style="margin-bottom: 12px; font-size: 14px; font-weight: 500;">可编辑属性</h4>
        
        <!-- Filename Input -->
        <div style="margin-bottom: 16px;">
          <label style="display: block; margin-bottom: 8px; font-weight: 500;">文件名</label>
          <a-input
            ref="filenameInputRef"
            v-model:value="filename"
            :placeholder="placeholder"
            @pressEnter="handleConfirm"
          />
          <p style="margin-top: 4px; color: #999; font-size: 12px;">
            文件扩展名: {{ extension ? extension.replace(/^\./, '') : '无' }}
          </p>
        </div>

        <!-- Visibility Selector -->
        <div>
          <label style="display: block; margin-bottom: 8px; font-weight: 500;">可见性</label>
          <a-select
            v-model:value="visibility"
            style="width: 100%"
            :options="visibilityOptions"
          />
        </div>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'
import type { FileDetailResponse } from '../../types/file'
import { formatFileSize } from '../../utils/file'
import { formatDateTime } from '../../utils/time'

interface Props {
  /** Whether to show */
  open: boolean
  /** File detail information */
  fileDetail: FileDetailResponse | null
  /** Loading state */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  fileDetail: null,
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'confirm': [data: { originalFilename?: string; visibility?: string }]
  'cancel': []
}>()

const visible = ref(props.open)
const filenameInputRef = ref<{ focus: () => void; select: () => void } | null>(null)
const filename = ref('')
const extension = ref('')
const visibility = ref<string>('PRIVATE')

const visibilityOptions = [
  { label: '私有 (PRIVATE)', value: 'PRIVATE' },
  { label: '公开 (PUBLIC)', value: 'PUBLIC' },
]

// Parse filename and extension
const parseFilename = (fullName: string, fileExtension?: string) => {
  if (!fullName) {
    return { name: '', extension: '' }
  }
  
  // Try to find extension in fullName first (check for dot)
  const lastDotIndex = fullName.lastIndexOf('.')
  if (lastDotIndex !== -1 && lastDotIndex > 0 && lastDotIndex < fullName.length - 1) {
    // Found a valid extension (has dot and characters after it)
    const name = fullName.substring(0, lastDotIndex)
    const ext = fullName.substring(lastDotIndex) // Includes dot, e.g., ".jpg"
    
    // Verify this looks like an extension (typically 2-5 chars after dot)
    const extWithoutDot = ext.substring(1)
    if (extWithoutDot.length >= 1 && extWithoutDot.length <= 5 && /^[a-zA-Z0-9]+$/.test(extWithoutDot)) {
      return {
        name,
        extension: ext,
      }
    }
    // If it doesn't look like an extension, treat as part of filename
  }
  
  // No extension found in fullName, try to use fileExtension from fileDetail
  if (fileExtension) {
    // Remove dot if present
    const extClean = fileExtension.replace(/^\./, '')
    if (extClean) {
      // Check if fullName ends with this extension (without dot)
      if (fullName.toLowerCase().endsWith(extClean.toLowerCase())) {
        // Remove extension from end of filename
        const name = fullName.substring(0, fullName.length - extClean.length)
        return {
          name,
          extension: `.${extClean}`,
        }
      } else {
        // Add extension
        return {
          name: fullName,
          extension: `.${extClean}`,
        }
      }
    }
  }
  
  // No extension available
  return {
    name: fullName,
    extension: '',
  }
}

const placeholder = computed(() => {
  // Display extension without dot for better UX
  const extDisplay = extension.value ? extension.value.replace(/^\./, '') : ''
  return extDisplay ? `请输入文件名（扩展名: ${extDisplay}）` : '请输入文件名'
})

const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  return formatDateTime(timeStr)
}

// Watch open changes
watch(
  () => props.open,
  (newVal) => {
    visible.value = newVal
    if (newVal && props.fileDetail) {
      const { name, extension: ext } = parseFilename(
        props.fileDetail.originalFilename,
        props.fileDetail.fileExtension
      )
      filename.value = name
      extension.value = ext // Extension already includes dot if exists
      visibility.value = props.fileDetail.visibility || 'PRIVATE'
      
      // Focus input field
      nextTick(() => {
        filenameInputRef.value?.focus()
        filenameInputRef.value?.select()
      })
    }
  }
)

// Watch visible changes and sync to parent component
watch(visible, (newVal) => {
  emit('update:open', newVal)
})

// Confirm
const handleConfirm = () => {
  const finalName = (filename.value.trim() + extension.value).trim()

  // Validate filename
  if (!finalName || finalName === extension.value) {
    // Filename cannot be empty, let parent component handle validation
    return
  }

  // Prepare update data
  const updateData: { originalFilename?: string; visibility?: string } = {}

  // Check if filename changed
  if (props.fileDetail && finalName !== props.fileDetail.originalFilename) {
    updateData.originalFilename = finalName
  }

  // Check if visibility changed
  if (props.fileDetail && visibility.value !== props.fileDetail.visibility) {
    updateData.visibility = visibility.value
  }

  // If no changes, just close
  if (Object.keys(updateData).length === 0) {
    visible.value = false
    return
  }

  emit('confirm', updateData)
}

// Cancel
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<style scoped>
.properties-modal-content {
  padding: 8px 0;
}

.info-section {
  margin-bottom: 8px;
}

.edit-section {
  border-top: 1px solid #f0f0f0;
  padding-top: 16px;
}
</style>

