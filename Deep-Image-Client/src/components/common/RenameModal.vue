<!--
  重命名对话框组件
  用于文件重命名操作
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
  /** 是否显示 */
  open: boolean
  /** 当前文件名 */
  currentFilename: string
  /** 加载状态 */
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

// 解析文件名和扩展名
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

// 监听 open 变化
watch(() => props.open, (newVal) => {
  visible.value = newVal
  if (newVal) {
    const { name, extension: ext } = parseFilename(props.currentFilename)
    filename.value = name
    extension.value = ext
    // 聚焦输入框
    nextTick(() => {
      inputRef.value?.focus()
      inputRef.value?.select()
    })
  }
})

// 监听 visible 变化，同步到父组件
watch(visible, (newVal) => {
  emit('update:open', newVal)
})

const placeholder = computed(() => {
  return extension.value ? `请输入文件名（扩展名: ${extension.value}）` : '请输入文件名'
})

// 确认重命名
const handleConfirm = () => {
  const finalName = (filename.value.trim() + extension.value).trim()
  
  // 验证文件名
  if (!finalName || finalName === extension.value) {
    // 文件名不能为空，但不在这里显示错误，由父组件处理
    // 因为父组件可能需要自定义错误消息
    return
  }
  
  if (finalName === props.currentFilename) {
    visible.value = false
    return // 文件名未改变，直接关闭
  }
  
  emit('confirm', finalName)
}

// 取消
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

