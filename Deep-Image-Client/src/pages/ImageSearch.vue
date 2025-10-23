<template>
  <div class="image-search">
    <div class="search-header">
      <h1>图片搜索</h1>
      <p class="search-description">通过关键词搜索网络图片，选择后下载到图库</p>
    </div>

    <!-- 步骤指示器 -->
    <div class="steps-container">
      <a-steps :current="currentStep" size="small">
        <a-step title="搜索图片" description="输入关键词搜索" />
        <a-step title="选择图片" description="预览并选择图片" />
        <a-step title="下载完成" description="保存到图库" />
      </a-steps>
    </div>

    <!-- 第一步：搜索表单 -->
    <div v-if="currentStep === 0" class="search-form">
      <a-card :bordered="false" style="margin-bottom: 24px">
        <div class="form-content">
          <div class="form-row">
            <div class="form-item">
              <label class="form-label">搜索关键词 *</label>
              <a-input
                v-model:value="searchForm.keyword"
                placeholder="输入要搜索的关键词，如：樱花、风景、动物"
                size="large"
                style="width: 100%"
                :maxlength="50"
                @pressEnter="handleSearch"
              />
            </div>
          </div>
          
          <div class="form-row">
            <div class="form-item">
              <label class="form-label">搜索数量</label>
              <a-slider
                v-model:value="searchForm.count"
                :min="1"
                :max="30"
                :marks="{ 1: '1', 10: '10', 20: '20', 30: '30' }"
                style="width: 100%; margin-top: 8px"
              />
              <div class="form-help">
                搜索 {{ searchForm.count }} 张图片供您选择
              </div>
            </div>
          </div>

          <div class="form-actions">
            <a-button 
              type="primary" 
              size="large"
              :loading="isSearching"
              :disabled="!searchForm.keyword.trim()"
              @click="handleSearch"
            >
              <SearchOutlined />
              搜索图片
            </a-button>
          </div>
        </div>
      </a-card>
    </div>

    <!-- 第二步：图片选择 -->
    <div v-if="currentStep === 1" class="image-selection">
      <a-card :bordered="false" style="margin-bottom: 24px">
        <template #title>
          <div class="selection-header">
            <span>找到 {{ searchResults?.images?.length || 0 }} 张图片</span>
            <div class="selection-stats">
              <span>已选择 {{ selectedImages.length }} 张</span>
              <a-button 
                v-if="selectedImages.length < (searchResults?.images?.length || 0)"
                type="link" 
                size="small"
                @click="selectAll"
              >
                全选
              </a-button>
              <a-button 
                v-if="selectedImages.length > 0"
                type="link" 
                size="small"
                @click="clearSelection"
              >
                清空
              </a-button>
            </div>
          </div>
        </template>

        <!-- 标签选择 -->
        <div class="tag-selection">
          <label class="form-label">添加标签（可选）</label>
          <a-select
            v-model:value="downloadForm.tagIds"
            mode="multiple"
            placeholder="选择要添加的标签"
            style="width: 100%; margin-bottom: 16px"
            allowClear
            :options="tagOptions"
          />
        </div>

        <!-- 图片网格 -->
        <div class="image-grid">
          <div 
            v-for="(image, index) in searchResults?.images"
            :key="index"
            class="image-item"
            :class="{ selected: selectedImages.includes(image) }"
            @click="toggleImageSelection(image)"
          >
            <div class="image-container">
              <img 
                :src="image.url" 
                :alt="image.title || '图片'"
                @error="handleImageError"
                loading="lazy"
              />
              <div class="image-overlay">
                <a-checkbox 
                  :checked="selectedImages.includes(image)"
                  @click.stop
                  @change="() => toggleImageSelection(image)"
                />
              </div>
            </div>
            <div class="image-info">
              <div class="image-title">{{ image.title || '无标题' }}</div>
            </div>
          </div>
        </div>

        <div class="selection-actions">
          <a-button @click="goBack">
            返回搜索
          </a-button>
          <a-button 
            type="primary"
            :disabled="selectedImages.length === 0"
            @click="handleDownload"
          >
            下载选中图片 ({{ selectedImages.length }})
          </a-button>
        </div>
      </a-card>
    </div>

    <!-- 第三步：下载进度和结果 -->
    <div v-if="currentStep === 2" class="download-progress">
      <!-- 下载中状态 -->
      <div v-if="isDownloading" class="downloading-status">
        <a-card title="正在下载" :bordered="false">
          <div class="downloading-content">
            <a-spin size="large" />
            <div class="downloading-text">
              正在下载 {{ selectedImages.length }} 张图片，请稍候...
            </div>
          </div>
        </a-card>
      </div>

      <!-- 下载结果 -->
      <div v-if="downloadResult && !isDownloading" class="download-result">
        <a-card title="下载结果" :bordered="false">
          <div class="result-summary">
            <a-descriptions :column="2" bordered>
              <a-descriptions-item label="下载状态">
                <a-tag :color="getStatusColor(downloadResult.status)">
                  {{ getStatusText(downloadResult.status) }}
                </a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="成功下载">
                <a-tag color="green">{{ downloadResult.successCount }} 张</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="下载失败">
                <a-tag color="red">{{ downloadResult.failedCount }} 张</a-tag>
              </a-descriptions-item>
              <a-descriptions-item label="总耗时">
                {{ downloadResult.totalTimeSeconds }} 秒
              </a-descriptions-item>
              <a-descriptions-item label="关键词">
                {{ lastSearchKeyword }}
              </a-descriptions-item>
              <a-descriptions-item label="总数量">
                {{ downloadResult.totalCount }} 张
              </a-descriptions-item>
            </a-descriptions>
          </div>

          <div class="result-actions">
            <a-button type="primary" @click="goToGallery">
              <PictureOutlined />
              查看图库
            </a-button>
            <a-button @click="startNewSearch">
              <ReloadOutlined />
              开始新搜索
            </a-button>
          </div>

          <!-- 失败列表 -->
          <div v-if="downloadResult.failedImages?.length > 0" class="failed-images">
            <h4>下载失败的图片：</h4>
            <a-list
              :data-source="downloadResult.failedImages"
              size="small"
            >
              <template #renderItem="{ item }">
                <a-list-item>
                  <div class="failed-item">
                    <span class="failed-url">{{ item.url }}</span>
                    <span class="failed-reason">{{ item.errorMessage }}</span>
                  </div>
                </a-list-item>
              </template>
            </a-list>
          </div>
        </a-card>
      </div>
    </div>

    <!-- 版权提醒 -->
    <a-alert
      type="warning"
      style="margin-top: 24px"
      message="请注意图片版权，仅用于学习和研究目的"
      show-icon
    />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { 
  SearchOutlined, 
  PictureOutlined, 
  ReloadOutlined 
} from '@ant-design/icons-vue'
import { useTagStore } from '@/stores/useTagStore'
import ImageSearchApi from '@/api/imageSearch'
import type { 
  SearchImageRequest, 
  SearchImageResponse,
  ImageInfo,
  ImageDownloadRequest,
  DownloadResult
} from '@/types/imageSearch'

const router = useRouter()
const tagStore = useTagStore()

// 响应式数据
const currentStep = ref(0) // 0:搜索 1:选择 2:下载
const searchForm = ref<SearchImageRequest>({
  keyword: '',
  count: 10
})

const downloadForm = ref<ImageDownloadRequest>({
  keyword: '',
  selectedImages: [],
  tagIds: []
})

const searchResults = ref<SearchImageResponse | null>(null)
const selectedImages = ref<ImageInfo[]>([])
const downloadResult = ref<DownloadResult | null>(null)
const isSearching = ref(false)
const isDownloading = ref(false)
const lastSearchKeyword = ref('')

// 计算属性
const tagOptions = computed(() => 
  tagStore.tags.map(tag => ({
    label: tag.tagName,
    value: tag.id
  }))
)

const isTaskRunning = computed(() => isDownloading.value)

// 方法
const handleSearch = async () => {
  if (!searchForm.value.keyword.trim()) {
    message.warning('请输入搜索关键词')
    return
  }

  try {
    isSearching.value = true
    lastSearchKeyword.value = searchForm.value.keyword
    
    const response = await ImageSearchApi.searchImages(searchForm.value)
    searchResults.value = response
    
    if (response.images && response.images.length > 0) {
      message.success(`搜索到 ${response.images.length} 张图片`)
      currentStep.value = 1 // 进入选择阶段
      
      // 准备下载表单
      downloadForm.value.keyword = searchForm.value.keyword
      downloadForm.value.selectedImages = []
      downloadForm.value.tagIds = []
      selectedImages.value = []
    } else {
      message.warning('未找到相关图片，请尝试其他关键词')
    }
  } catch (error: any) {
    message.error('搜索失败: ' + (error.message || '未知错误'))
  } finally {
    isSearching.value = false
  }
}

const toggleImageSelection = (image: ImageInfo) => {
  const index = selectedImages.value.findIndex(img => img.url === image.url)
  if (index > -1) {
    selectedImages.value.splice(index, 1)
  } else {
    selectedImages.value.push(image)
  }
  downloadForm.value.selectedImages = [...selectedImages.value]
}

const selectAll = () => {
  if (searchResults.value?.images) {
    selectedImages.value = [...searchResults.value.images]
    downloadForm.value.selectedImages = [...selectedImages.value]
  }
}

const clearSelection = () => {
  selectedImages.value = []
  downloadForm.value.selectedImages = []
}

const goBack = () => {
  currentStep.value = 0
  searchResults.value = null
  selectedImages.value = []
}

const handleDownload = async () => {
  if (selectedImages.value.length === 0) {
    message.warning('请选择要下载的图片')
    return
  }

  try {
    isDownloading.value = true
    
    // 进入下载阶段
    currentStep.value = 2
    downloadResult.value = null
    
    message.info(`正在下载 ${selectedImages.value.length} 张图片，请稍候...`)
    
    // 同步下载
    const result = await ImageSearchApi.downloadImages(downloadForm.value)
    downloadResult.value = result
    
    // 显示结果
    if (result.status === 'completed') {
      message.success(`下载完成！成功下载 ${result.successCount} 张图片`)
    } else if (result.status === 'partial') {
      message.warning(`部分下载成功：${result.successCount}/${result.totalCount} 张图片`)
    } else {
      message.error('下载失败，请重试')
    }
    
  } catch (error: any) {
    message.error('下载失败: ' + (error.message || '未知错误'))
  } finally {
    isDownloading.value = false
  }
}

const handleImageError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.style.display = 'none'
}

const getStatusColor = (status: string) => {
  const colorMap: Record<string, string> = {
    'completed': 'success',
    'partial': 'warning', 
    'failed': 'error'
  }
  return colorMap[status] || 'default'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    'completed': '全部成功',
    'partial': '部分成功',
    'failed': '全部失败'
  }
  return textMap[status] || status
}

const goToGallery = () => {
  router.push('/gallery')
}

const startNewSearch = () => {
  currentStep.value = 0
  downloadResult.value = null
  searchResults.value = null
  selectedImages.value = []
  searchForm.value.keyword = ''
  downloadForm.value = {
    keyword: '',
    selectedImages: [],
    tagIds: []
  }
}

// 生命周期
onMounted(async () => {
  // 获取标签列表
  await tagStore.fetchTags()
})

// 无需清理异步任务
</script>

<style scoped>
.image-search {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.search-header {
  margin-bottom: 32px;
  text-align: center;
}

.search-header h1 {
  font-size: 32px;
  font-weight: bold;
  color: #1890ff;
  margin-bottom: 8px;
}

.search-description {
  font-size: 16px;
  color: #666;
  margin: 0;
}

.steps-container {
  margin-bottom: 32px;
  max-width: 600px;
  margin-left: auto;
  margin-right: auto;
}

.form-content {
  max-width: 600px;
  margin: 0 auto;
}

.form-row {
  margin-bottom: 24px;
}

.form-item {
  width: 100%;
}

.form-label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  color: #333;
}

.form-help {
  margin-top: 4px;
  font-size: 14px;
  color: #666;
}

.form-actions {
  text-align: center;
  margin-top: 32px;
}

.task-progress,
.task-result {
  margin-top: 24px;
}

.image-selection {
  width: 100%;
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.selection-stats {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.tag-selection {
  margin-bottom: 24px;
}

.image-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}

.image-item {
  border: 2px solid transparent;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  transition: all 0.3s ease;
  background: #fafafa;
}

.image-item:hover {
  border-color: #1890ff;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.image-item.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.image-container {
  position: relative;
  width: 100%;
  height: 200px;
  overflow: hidden;
}

.image-container img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.image-item:hover .image-container img {
  transform: scale(1.05);
}

.image-overlay {
  position: absolute;
  top: 8px;
  right: 8px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 4px;
  padding: 4px;
}

.image-info {
  padding: 12px;
}

.image-title {
  font-size: 14px;
  color: #333;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.selection-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
}

.downloading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 40px;
}

.downloading-text {
  margin-top: 16px;
  font-size: 16px;
  color: #666;
}

.progress-content {
  max-width: 800px;
  margin: 0 auto;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 24px;
}

.progress-status {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.task-id {
  font-size: 12px;
  color: #999;
}

.progress-stats {
  display: flex;
  gap: 32px;
}

.progress-bar {
  margin-top: 16px;
}

.result-summary {
  margin-bottom: 24px;
}

.result-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-bottom: 24px;
}

.failed-images {
  margin-top: 24px;
}

.failed-images h4 {
  margin-bottom: 12px;
  color: #ff4d4f;
}

.failed-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.failed-url {
  flex: 1;
  font-family: monospace;
  font-size: 12px;
  color: #666;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  margin-right: 12px;
}

.failed-reason {
  color: #ff4d4f;
  font-size: 12px;
}

@media (max-width: 768px) {
  .image-search {
    padding: 16px;
  }
  
  .progress-info {
    flex-direction: column;
    gap: 16px;
  }
  
  .progress-stats {
    flex-wrap: wrap;
    gap: 16px;
  }
  
  .result-actions {
    flex-direction: column;
    align-items: center;
  }
  
  .selection-header {
    flex-direction: column;
    gap: 8px;
    align-items: flex-start;
  }
  
  .selection-stats {
    align-self: stretch;
    justify-content: space-between;
  }
  
  .image-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .image-container {
    height: 150px;
  }
  
  .selection-actions {
    flex-direction: column;
    gap: 12px;
  }
  
  .selection-actions .ant-btn {
    width: 100%;
  }
}
</style>
