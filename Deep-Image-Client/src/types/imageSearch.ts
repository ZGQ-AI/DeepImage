/**
 * 图片搜索相关类型定义
 */

// 图片信息
export interface ImageInfo {
  url: string
  title?: string
  extension?: string
}

// 搜索请求
export interface SearchImageRequest {
  keyword: string
  count: number
}

// 搜索响应
export interface SearchImageResponse {
  images: ImageInfo[]
}

// 下载请求
export interface ImageDownloadRequest {
  keyword: string
  selectedImages: ImageInfo[]
  tagIds?: number[]  // 可选的标签ID列表
}

// 失败图片信息
export interface FailedImageInfo {
  url: string
  errorMessage: string
}

// 下载结果
export interface DownloadResult {
  status: 'completed' | 'partial' | 'failed'
  successCount: number
  failedCount: number
  totalCount: number
  totalTimeSeconds: number
  downloadedFileIds: number[]
  failedImages: FailedImageInfo[]
}

