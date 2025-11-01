/**
 * File-related type definitions
 */

/**
 * File business type enumeration
 * Keep consistent with backend BusinessTypeEnum
 */
export enum BusinessType {
  /** Avatar */
  AVATAR = 'AVATAR',
  /** Document */
  DOCUMENT = 'DOCUMENT',
  /** Image */
  IMAGE = 'IMAGE',
  /** Video */
  VIDEO = 'VIDEO',
  /** Temporary file */
  TEMP = 'TEMP',
}

/**
 * Business type description mapping
 */
export const BusinessTypeLabels: Record<BusinessType, string> = {
  [BusinessType.AVATAR]: '头像',
  [BusinessType.DOCUMENT]: '文档',
  [BusinessType.IMAGE]: '图片',
  [BusinessType.VIDEO]: '视频',
  [BusinessType.TEMP]: '临时文件',
}

/**
 * File upload response
 */
export interface FileUploadResponse {
  /** File ID */
  fileId: number
  /** Original filename */
  originalFilename: string
  /** File access URL */
  fileUrl: string
  /** Thumbnail URL (image types only) */
  thumbnailUrl?: string
  /** File size in bytes */
  fileSize: number
  /** Content type */
  contentType: string
  /** File hash value */
  fileHash: string
  /** Upload time */
  uploadedAt: string
}

/**
 * File information response
 */
export interface FileInfoResponse {
  /** File ID */
  fileId: number
  /** Original filename */
  originalFilename: string
  /** File access URL */
  fileUrl: string
  /** Thumbnail URL */
  thumbnailUrl?: string
  /** File size in bytes */
  fileSize: number
  /** Content type */
  contentType: string
  /** File extension */
  fileExtension?: string
  /** Business type */
  businessType: string
  /** Creation time */
  createdAt: string
  /** Update time */
  updatedAt: string
  /** Associated tags */
  tags?: TagInfo[]
}


/**
 * Pagination response
 */
export interface PageResponse<T> {
  /** Current page data */
  records: T[]
  /** Total record count */
  total: number
  /** Current page number */
  current: number
  /** Page size */
  size: number
  /** Total pages */
  pages: number
}

/**
 * Query file list request (unified interface)
 */
export interface ListFilesRequest {
  /** Business type (optional) */
  businessType?: BusinessType
  /** Tag ID (optional) */
  tagId?: number
  /** Filename search keyword (optional) */
  filename?: string
  /** Sort field (optional): createdAt, fileSize, originalFilename */
  sortBy?: string
  /** Sort direction (optional): asc, desc */
  sortOrder?: string
  /** Page number, default page 1 */
  page?: number
  /** Page size, default 20 */
  pageSize?: number
}

/**
 * File details response
 */
export interface FileDetailResponse {
  /** File ID */
  fileId: number
  /** Original filename */
  originalFilename: string
  /** File access URL */
  fileUrl: string
  /** Thumbnail URL */
  thumbnailUrl?: string
  /** File size in bytes */
  fileSize: number
  /** Content type */
  contentType: string
  /** File extension */
  fileExtension?: string
  /** Business type */
  businessType: string
  /** File visibility: PRIVATE, PUBLIC, SHARED */
  visibility?: string
  /** File hash value (may be null when filterSensitive=true) */
  fileHash?: string | null
  /** Metadata (JSON, may be null when filterSensitive=true) */
  metadata?: Record<string, any> | null
  /** Creation time */
  createdAt: string
  /** Update time */
  updatedAt: string
  /** Associated tags */
  tags?: TagInfo[]
}

/**
 * Tag information
 */
export interface TagInfo {
  /** Tag ID */
  tagId: number
  /** Tag name */
  tagName: string
  /** Tag color */
  color?: string
}

/**
 * Batch operation request (unified)
 */
export interface BatchOperationRequest {
  /** File ID list */
  fileIds: number[]
}

/**
 * Operation result
 */
export interface OperationResult {
  /** File ID */
  fileId: number
  /** Status: success | failed */
  status: string
  /** Failure reason (only present when failed) */
  reason?: string
}

/**
 * Batch operation response (unified)
 */
export interface BatchOperationResponse {
  /** Total operation count */
  total: number
  /** Success count */
  success: number
  /** Failed count */
  failed: number
  /** Operation result details list */
  results: OperationResult[] | null
}

/**
 * Add file tags request
 */
export interface AddFileTagsRequest {
  /** File ID */
  fileId: number
  /** Tag ID list */
  tagIds: number[]
}


/**
 * File preview URL response
 */
export interface FilePreviewResponse {
  /** Preview URL */
  previewUrl: string
  /** Expiration time (seconds) */
  expirySeconds: number
}

/**
 * File statistics response
 */
export interface FileStatisticsResponse {
  /** Total file count */
  totalFileCount: number
  /** Total storage size (bytes) */
  totalFileSize: number
  /** Statistics by business type */
  byBusinessType: Record<string, FileTypeStatistics>
  /** Recently uploaded files */
  recentFiles?: FileInfoResponse[]
}

/**
 * File type statistics
 */
export interface FileTypeStatistics {
  /** File count */
  count: number
  /** Total size (bytes) */
  totalSize: number
}

/**
 * Check file exists request
 */
export interface FileExistsCheckRequest {
  /** File hash */
  fileHash: string
  /** Business type */
  businessType: BusinessType
}

/**
 * File exists response
 */
export interface FileExistsResponse {
  /** Whether exists */
  exists: boolean
  /** If exists, return file info */
  fileInfo?: FileInfoResponse
}


/**
 * Recycle bin query request
 */
export interface RecycleBinQueryRequest {
  /** Page number */
  page?: number
  /** Page size */
  size?: number
}

/**
 * Recycle bin statistics response
 */
export interface TrashStatsResponse {
  /** Recycle bin file count */
  count: number
  /** Recycle bin total size (bytes) */
  totalSize: number
}
