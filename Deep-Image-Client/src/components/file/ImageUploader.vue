<!--
  图库批量上传组件
  使用 CommonFileUploader 通用组件，专注于业务逻辑
-->
<template>
  <div class="image-uploader">
    <!-- 使用通用文件上传组件 -->
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
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import CommonFileUploader from '../common/CommonFileUploader.vue'
import { uploadFile } from '../../api/file'
import { BusinessType } from '../../types/file'

// Props - 保持外部 API 不变
interface Props {
  maxSize?: number // 最大文件大小，单位MB
  maxCount?: number // 最大文件数量
}

const props = withDefaults(defineProps<Props>(), {
  maxSize: 10,
  maxCount: 20
})

// Emits - 保持外部 API 不变
const emit = defineEmits<{
  success: [files: any[]]
  error: [error: any]
}>()

// State
const uploading = ref(false)
const uploadProgress = ref(0)
const currentFile = ref('')
const commonUploaderRef = ref<InstanceType<typeof CommonFileUploader>>()

// 组件挂载后自动聚焦，支持直接粘贴
onMounted(() => {
  setTimeout(() => {
    commonUploaderRef.value?.focus()
  }, 200) // 等待组件渲染完成
})

// 文件选择处理 - 调用上传 API
const handleFileSelect = async (files: File[]) => {
  // 批量上传文件
  for (const file of files) {
    await handleUploadSingle(file)
  }
}

// 单个文件上传
const handleUploadSingle = async (file: File) => {
  try {
    uploading.value = true
    uploadProgress.value = 0
    currentFile.value = file.name

    // 调用上传 API
    const response = await uploadFile(file, BusinessType.IMAGE)
    
    // 后端返回格式：{ code: number, message: string, data: T }
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

/* 上传进度 */
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
