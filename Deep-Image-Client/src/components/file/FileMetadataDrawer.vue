<!--
  File Metadata Drawer Component
  Displays detailed metadata information for files in a side drawer
-->
<template>
  <a-drawer
    v-model:open="visible"
    title="文件详情"
    placement="right"
    :width="420"
    :closable="true"
    @close="handleClose"
  >
    <div v-if="fileInfo" class="metadata-drawer-content">
      <!-- Editable Filename -->
      <div class="metadata-section">
        <h4 class="section-title">文件名</h4>
        <div class="filename-edit">
          <a-input
            ref="filenameInputRef"
            v-model:value="filename"
            :placeholder="placeholder"
            @pressEnter="handleRename"
          />
          <p class="extension-hint">
            文件扩展名: {{ extension ? extension.replace(/^\./, '') : '无' }}
          </p>
        </div>
      </div>

      <!-- Editable Visibility -->
      <div class="metadata-section">
        <h4 class="section-title">可见性</h4>
        <div class="visibility-edit">
          <a-select
            v-model:value="visibility"
            style="width: 100%"
            :options="visibilityOptions"
          />
          <p class="visibility-hint">
            设置文件的访问权限
          </p>
        </div>
      </div>

      <!-- Save Button -->
      <div class="metadata-section">
        <a-button 
          type="primary" 
          @click="handleSave"
          :loading="saving"
          block
        >
          保存修改
        </a-button>
      </div>

      <!-- File Basic Information -->
      <div class="metadata-section">
        <h4 class="section-title">基本信息</h4>
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="文件大小">
            {{ formatFileSize(fileInfo.fileSize) }}
          </a-descriptions-item>
          <a-descriptions-item label="文件类型">
            {{ fileInfo.contentType }}
          </a-descriptions-item>
          <a-descriptions-item label="文件后缀">
            {{ fileInfo.fileExtension || '无' }}
          </a-descriptions-item>
          <a-descriptions-item label="业务类型">
            {{ getBusinessTypeLabel(fileInfo.businessType) }}
          </a-descriptions-item>
          <a-descriptions-item label="可见性">
            {{ getVisibilityLabel(fileInfo.visibility || 'PRIVATE') }}
          </a-descriptions-item>
          <a-descriptions-item label="创建时间">
            {{ formatTime(fileInfo.createdAt) }}
          </a-descriptions-item>
          <a-descriptions-item label="更新时间">
            {{ formatTime(fileInfo.updatedAt) }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- Image Metadata (if available) -->
      <div v-if="imageMetadata" class="metadata-section">
        <h4 class="section-title">图片信息</h4>
        <a-descriptions :column="1" bordered size="small">
          <a-descriptions-item label="宽度">
            {{ imageMetadata.width }} px
          </a-descriptions-item>
          <a-descriptions-item label="高度">
            {{ imageMetadata.height }} px
          </a-descriptions-item>
          <a-descriptions-item label="尺寸比例">
            {{ getAspectRatio(imageMetadata.width, imageMetadata.height) }}
          </a-descriptions-item>
          <a-descriptions-item label="格式">
            {{ imageMetadata.format }}
          </a-descriptions-item>
          <a-descriptions-item label="色彩空间">
            {{ imageMetadata.colorSpace }}
          </a-descriptions-item>
          <a-descriptions-item label="位深度">
            {{ imageMetadata.bitDepth }} bit
          </a-descriptions-item>
          <a-descriptions-item label="透明通道">
            {{ imageMetadata.hasAlpha ? '是' : '否' }}
          </a-descriptions-item>
          <a-descriptions-item v-if="imageMetadata.orientation" label="方向">
            {{ formatOrientation(imageMetadata.orientation) }}
          </a-descriptions-item>
        </a-descriptions>
      </div>

      <!-- No Metadata Message -->
      <div v-else-if="fileInfo.metadata === null" class="metadata-section">
        <a-empty
          description="暂无元数据信息"
          :image="false"
        />
        <p class="empty-hint">
          此文件暂无提取的元数据信息。可能是上传时提取失败，或该文件类型暂不支持元数据提取。
        </p>
      </div>

      <!-- Raw JSON View (Collapsible) -->
      <div v-if="fileInfo.metadata" class="metadata-section">
        <a-collapse>
          <a-collapse-panel key="raw" header="查看原始 JSON 数据">
            <pre class="json-view">{{ formatJson(fileInfo.metadata) }}</pre>
            <div class="copy-action">
              <a-button 
                type="link" 
                size="small" 
                @click="copyToClipboard"
                :icon="h(CopyOutlined)"
              >
                复制 JSON
              </a-button>
            </div>
          </a-collapse-panel>
        </a-collapse>
      </div>
    </div>
  </a-drawer>
</template>

<script setup lang="ts">
import { ref, watch, computed, h, nextTick } from 'vue'
import { message } from 'ant-design-vue'
import { CopyOutlined } from '@ant-design/icons-vue'
import type { FileInfoResponse } from '../../types/file'
import { formatFileSize } from '../../utils/file'
import { formatDateTime } from '../../utils/time'
import { BusinessType, BusinessTypeLabels } from '../../types/file'

interface Props {
  /** Whether drawer is visible */
  open: boolean
  /** File information */
  fileInfo: FileInfoResponse | null
}

const props = withDefaults(defineProps<Props>(), {
  open: false,
  fileInfo: null,
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'close': []
  'save': [fileId: number, data: { originalFilename?: string; visibility?: string }]
}>()

const visible = ref(props.open)
const filenameInputRef = ref<{ focus: () => void; select: () => void } | null>(null)
const filename = ref('')
const extension = ref('')
const visibility = ref<string>('PRIVATE')
const saving = ref(false)

const visibilityOptions = [
  { label: '私有 (PRIVATE)', value: 'PRIVATE' },
  { label: '公开 (PUBLIC)', value: 'PUBLIC' },
]

// Parse filename and extension
const parseFilename = (fullName: string, fileExtension?: string) => {
  if (!fullName) {
    return { name: '', extension: '' }
  }
  
  const lastDotIndex = fullName.lastIndexOf('.')
  if (lastDotIndex !== -1 && lastDotIndex > 0 && lastDotIndex < fullName.length - 1) {
    const ext = fullName.substring(lastDotIndex)
    const extWithoutDot = ext.substring(1)
    if (extWithoutDot.length >= 1 && extWithoutDot.length <= 5 && /^[a-zA-Z0-9]+$/.test(extWithoutDot)) {
      return {
        name: fullName.substring(0, lastDotIndex),
        extension: ext,
      }
    }
  }
  
  if (fileExtension) {
    const extClean = fileExtension.replace(/^\./, '')
    if (extClean) {
      if (fullName.toLowerCase().endsWith(extClean.toLowerCase())) {
        return {
          name: fullName.substring(0, fullName.length - extClean.length),
          extension: `.${extClean}`,
        }
      } else {
        return {
          name: fullName,
          extension: `.${extClean}`,
        }
      }
    }
  }
  
  return {
    name: fullName,
    extension: '',
  }
}

const placeholder = computed(() => {
  const extDisplay = extension.value ? extension.value.replace(/^\./, '') : ''
  return extDisplay ? `请输入文件名（扩展名: ${extDisplay}）` : '请输入文件名'
})

// Get visibility label
const getVisibilityLabel = (visibility: string) => {
  const option = visibilityOptions.find(opt => opt.value === visibility)
  return option ? option.label : visibility
}

// Handle save (both filename and visibility)
const handleSave = async () => {
  if (!props.fileInfo) return
  
  const finalName = (filename.value.trim() + extension.value).trim()
  
  if (!finalName || finalName === extension.value) {
    message.warning('文件名不能为空')
    return
  }
  
  const updateData: { originalFilename?: string; visibility?: string } = {}
  
  // Check if filename changed
  if (finalName !== props.fileInfo.originalFilename) {
    updateData.originalFilename = finalName
  }
  
  // Check if visibility changed
  if (visibility.value !== (props.fileInfo.visibility || 'PRIVATE')) {
    updateData.visibility = visibility.value
  }
  
  // If no changes, just return
  if (Object.keys(updateData).length === 0) {
    message.info('没有修改任何内容')
    return
  }
  
  saving.value = true
  try {
    emit('save', props.fileInfo.fileId, updateData)
    // Parent component will handle success message and reload
  } catch (e) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

// Handle rename (for backward compatibility, now calls handleSave)
const handleRename = () => {
  handleSave()
}

// Parse metadata JSON
const imageMetadata = computed(() => {
  if (!props.fileInfo?.metadata) {
    return null
  }
  
  try {
    const metadata = JSON.parse(props.fileInfo.metadata)
    // Check if it's image metadata by checking for width and height
    if (metadata.width && metadata.height) {
      return metadata as ImageMetadata
    }
  } catch (e) {
    console.warn('Failed to parse metadata:', e)
  }
  
  return null
})

interface ImageMetadata {
  width: number
  height: number
  format: string
  colorSpace: string
  bitDepth?: number
  hasAlpha?: boolean
  orientation?: number
}

// Format time
const formatTime = (timeStr?: string) => {
  if (!timeStr) return '-'
  return formatDateTime(timeStr)
}

// Get business type label
const getBusinessTypeLabel = (businessType: string) => {
  return BusinessTypeLabels[businessType as BusinessType] || businessType
}

// Calculate aspect ratio
const getAspectRatio = (width: number, height: number) => {
  if (!width || !height) return '-'
  const gcd = (a: number, b: number): number => b === 0 ? a : gcd(b, a % b)
  const divisor = gcd(width, height)
  return `${width / divisor}:${height / divisor}`
}

// Format orientation
const formatOrientation = (orientation: number) => {
  const orientations: Record<number, string> = {
    1: '正常 (0°)',
    2: '水平翻转',
    3: '旋转 180°',
    4: '垂直翻转',
    5: '顺时针旋转 90° + 水平翻转',
    6: '顺时针旋转 90°',
    7: '逆时针旋转 90° + 水平翻转',
    8: '逆时针旋转 90°',
  }
  return orientations[orientation] || `未知 (${orientation})`
}

// Format JSON with indentation
const formatJson = (jsonStr: string) => {
  try {
    const obj = JSON.parse(jsonStr)
    return JSON.stringify(obj, null, 2)
  } catch (e) {
    return jsonStr
  }
}

// Copy to clipboard
const copyToClipboard = async () => {
  if (!props.fileInfo?.metadata) return
  
  try {
    await navigator.clipboard.writeText(props.fileInfo.metadata)
    message.success('已复制到剪贴板')
  } catch (e) {
    message.error('复制失败')
  }
}

// Watch open changes and initialize filename and visibility
watch(
  () => props.open,
  (newVal) => {
    visible.value = newVal
    if (newVal && props.fileInfo) {
      const { name, extension: ext } = parseFilename(
        props.fileInfo.originalFilename,
        props.fileInfo.fileExtension
      )
      filename.value = name
      extension.value = ext
      visibility.value = props.fileInfo.visibility || 'PRIVATE'
      
      // Focus input field
      nextTick(() => {
        filenameInputRef.value?.focus()
        filenameInputRef.value?.select()
      })
    }
  }
)

// Watch fileInfo changes to update filename and visibility
watch(
  () => props.fileInfo,
  (newFileInfo) => {
    if (newFileInfo) {
      const { name, extension: ext } = parseFilename(
        newFileInfo.originalFilename,
        newFileInfo.fileExtension
      )
      filename.value = name
      extension.value = ext
      if (newFileInfo.visibility) {
        visibility.value = newFileInfo.visibility
      }
    }
  },
  { deep: true }
)

// Watch visible changes and sync to parent
watch(visible, (newVal) => {
  emit('update:open', newVal)
})

// Handle close
const handleClose = () => {
  visible.value = false
  emit('close')
}
</script>

<style scoped>
.metadata-drawer-content {
  padding: 8px 0;
}

.metadata-section {
  margin-bottom: 24px;
}

.metadata-section:last-child {
  margin-bottom: 0;
}

.section-title {
  margin-bottom: 12px;
  font-size: 14px;
  font-weight: 500;
  color: #262626;
}

.empty-hint {
  margin-top: 16px;
  padding: 12px;
  background: #f5f5f5;
  border-radius: 4px;
  font-size: 12px;
  color: #8c8c8c;
  line-height: 1.6;
}

.json-view {
  background: #f5f5f5;
  border: 1px solid #e8e8e8;
  border-radius: 4px;
  padding: 12px;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.6;
  overflow-x: auto;
  max-height: 400px;
  overflow-y: auto;
  margin: 0;
}

.copy-action {
  margin-top: 8px;
  text-align: right;
}

:deep(.ant-descriptions-item-label) {
  font-weight: 500;
  width: 100px;
}

.filename-edit {
  margin-bottom: 8px;
}

.extension-hint {
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}

.visibility-edit {
  margin-bottom: 8px;
}

.visibility-hint {
  margin-top: 4px;
  color: #999;
  font-size: 12px;
}
</style>

