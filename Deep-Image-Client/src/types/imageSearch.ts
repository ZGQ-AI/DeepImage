/**
 * Image search-related type definitions
 */

// Image information
export interface ImageInfo {
  url: string
  title?: string
  extension?: string
}

// Search request
export interface SearchImageRequest {
  keyword: string
  count: number
}

// Search response
export interface SearchImageResponse {
  images: ImageInfo[]
}

// Download request
export interface ImageDownloadRequest {
  keyword: string
  selectedImages: ImageInfo[]
  tagIds?: number[]  // Optional tag ID list
}

// Failed image information
export interface FailedImageInfo {
  url: string
  errorMessage: string
}

// Download result
export interface DownloadResult {
  status: 'completed' | 'partial' | 'failed'
  successCount: number
  failedCount: number
  totalCount: number
  totalTimeSeconds: number
  downloadedFileIds: number[]
  failedImages: FailedImageInfo[]
}

