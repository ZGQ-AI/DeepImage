<!--
  Status Display Component
  Used to display operation status (success, partial success, failure, etc.)
-->
<template>
  <a-tag :color="statusColor">
    {{ statusText }}
  </a-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** Status value */
  status: 'completed' | 'partial' | 'failed' | string
}

const props = defineProps<Props>()

// Status to color mapping
const statusColorMap: Record<string, string> = {
  completed: 'success',
  partial: 'warning',
  failed: 'error',
}

// Status to text mapping
const statusTextMap: Record<string, string> = {
  completed: '全部成功',
  partial: '部分成功',
  failed: '全部失败',
}

// Compute status color
const statusColor = computed(() => {
  return statusColorMap[props.status] || 'default'
})

// Compute status text
const statusText = computed(() => {
  return statusTextMap[props.status] || props.status
})
</script>

<style scoped>
/* Component styles are provided by a-tag */
</style>

