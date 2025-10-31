<!--
  状态显示组件
  用于显示操作状态（成功、部分成功、失败等）
-->
<template>
  <a-tag :color="statusColor">
    {{ statusText }}
  </a-tag>
</template>

<script setup lang="ts">
import { computed } from 'vue'

interface Props {
  /** 状态值 */
  status: 'completed' | 'partial' | 'failed' | string
}

const props = defineProps<Props>()

// 状态到颜色的映射
const statusColorMap: Record<string, string> = {
  completed: 'success',
  partial: 'warning',
  failed: 'error',
}

// 状态到文字的映射
const statusTextMap: Record<string, string> = {
  completed: '全部成功',
  partial: '部分成功',
  failed: '全部失败',
}

// 计算状态颜色
const statusColor = computed(() => {
  return statusColorMap[props.status] || 'default'
})

// 计算状态文字
const statusText = computed(() => {
  return statusTextMap[props.status] || props.status
})
</script>

<style scoped>
/* 组件样式由 a-tag 提供 */
</style>

