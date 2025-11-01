<!--
  Gallery Batch Upload Component
  Uses CommonFileUploader generic component, focuses on business logic
-->
<template>
  <div class="image-uploader">
    <!-- Use common file uploader component -->
    <CommonFileUploader
      ref="commonUploaderRef"
      :max-size="maxSize"
      :max-count="maxCount"
      :multiple="true"
      :enable-drag-drop="true"
      :enable-paste="true"
      :enable-url-input="true"
      mode="full"
      list-type="picture"
      accept="image/*"
      upload-text="选择图片批量上传"
      @file-select="handleFileSelect"
    />

    <!-- Upload progress -->
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
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import CommonFileUploader from '../common/CommonFileUploader.vue'
import { uploadFile } from '../../api/file'
import { BusinessType } from '../../types/file'

// Props - Keep external API unchanged
interface Props {
  maxSize?: number // Maximum file size in MB
  maxCount?: number // Maximum file count
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 10,
  maxCount: 20
})

// Emits - Keep external API unchanged
const emit = defineEmits<{
  success: [files: any[]]
  error: [error: any]
}>()

// State
const uploading = ref(false)
const uploadProgress = ref(0)
const currentFile = ref('')
const commonUploaderRef = ref<InstanceType<typeof CommonFileUploader>>()

// Auto focus after component mount, supports direct paste
onMounted(() => {
  setTimeout(() => {
    commonUploaderRef.value?.focus()
  }, 200) // Wait for component rendering to complete
})

// File selection handler - call upload API
const handleFileSelect = async (files: File[]) => {
  // Batch upload files
  for (const file of files) {
    await handleUploadSingle(file)
  }
}

// Single file upload
const handleUploadSingle = async (file: File) => {
  try {
    uploading.value = true
    uploadProgress.value = 0
    currentFile.value = file.name

    // Call upload API
    const response = await uploadFile(file, BusinessType.IMAGE)
    
    // Backend response format: { code: number, message: string, data: T }
    if (response.data.code === 200 && response.data.data) {
      message.success(`${file.name} 上传成功`)
      uploadProgress.value = 100
      emit('success', [response.data.data])
    } else {
      throw new Error(response.data.message || '上传失败')
    }
  } catch (error: any) {
    message.error(`${file.name} 上传失败: ${error.message}`)
    emit('error', error)
  } finally {
    uploading.value = false
    currentFile.value = ''
    uploadProgress.value = 0
  }
}
</script>

<style scoped>
.image-uploader {
  width: 100%;
}

/* Upload progress */
.upload-progress {
  margin-top: 16px;
  padding: 16px;
  background-color: #f0f8ff;
  border-radius: 8px;
  border: 1px solid #91d5ff;
}

.progress-text {
  margin-top: 8px;
  font-size: 14px;
  color: #595959;
  text-align: center;
}
</style>
