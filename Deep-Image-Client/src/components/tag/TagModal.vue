<!--
  标签创建/编辑模态框
-->
<template>
  <a-modal
    :open="open"
    :title="isEdit ? '编辑标签' : '创建标签'"
    :confirm-loading="loading"
    @ok="handleOk"
    @cancel="handleCancel"
    okText="确定"
    cancelText="取消"
  >
    <a-form
      ref="formRef"
      :model="formState"
      :rules="rules"
      layout="vertical"
      style="margin-top: 24px"
    >
      <a-form-item label="标签名称" name="tagName">
        <a-input
          v-model:value="formState.tagName"
          placeholder="请输入标签名称（最多50个字符）"
          :maxlength="50"
          show-count
          allow-clear
        />
      </a-form-item>

      <a-form-item label="标签颜色" name="color">
        <div class="color-picker-wrapper">
          <a-input
            v-model:value="formState.color"
            placeholder="#RRGGBB"
            :maxlength="7"
            allow-clear
            style="flex: 1"
          >
            <template #prefix>
              <div
                class="color-preview"
                :style="{ backgroundColor: formState.color || '#1890ff' }"
              ></div>
            </template>
            <template #suffix>
              <div class="color-picker-button">
                <input
                  type="color"
                  v-model="formState.color"
                  class="color-input"
                  title="选择颜色"
                />
                <BgColorsOutlined class="color-picker-icon" />
              </div>
            </template>
          </a-input>
        </div>
        <div class="color-presets-section">
          <div class="color-presets-label">预设颜色：</div>
          <div class="color-presets">
            <div
              v-for="color in colorPresets"
              :key="color"
              class="color-preset-item"
              :class="{ active: formState.color?.toLowerCase() === color.toLowerCase() }"
              :style="{ backgroundColor: color }"
              @click="formState.color = color"
              :title="color"
            ></div>
          </div>
        </div>
      </a-form-item>
    </a-form>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import { BgColorsOutlined } from '@ant-design/icons-vue'
import type { FormInstance } from 'ant-design-vue'
import type { TagResponse } from '../../types/tag'

interface Props {
  open: boolean
  loading?: boolean
  tag?: TagResponse | null
}

interface Emits {
  (e: 'update:open', value: boolean): void
  (e: 'submit', data: { tagName: string; color?: string; tagId?: number }): void
}

const props = withDefaults(defineProps<Props>(), {
  loading: false,
  tag: null,
})

const emit = defineEmits<Emits>()

const formRef = ref<FormInstance>()
const formState = reactive({
  tagName: '',
  color: '#1890ff',
})

const isEdit = ref(false)

// 颜色预设
const colorPresets = [
  '#1890ff', // 蓝色
  '#52c41a', // 绿色
  '#faad14', // 橙色
  '#f5222d', // 红色
  '#722ed1', // 紫色
  '#eb2f96', // 粉色
  '#13c2c2', // 青色
  '#fa8c16', // 橙红
  '#a0d911', // 黄绿
  '#2f54eb', // 深蓝
]

// 表单验证规则
const rules = {
  tagName: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { max: 50, message: '标签名称不能超过50个字符', trigger: 'blur' },
  ],
  color: [
    {
      pattern: /^#[0-9A-Fa-f]{6}$/,
      message: '请输入正确的颜色格式（#RRGGBB）',
      trigger: 'blur',
    },
  ],
}

// 监听 tag 变化，初始化表单
watch(
  () => props.tag,
  (newTag) => {
    if (newTag) {
      isEdit.value = true
      formState.tagName = newTag.tagName
      formState.color = newTag.color || '#1890ff'
    } else {
      isEdit.value = false
      formState.tagName = ''
      formState.color = '#1890ff'
    }
  },
  { immediate: true },
)

// 监听 open 变化，重置表单
watch(
  () => props.open,
  (newOpen) => {
    if (!newOpen) {
      formRef.value?.resetFields()
    }
  },
)

// 提交表单
async function handleOk() {
  try {
    await formRef.value?.validate()
    // eslint-disable-next-line @typescript-eslint/no-explicit-any
    const data: any = {
      tagName: formState.tagName,
      color: formState.color,
    }
    if (isEdit.value && props.tag) {
      data.tagId = props.tag.id
    }
    emit('submit', data)
  } catch (error) {
    console.error('表单验证失败:', error)
  }
}

// 取消
function handleCancel() {
  emit('update:open', false)
}
</script>

<style scoped>
.color-picker-wrapper {
  display: flex;
  align-items: center;
}

.color-preview {
  width: 16px;
  height: 16px;
  border-radius: 4px;
  border: 1px solid #d9d9d9;
}

.color-picker-button {
  position: relative;
  width: 24px;
  height: 24px;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
}

.color-input {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  opacity: 0;
  cursor: pointer;
}

.color-picker-icon {
  font-size: 18px;
  color: #8c8c8c;
  pointer-events: none;
  transition: color 0.3s;
}

.color-picker-button:hover .color-picker-icon {
  color: #1890ff;
}

.color-presets-section {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.color-presets-label {
  font-size: 12px;
  color: #8c8c8c;
  margin-bottom: 8px;
}

.color-presets {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-preset-item {
  width: 32px;
  height: 32px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.3s;
  border: 2px solid transparent;
}

.color-preset-item:hover {
  transform: scale(1.1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.color-preset-item.active {
  border-color: #000;
  transform: scale(1.1);
}

:deep(.ant-form-item) {
  margin-bottom: 20px;
}

:deep(.ant-input) {
  border-radius: 8px;
}

:deep(.ant-modal-body) {
  padding-top: 12px;
}
</style>
