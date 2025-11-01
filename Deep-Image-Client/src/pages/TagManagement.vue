<!--
  Tag Management Page
-->
<template>
  <div class="tag-management-page">
    <!-- Page header -->
    <a-page-header title="标签管理" @back="() => router.back()">
      <template #subTitle>
        <span style="color: #8c8c8c">管理您的标签分类</span>
      </template>
      <template #extra>
        <a-space>
          <a-button @click="handleRefresh" :loading="tagStore.tagsLoading">
            <ReloadOutlined /> 刷新
          </a-button>
          <a-button type="primary" @click="handleCreate"> <PlusOutlined /> 创建标签 </a-button>
        </a-space>
      </template>
      <template #footer>
        <div class="statistics-wrapper">
          <a-row :gutter="16">
            <a-col :span="8">
              <a-statistic title="标签总数" :value="tagStore.tags.length">
                <template #prefix>
                  <TagsOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="8">
              <a-statistic title="总使用次数" :value="totalUsageCount">
                <template #prefix>
                  <FileImageOutlined />
                </template>
              </a-statistic>
            </a-col>
            <a-col :span="8">
              <a-statistic title="平均使用次数" :value="averageUsageCount" :precision="1">
                <template #prefix>
                  <LineChartOutlined />
                </template>
              </a-statistic>
            </a-col>
          </a-row>
        </div>
      </template>
    </a-page-header>

    <!-- Main content area -->
    <div class="content-wrapper">
      <a-card :bordered="false" class="tags-card">
        <TagList
          :tags="tagStore.tags"
          :loading="tagStore.tagsLoading"
          @edit="handleEdit"
          @delete="handleDelete"
        />
      </a-card>
    </div>

    <!-- Create/Edit tag modal -->
    <TagModal
      v-model:open="modalVisible"
      :loading="tagStore.operationLoading"
      :tag="currentTag"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import {
  ReloadOutlined,
  PlusOutlined,
  TagsOutlined,
  FileImageOutlined,
  LineChartOutlined,
} from '@ant-design/icons-vue'
import { useTagStore } from '../stores/useTagStore'
import TagList from '../components/tag/TagList.vue'
import TagModal from '../components/tag/TagModal.vue'
import type { TagResponse } from '../types/tag'

const router = useRouter()
const tagStore = useTagStore()

const modalVisible = ref(false)
const currentTag = ref<TagResponse | null>(null)

// Statistics data
const totalUsageCount = computed(() => {
  return tagStore.tags.reduce((sum, tag) => sum + tag.usageCount, 0)
})

const averageUsageCount = computed(() => {
  if (tagStore.tags.length === 0) return 0
  return totalUsageCount.value / tagStore.tags.length
})

// Refresh data
async function handleRefresh() {
  await tagStore.fetchTags()
}

// Create tag
function handleCreate() {
  currentTag.value = null
  modalVisible.value = true
}

// Edit tag
function handleEdit(tag: TagResponse) {
  currentTag.value = tag
  modalVisible.value = true
}

// Delete tag
async function handleDelete(tagId: number) {
  await tagStore.deleteTag(tagId)
}

// Submit form (create or edit)
async function handleSubmit(data: { tagName: string; color?: string; tagId?: number }) {
  try {
    if (data.tagId) {
      // Edit
      await tagStore.updateTag({
        tagId: data.tagId,
        tagName: data.tagName,
        color: data.color,
      })
    } else {
      // Create
      await tagStore.createTag({
        tagName: data.tagName,
        color: data.color,
      })
    }
    modalVisible.value = false
  } catch {
    // Errors are handled in store
  }
}

// Page initialization
onMounted(async () => {
  await handleRefresh()
})
</script>

<style scoped>
.tag-management-page {
  max-width: 1400px;
  margin: 0 auto;
}

:deep(.ant-page-header) {
  background-color: white;
  padding: 16px 24px;
  border-radius: 12px;
  margin-bottom: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

:deep(.ant-page-header-footer) {
  margin-top: 16px;
}

.statistics-wrapper {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 24px;
}

.statistics-wrapper :deep(.ant-statistic) {
  color: white;
}

.statistics-wrapper :deep(.ant-statistic-title) {
  color: rgba(255, 255, 255, 0.85);
  font-size: 14px;
  margin-bottom: 8px;
}

.statistics-wrapper :deep(.ant-statistic-content) {
  color: white;
  font-size: 24px;
  font-weight: 600;
}

.statistics-wrapper :deep(.ant-statistic-content-prefix) {
  margin-right: 8px;
  font-size: 20px;
  color: white;
}

.content-wrapper {
  padding: 0 24px;
}

.tags-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s;
  min-height: 400px;
}

.tags-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}

:deep(.ant-card-body) {
  padding: 24px;
}

/* Responsive design */
@media (max-width: 992px) {
  .content-wrapper {
    padding: 0 12px;
  }

  :deep(.ant-card-body) {
    padding: 16px;
  }

  .statistics-wrapper {
    padding: 16px;
  }

  .statistics-wrapper :deep(.ant-statistic-content) {
    font-size: 20px;
  }
}

@media (max-width: 768px) {
  :deep(.ant-page-header-heading-extra) {
    flex-direction: column;
    align-items: stretch;
  }

  :deep(.ant-space) {
    width: 100%;
  }

  :deep(.ant-space-item) {
    width: 100%;
  }

  :deep(.ant-btn) {
    width: 100%;
  }
}

@media (max-width: 576px) {
  :deep(.ant-page-header) {
    padding: 12px 16px;
  }

  .content-wrapper {
    padding: 0 8px;
  }

  .statistics-wrapper {
    padding: 12px;
  }

  .statistics-wrapper :deep(.ant-statistic-title) {
    font-size: 12px;
  }

  .statistics-wrapper :deep(.ant-statistic-content) {
    font-size: 18px;
  }

  :deep(.ant-col) {
    margin-bottom: 12px;
  }

  :deep(.ant-col:last-child) {
    margin-bottom: 0;
  }
}
</style>
