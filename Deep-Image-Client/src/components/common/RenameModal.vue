<!--
  Rename Dialog Component
  Used for file rename operations
-->
<template>
  <a-modal
    v-model:open="visible"
    title="重命名图片"
    :confirm-loading="loading"
    @ok="handleConfirm"
    @cancel="handleCancel"
  >
    <div class="rename-modal-content">
      <p style="margin-bottom: 8px;">请输入新的文件名：</p>
      <a-input
        ref="inputRef"
        v-model:value="filename"
        :placeholder="placeholder"
        @pressEnter="handleConfirm"
      />
      <p style="margin-top: 8px; color: #999; font-size: 12px;">
        文件扩展名: {{ extension || '无' }}
      </p>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, watch, nextTick, computed } from 'vue'

interface Props {
  /** Whether to show */
  open: boolean
  /** Current filename */
  currentFilename: string
  /** Loading state */
  loading?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  loading: false
})

const emit = defineEmits<{
  'update:open': [value: boolean]
  'confirm': [filename: string]
  'cancel': []
}>()

const visible = ref(props.open)
const inputRef = ref<{ focus: () => void; select: () => void } | null>(null)
const filename = ref('')
const extension = ref('')

// Parse filename and extension
const parseFilename = (fullName: string) => {
  const lastDotIndex = fullName.lastIndexOf('.')
  if (lastDotIndex !== -1) {
    return {
      name: fullName.substring(0, lastDotIndex),
      extension: fullName.substring(lastDotIndex)
    }
  }
  return {
    name: fullName,
    extension: ''
  }
}

// Watch open changes
watch(() => props.open, (newVal) => {
  visible.value = newVal
  if (newVal) {
    const { name, extension: ext } = parseFilename(props.currentFilename)
    filename.value = name
    extension.value = ext
    // Focus input field
    nextTick(() => {
      inputRef.value?.focus()
      inputRef.value?.select()
    })
  }
})

// Watch visible changes and sync to parent component
watch(visible, (newVal) => {
  emit('update:open', newVal)
})

const placeholder = computed(() => {
  return extension.value ? `请输入文件名（扩展名: ${extension.value}）` : '请输入文件名'
})

// Confirm rename
const handleConfirm = () => {
  const finalName = (filename.value.trim() + extension.value).trim()
  
  // Validate filename
  if (!finalName || finalName === extension.value) {
    // Filename cannot be empty, but don't show error here, let parent component handle it
    // because parent component may need custom error messages
    return
  }
  
  if (finalName === props.currentFilename) {
    visible.value = false
    return // Filename unchanged, close directly
  }
  
  emit('confirm', finalName)
}

// Cancel
const handleCancel = () => {
  visible.value = false
  emit('cancel')
}
</script>

<style scoped>
.rename-modal-content {
  padding: 8px 0;
}
</style>

