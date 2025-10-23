<template>
  <div class="recycle-bin">
    <div class="bin-header">
      <h1>回收站</h1>
      <p class="bin-description">已删除的文件将在此保存，可以恢复或彻底删除</p>
    </div>

    <!-- 统计信息 -->
    <div v-if="stats" class="stats-card">
      <div class="stat-item">
        <span class="stat-label">文件数量</span>
        <span class="stat-value">{{ stats.count }} 个</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">占用空间</span>
        <span class="stat-value">{{ formatFileSize(stats.totalSize) }}</span>
      </div>
      <div class="stat-actions">
        <a-button 
          danger 
          :disabled="stats.count === 0"
          @click="handleEmptyBin"
        >
          清空回收站
        </a-button>
      </div>
    </div>

    <!-- 批量操作工具栏 -->
    <div v-if="images.length > 0 || pagination.total > 0" class="batch-toolbar">
      <div class="batch-toolbar-left">
        <a-checkbox 
          :checked="isAllSelected"
          :indeterminate="selectedFileIds.size > 0 && !isAllSelected"
          @change="handleSelectAll"
        >
          全选
        </a-checkbox>
        <a-divider type="vertical" />
        <span class="toolbar-label">
          共 {{ pagination.total }} 个文件
          <template v-if="selectedFileIds.size > 0">
            (已选 {{ selectedFileIds.size }} 个)
          </template>
        </span>
      </div>
      <div class="batch-toolbar-right">
        <ViewModeToggle v-model="viewMode" @change="handleViewModeChange" />
        
        <a-divider type="vertical" />
        
        <a-button 
          type="primary"
          :disabled="selectedFileIds.size === 0"
          @click="handleBatchRestore"
        >
          恢复 ({{ selectedFileIds.size }})
        </a-button>
        <a-button 
          type="primary" 
          danger 
          :disabled="selectedFileIds.size === 0"
          @click="handleBatchPermanentDelete"
        >
          彻底删除 ({{ selectedFileIds.size }})
        </a-button>
      </div>
    </div>

    <!-- 图片展示区域 -->
    <div class="bin-content">
      <!-- 空状态 -->
      <div v-if="images.length === 0 && !loading" class="empty-state">
        <DeleteOutlined :style="{ fontSize: '64px', color: '#d9d9d9' }" />
        <h3>回收站是空的</h3>
        <p>已删除的文件会在这里保留</p>
      </div>

      <!-- 图片列表 -->
      <div v-else-if="images.length > 0" class="images-container">
        <ImageGridView
          v-if="viewMode === 'grid'"
          :images="images"
          :loading="loading"
          :selection-mode="true"
          :selected-file-ids="selectedFileIds"
          @preview="handleImagePreview"
          @toggle-select="handleToggleSelect"
        />
        
        <ImageListView
          v-else-if="viewMode === 'list'"
          :images="images"
          :loading="loading"
          :selection-mode="true"
          :selected-file-ids="selectedFileIds"
          @preview="handleImagePreview"
          @toggle-select="handleToggleSelect"
        />
      </div>
      
      <!-- 分页 -->
      <div v-if="pagination.total > 0" class="pagination-wrapper">
        <a-pagination
          v-model:current="pagination.current"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :show-size-changer="true"
          :page-size-options="['10', '20', '50', '100']"
          :show-total="(total: number) => `共 ${total} 条`"
          @change="handlePageChange"
          @show-size-change="handlePageSizeChange"
        />
      </div>
    </div>

    <!-- 图片预览弹窗 -->
    <a-image-preview-group 
      :preview="{
        visible: previewVisible,
        onVisibleChange: (visible: boolean) => { previewVisible = visible },
        current: currentImageIndex
      }"
    >
      <a-image
        v-for="image in images"
        :key="image.fileId"
        :src="image.fileUrl"
        :alt="image.originalFilename"
        style="display: none"
      />
    </a-image-preview-group>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { message, Modal } from 'ant-design-vue'
import { DeleteOutlined } from '@ant-design/icons-vue'
import ImageGridView from '../components/file/ImageGridView.vue'
import ImageListView from '../components/file/ImageListView.vue'
import ViewModeToggle from '../components/file/ViewModeToggle.vue'
import { 
  getTrash, 
  batchRestoreFiles, 
  batchPermanentDeleteFiles, 
  emptyRecycleBin,
  getTrashStats 
} from '../api/file'
import type { FileInfoResponse, TrashStatsResponse } from '../types/file'
import type { ViewMode } from '../components/file/ViewModeToggle.vue'

// 状态管理
const images = ref<FileInfoResponse[]>([])
const loading = ref(false)
const viewMode = ref<ViewMode>('grid')
const previewVisible = ref(false)
const currentImageIndex = ref(0)
const stats = ref<TrashStatsResponse | null>(null)

// 分页状态
const pagination = ref({
  current: 1,
  pageSize: 10,
  total: 0
})

// 批量操作状态（回收站始终处于选择模式）
const selectedFileIds = ref<Set<number>>(new Set())

// 计算是否全选
const isAllSelected = computed(() => {
  return images.value.length > 0 && selectedFileIds.value.size === images.value.length
})

// 格式化文件大小
const formatFileSize = (bytes: number): string => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

// 加载统计信息
const loadStats = async () => {
  try {
    const response = await getTrashStats()
    if (response.data.code === 200) {
      stats.value = response.data.data
    }
  } catch (error) {
    console.error('加载统计信息失败:', error)
  }
}

// 加载回收站文件
const loadTrash = async () => {
  try {
    loading.value = true
    
    const response = await getTrash({
      page: pagination.value.current,
      size: pagination.value.pageSize
    })

    if (response.data.code === 200 && response.data.data) {
      const pageData = response.data.data
      images.value = pageData.records || []
      pagination.value.total = pageData.total || 0
      pagination.value.current = pageData.current || 1
      pagination.value.pageSize = pageData.size || 10
    }
  } catch (error: any) {
    console.error('加载回收站失败:', error)
    message.error('加载回收站失败')
  } finally {
    loading.value = false
  }
}

// 视图模式切换
const handleViewModeChange = (mode: ViewMode) => {
  localStorage.setItem('recycle-bin-view-mode', mode)
}

// 处理分页变化
const handlePageChange = (page: number, pageSize: number) => {
  pagination.value.current = page
  pagination.value.pageSize = pageSize
  selectedFileIds.value.clear() // 切换页面时清空选择
  loadTrash()
}

// 处理每页大小变化
const handlePageSizeChange = (current: number, size: number) => {
  pagination.value.current = 1 // 改变每页大小时重置到第一页
  pagination.value.pageSize = size
  selectedFileIds.value.clear() // 切换页面时清空选择
  loadTrash()
}

// 图片预览
const handleImagePreview = (image: FileInfoResponse) => {
  currentImageIndex.value = images.value.findIndex(img => img.fileId === image.fileId)
  if (currentImageIndex.value === -1) {
    currentImageIndex.value = 0
  }
  previewVisible.value = true
}

// 切换单个文件选择
const handleToggleSelect = (fileId: number) => {
  if (selectedFileIds.value.has(fileId)) {
    selectedFileIds.value.delete(fileId)
  } else {
    selectedFileIds.value.add(fileId)
  }
}

// 全选/取消全选
const handleSelectAll = (e: any) => {
  if (e.target.checked) {
    images.value.forEach(img => selectedFileIds.value.add(img.fileId))
  } else {
    selectedFileIds.value.clear()
  }
}

// 批量恢复
const handleBatchRestore = async () => {
  const count = selectedFileIds.value.size
  if (count === 0) {
    message.warning('请先选择要恢复的文件')
    return
  }

  Modal.confirm({
    title: '批量恢复确认',
    content: `确定要恢复选中的 ${count} 个文件吗？`,
    okText: '确认恢复',
    cancelText: '取消',
    onOk: async () => {
      try {
        message.loading({
          content: '正在恢复...',
          key: 'batch-restore',
          duration: 0
        })

        const fileIds = Array.from(selectedFileIds.value)
        const response = await batchRestoreFiles(fileIds)
        
        if (response.data.code === 200 && response.data.data) {
          const result = response.data.data
          
          selectedFileIds.value.clear()
          
          // 重新加载当前页和统计信息
          await Promise.all([loadTrash(), loadStats()])
          
          if (result.failed > 0) {
            message.warning({
              content: `恢复完成：成功 ${result.success} 个，失败 ${result.failed} 个`,
              key: 'batch-restore'
            })
          } else {
            message.success({
              content: `成功恢复 ${result.success} 个文件`,
              key: 'batch-restore'
            })
          }
        } else {
          throw new Error(response.data.message || '批量恢复失败')
        }
      } catch (error) {
        console.error('批量恢复失败:', error)
        message.error({
          content: `批量恢复失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: 'batch-restore'
        })
      }
    }
  })
}

// 批量彻底删除
const handleBatchPermanentDelete = async () => {
  const count = selectedFileIds.value.size
  if (count === 0) {
    message.warning('请先选择要删除的文件')
    return
  }

  Modal.confirm({
    title: '批量删除确认',
    content: `确定要彻底删除选中的 ${count} 个文件吗？此操作不可恢复！`,
    okText: '确认删除',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        message.loading({
          content: '正在删除...',
          key: 'batch-delete',
          duration: 0
        })

        const fileIds = Array.from(selectedFileIds.value)
        const response = await batchPermanentDeleteFiles(fileIds)
        
        if (response.data.code === 200 && response.data.data) {
          const result = response.data.data
          
          selectedFileIds.value.clear()
          
          // 重新加载当前页和统计信息
          await Promise.all([loadTrash(), loadStats()])
          
          if (result.failed > 0) {
            message.warning({
              content: `删除完成：成功 ${result.success} 个，失败 ${result.failed} 个`,
              key: 'batch-delete'
            })
          } else {
            message.success({
              content: `成功删除 ${result.success} 个文件`,
              key: 'batch-delete'
            })
          }
        } else {
          throw new Error(response.data.message || '批量删除失败')
        }
      } catch (error) {
        console.error('批量删除失败:', error)
        message.error({
          content: `批量删除失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: 'batch-delete'
        })
      }
    }
  })
}

// 清空回收站
const handleEmptyBin = async () => {
  Modal.confirm({
    title: '清空回收站',
    content: '确定要清空回收站吗？所有文件将被彻底删除，此操作不可恢复！',
    okText: '确认清空',
    okType: 'danger',
    cancelText: '取消',
    onOk: async () => {
      try {
        message.loading({
          content: '正在清空回收站...',
          key: 'empty-bin',
          duration: 0
        })

        const response = await emptyRecycleBin()
        
        if (response.data.code === 200 && response.data.data) {
          const result = response.data.data
          
          selectedFileIds.value.clear()
          pagination.value.current = 1 // 重置到第一页
          
          // 重新加载当前页和统计信息
          await Promise.all([loadTrash(), loadStats()])
          
          message.success({
            content: `已清空回收站，删除了 ${result.success} 个文件`,
            key: 'empty-bin'
          })
        } else {
          throw new Error(response.data.message || '清空回收站失败')
        }
      } catch (error) {
        console.error('清空回收站失败:', error)
        message.error({
          content: `清空回收站失败: ${error instanceof Error ? error.message : '未知错误'}`,
          key: 'empty-bin'
        })
      }
    }
  })
}

// 初始化用户偏好
const initUserPreferences = () => {
  const savedViewMode = localStorage.getItem('recycle-bin-view-mode') as ViewMode
  if (savedViewMode && ['grid', 'list'].includes(savedViewMode)) {
    viewMode.value = savedViewMode
  }
}

// 页面加载
onMounted(() => {
  initUserPreferences()
  loadTrash()
  loadStats()
})
</script>

<style scoped>
.recycle-bin {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.bin-header {
  text-align: center;
  margin-bottom: 32px;
}

.bin-header h1 {
  font-size: 32px;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 8px;
}

.bin-description {
  font-size: 16px;
  color: #6b7280;
  margin: 0;
}

.stats-card {
  display: flex;
  align-items: center;
  gap: 32px;
  padding: 24px;
  background: #f8faff;
  border: 1px solid #e0e7ff;
  border-radius: 12px;
  margin-bottom: 24px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 14px;
  color: #6b7280;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
}

.stat-actions {
  margin-left: auto;
}

.bin-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  padding: 0 8px;
}

.toolbar-left {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-right {
  display: flex;
  gap: 12px;
  align-items: center;
}

.toolbar-label {
  font-size: 14px;
  color: #6b7280;
  font-weight: 500;
}

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

.bin-content {
  min-height: 200px;
}

.empty-state {
  text-align: center;
  padding: 80px 24px;
}

.empty-state h3 {
  font-size: 20px;
  color: #374151;
  margin: 24px 0 8px;
}

.empty-state p {
  font-size: 14px;
  color: #6b7280;
  margin: 0;
}

.images-container {
  width: 100%;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
  padding: 16px;
}
</style>

