<template>
  <div class="view-mode-toggle">
    <a-tooltip title="网格视图">
      <a-button
        :type="currentMode === 'grid' ? 'primary' : 'default'"
        :class="{ active: currentMode === 'grid' }"
        @click="handleModeChange('grid')"
        size="small"
      >
        <AppstoreOutlined />
      </a-button>
    </a-tooltip>
    
    <a-tooltip title="列表视图">
      <a-button
        :type="currentMode === 'list' ? 'primary' : 'default'"
        :class="{ active: currentMode === 'list' }"
        @click="handleModeChange('list')"
        size="small"
      >
        <BarsOutlined />
      </a-button>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
import { AppstoreOutlined, BarsOutlined } from '@ant-design/icons-vue'

// 视图模式类型定义
export type ViewMode = 'grid' | 'list'

// Props
interface Props {
  modelValue: ViewMode
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:modelValue': [mode: ViewMode]
  change: [mode: ViewMode]
}>()

// 计算当前模式
const currentMode = computed(() => props.modelValue)

// 处理模式切换
const handleModeChange = (mode: ViewMode) => {
  if (mode !== currentMode.value) {
    emit('update:modelValue', mode)
    emit('change', mode)
  }
}
</script>

<script lang="ts">
import { computed } from 'vue'
</script>

<style scoped>
.view-mode-toggle {
  display: flex;
  gap: 4px;
  background: #f5f5f5;
  padding: 4px;
  border-radius: 8px;
  border: 1px solid #e5e7eb;
}

.view-mode-toggle :deep(.ant-btn) {
  border: none;
  background: transparent;
  box-shadow: none;
  color: #6b7280;
  transition: all 0.2s ease;
}

.view-mode-toggle :deep(.ant-btn:hover) {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
}

.view-mode-toggle :deep(.ant-btn-primary) {
  background: #1890ff;
  color: white;
}

.view-mode-toggle :deep(.ant-btn-primary:hover) {
  background: #40a9ff;
  color: white;
}

.view-mode-toggle :deep(.ant-btn.active) {
  background: #1890ff;
  color: white;
}
</style>
