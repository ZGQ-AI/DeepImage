<template>
  <a-modal
    v-model:open="visible"
    title="管理标签"
    :width="600"
    :footer="null"
    @cancel="handleCancel"
  >
    <div class="tag-manager">
      <!-- 当前文件标签 -->
      <div class="section current-tags-section">
        <div class="section-header">
          <h4>当前标签</h4>
          <span class="tag-count">{{ currentTags.length }} 个</span>
        </div>
        <div class="section-content">
          <div v-if="currentTags.length > 0" class="tag-list">
            <a-tag
              v-for="tag in currentTags"
              :key="tag.id"
              :color="tag.color || 'blue'"
              closable
              class="tag-item"
              @close="handleRemoveTag(tag.id)"
            >
              {{ tag.tagName }}
            </a-tag>
          </div>
          <a-empty 
            v-else 
            description="暂无标签，点击下方添加" 
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            style="padding: 20px 0;"
          />
        </div>
      </div>

      <a-divider style="margin: 16px 0;" />

      <!-- 选择标签 -->
      <div class="section select-section">
        <div class="section-header">
          <h4>添加标签</h4>
          <span class="tag-count">共 {{ availableTags.length }} 个可选</span>
        </div>
        <div class="section-content">
          <div v-if="availableTags.length > 0" class="all-tags-grid">
            <a-tag
              v-for="tag in availableTags"
              :key="tag.id"
              :color="tag.color || 'default'"
              class="selectable-tag"
              @click="handleQuickAddTag(tag.id)"
            >
              <span class="tag-name">{{ tag.tagName }}</span>
              <span class="tag-usage">({{ tag.usageCount }})</span>
            </a-tag>
          </div>
          <a-empty 
            v-else 
            description="没有更多可添加的标签" 
            :image="Empty.PRESENTED_IMAGE_SIMPLE"
            style="padding: 20px 0;"
          >
            <template #description>
              <span style="color: #999;">
                没有更多可添加的标签<br/>
                请前往<router-link to="/tags" style="color: #1890ff;">标签管理</router-link>页面创建新标签
              </span>
            </template>
          </a-empty>
        </div>
      </div>

      <!-- 底部按钮 -->
      <div class="modal-footer">
        <a-button @click="handleCancel" size="large">关闭</a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { message, Empty } from 'ant-design-vue'
import { getFileTags, addFileTags, removeFileTag } from '../../api/file'
import { listTags } from '../../api/tag'
import type { TagResponse } from '../../types/tag'
import type { FileInfoResponse } from '../../types/file'

// Props
interface Props {
  open: boolean
  fileInfo: FileInfoResponse | null
}

const props = defineProps<Props>()

// Emits
const emit = defineEmits<{
  'update:open': [value: boolean]
  tagsUpdated: [tags: TagResponse[]]
}>()

// State
const visible = computed({
  get: () => props.open,
  set: (value) => emit('update:open', value)
})

const currentTags = ref<TagResponse[]>([])
const allTags = ref<TagResponse[]>([])
const loading = ref(false)

// 可选择的标签（排除已有标签）
const availableTags = computed(() => {
  const currentTagIds = new Set(currentTags.value.map(t => t.id))
  return allTags.value.filter(tag => !currentTagIds.has(tag.id))
})

// 加载数据
const loadData = async () => {
  if (!props.fileInfo) return

  loading.value = true
  try {
    // 并行加载文件标签和所有标签
    const [fileTagsRes, allTagsRes] = await Promise.all([
      getFileTags(props.fileInfo.fileId),
      listTags()
    ])

    if (fileTagsRes.data.code === 200) {
      currentTags.value = fileTagsRes.data.data || []
    }

    if (allTagsRes.data.code === 200) {
      allTags.value = allTagsRes.data.data || []
    }
  } catch (error) {
    console.error('加载标签数据失败:', error)
    message.error('加载标签数据失败')
  } finally {
    loading.value = false
  }
}

// 监听弹窗打开
watch(() => props.open, (newVal) => {
  if (newVal) {
    loadData()
  }
})

// 快速添加单个标签
const handleQuickAddTag = async (tagId: number) => {
  if (!props.fileInfo) return

  // 检查是否已经有该标签
  if (currentTags.value.some(t => t.id === tagId)) {
    message.info('该标签已存在')
    return
  }

  try {
    // 合并现有标签ID和新标签ID（后端是先删除再插入，所以需要传入所有标签）
    const existingTagIds = currentTags.value.map(t => t.id)
    const allTagIds = [...existingTagIds, tagId]
    
    const response = await addFileTags({
      fileId: props.fileInfo.fileId,
      tagIds: allTagIds
    })

    if (response.data.code === 200) {
      currentTags.value = response.data.data || []
      message.success('标签添加成功')
      emit('tagsUpdated', currentTags.value)
    } else {
      throw new Error(response.data.message || '添加标签失败')
    }
  } catch (error) {
    console.error('添加标签失败:', error)
    message.error(`添加标签失败: ${error instanceof Error ? error.message : '未知错误'}`)
  }
}

// 移除标签
const handleRemoveTag = async (tagId: number) => {
  if (!props.fileInfo) return

  try {
    const response = await removeFileTag(props.fileInfo.fileId, tagId)

    if (response.data.code === 200) {
      currentTags.value = currentTags.value.filter(t => t.id !== tagId)
      message.success('标签移除成功')
      emit('tagsUpdated', currentTags.value)
    } else {
      throw new Error(response.data.message || '移除标签失败')
    }
  } catch (error) {
    console.error('移除标签失败:', error)
    message.error(`移除标签失败: ${error instanceof Error ? error.message : '未知错误'}`)
  }
}

// 关闭
const handleCancel = () => {
  visible.value = false
}
</script>

<style scoped>
.tag-manager {
  padding: 12px 0;
}

.section {
  margin-bottom: 8px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.section-header h4 {
  margin: 0;
  font-size: 15px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.88);
}

.tag-count {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.45);
}

.section-content {
  min-height: 40px;
}

/* 当前标签 */
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.tag-item {
  margin: 0 !important;
  cursor: default;
}

/* 可选标签网格 */
.all-tags-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  max-height: 280px;
  overflow-y: auto;
}

.selectable-tag {
  margin: 0 !important;
  cursor: pointer;
  user-select: none;
}

.tag-name {
  margin-right: 4px;
}

.tag-usage {
  color: rgba(0, 0, 0, 0.45);
  font-size: 12px;
}

/* 底部按钮 */
.modal-footer {
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: flex-end;
}

/* 滚动条样式 */
.all-tags-grid::-webkit-scrollbar {
  width: 6px;
}

.all-tags-grid::-webkit-scrollbar-track {
  background: #f0f0f0;
  border-radius: 3px;
}

.all-tags-grid::-webkit-scrollbar-thumb {
  background: #bfbfbf;
  border-radius: 3px;
}

.all-tags-grid::-webkit-scrollbar-thumb:hover {
  background: #999;
}

:deep(.ant-empty) {
  margin: 0;
}
</style>

