<!--
  批量操作工具栏组件
  用于图片库和回收站等需要批量操作的页面
-->
<template>
  <div class="batch-toolbar">
    <div class="batch-toolbar-left">
      <a-checkbox 
        :checked="isAllSelected"
        :indeterminate="selectedCount > 0 && !isAllSelected"
        @change="handleSelectAllChange"
      >
        {{ selectAllText }}
      </a-checkbox>
      <a-divider type="vertical" />
      <span class="toolbar-label">
        {{ totalText }}
        <template v-if="selectedCount > 0">
          ({{ selectedText }})
        </template>
      </span>
    </div>
    <div class="batch-toolbar-right">
      <slot name="actions">
        <!-- 默认操作按钮区域，可由父组件自定义 -->
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** 是否全选 */
  isAllSelected: boolean
  /** 已选中的数量 */
  selectedCount: number
  /** 总数 */
  totalCount: number
  /** 全选文字 */
  selectAllText?: string
  /** 总数文字模板 */
  totalTextTemplate?: string
  /** 已选文字模板 */
  selectedTextTemplate?: string
}

const props = withDefaults(defineProps<Props>(), {
  selectAllText: '全选',
  totalTextTemplate: '共 {count} 张图片',
  selectedTextTemplate: '已选 {count} 张'
})

const emit = defineEmits<{
  'select-all': [checked: boolean]
}>()

// 计算总数文字
const totalText = computed(() => {
  return props.totalTextTemplate.replace('{count}', String(props.totalCount))
})

// 计算已选文字
const selectedText = computed(() => {
  return props.selectedTextTemplate.replace('{count}', String(props.selectedCount))
})

// 全选/取消全选处理
const handleSelectAllChange = (e: { target: { checked: boolean } }) => {
  emit('select-all', e.target.checked)
}
</script>

<style scoped>
.batch-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  margin-bottom: 16px;
  background: #f0f9ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
}

.batch-toolbar-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.batch-toolbar-right {
  display: flex;
  gap: 12px;
}

.toolbar-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}
</style>

