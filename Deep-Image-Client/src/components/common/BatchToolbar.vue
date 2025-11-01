<!--
  Batch Operation Toolbar Component
  Used for pages that require batch operations like image gallery and recycle bin
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
        <!-- Default action button area, can be customized by parent component -->
      </slot>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** Whether all items are selected */
  isAllSelected: boolean
  /** Number of selected items */
  selectedCount: number
  /** Total count */
  totalCount: number
  /** Select all text */
  selectAllText?: string
  /** Total text template */
  totalTextTemplate?: string
  /** Selected text template */
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

// Compute total text
const totalText = computed(() => {
  return props.totalTextTemplate.replace('{count}', String(props.totalCount))
})

// Compute selected text
const selectedText = computed(() => {
  return props.selectedTextTemplate.replace('{count}', String(props.selectedCount))
})

// Handle select all / deselect all
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

