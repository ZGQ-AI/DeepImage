/**
 * 文件相关类型定义
 */

/**
 * 文件业务类型枚举
 * 与后端 BusinessTypeEnum 保持一致
 */
export enum BusinessType {
  /** 头像 */
  AVATAR = 'AVATAR',
  /** 文档 */
  DOCUMENT = 'DOCUMENT',
  /** 图片 */
  IMAGE = 'IMAGE',
  /** 视频 */
  VIDEO = 'VIDEO',
  /** 临时文件 */
  TEMP = 'TEMP',
}

/**
 * 业务类型描述映射
 */
export const BusinessTypeLabels: Record<BusinessType, string> = {
  [BusinessType.AVATAR]: '头像',
  [BusinessType.DOCUMENT]: '文档',
  [BusinessType.IMAGE]: '图片',
  [BusinessType.VIDEO]: '视频',
  [BusinessType.TEMP]: '临时文件',
}

/**
 * 文件上传响应
 */
export interface FileUploadResponse {
  /** 文件ID */
  fileId: number
  /** 原始文件名 */
  originalFilename: string
  /** 文件访问 URL */
  fileUrl: string
  /** 缩略图 URL（仅图片类型） */
  thumbnailUrl?: string
  /** 文件大小（字节） */
  fileSize: number
  /** 内容类型 */
  contentType: string
  /** 文件哈希值 */
  fileHash: string
  /** 上传时间 */
  uploadedAt: string
}

/**
 * 文件信息响应
 */
export interface FileInfoResponse {
  /** 文件ID */
  fileId: number
  /** 原始文件名 */
  originalFilename: string
  /** 文件访问 URL */
  fileUrl: string
  /** 缩略图 URL */
  thumbnailUrl?: string
  /** 文件大小（字节） */
  fileSize: number
  /** 内容类型 */
  contentType: string
  /** 业务类型 */
  businessType: string
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
  /** 关联标签 */
  tags?: TagInfo[]
}


/**
 * 分页响应
 */
export interface PageResponse<T> {
  /** 当前页数据 */
  records: T[]
  /** 总记录数 */
  total: number
  /** 当前页码 */
  current: number
  /** 每页数量 */
  size: number
  /** 总页数 */
  pages: number
}

/**
 * 查询文件列表请求（统一接口）
 */
export interface ListFilesRequest {
  /** 业务类型（可选） */
  businessType?: BusinessType
  /** 标签ID（可选） */
  tagId?: number
  /** 文件名搜索关键词（可选） */
  filename?: string
  /** 排序字段（可选）：createdAt, fileSize, originalFilename */
  sortBy?: string
  /** 排序方向（可选）：asc, desc */
  sortOrder?: string
  /** 页码，默认第1页 */
  page?: number
  /** 每页数量，默认20条 */
  pageSize?: number
}

/**
 * 文件详情响应
 */
export interface FileDetailResponse {
  /** 文件ID */
  fileId: number
  /** 原始文件名 */
  originalFilename: string
  /** 文件访问 URL */
  fileUrl: string
  /** 缩略图 URL */
  thumbnailUrl?: string
  /** 文件大小（字节） */
  fileSize: number
  /** 内容类型 */
  contentType: string
  /** 业务类型 */
  businessType: string
  /** 文件哈希值 */
  fileHash: string
  /** 元数据（JSON） */
  metadata?: Record<string, any>
  /** 创建时间 */
  createdAt: string
  /** 更新时间 */
  updatedAt: string
  /** 关联标签 */
  tags?: TagInfo[]
}

/**
 * 标签信息
 */
export interface TagInfo {
  /** 标签ID */
  tagId: number
  /** 标签名称 */
  tagName: string
  /** 标签颜色 */
  color?: string
}

/**
 * 重命名文件请求
 */
export interface RenameFileRequest {
  /** 文件ID */
  fileId: number
  /** 新文件名 */
  newFilename: string
}

/**
 * 批量操作请求（统一）
 */
export interface BatchOperationRequest {
  /** 文件ID列表 */
  fileIds: number[]
}

/**
 * 操作结果
 */
export interface OperationResult {
  /** 文件ID */
  fileId: number
  /** 状态: success | failed */
  status: string
  /** 失败原因（仅在失败时有值） */
  reason?: string
}

/**
 * 批量操作响应（统一）
 */
export interface BatchOperationResponse {
  /** 总操作数量 */
  total: number
  /** 成功数量 */
  success: number
  /** 失败数量 */
  failed: number
  /** 操作结果详情列表 */
  results: OperationResult[] | null
}

/**
 * 添加文件标签请求
 */
export interface AddFileTagsRequest {
  /** 文件ID */
  fileId: number
  /** 标签ID列表 */
  tagIds: number[]
}


/**
 * 文件预览URL响应
 */
export interface FilePreviewResponse {
  /** 预览URL */
  previewUrl: string
  /** 过期时间（秒） */
  expirySeconds: number
}

/**
 * 文件统计响应
 */
export interface FileStatisticsResponse {
  /** 总文件数 */
  totalFileCount: number
  /** 总存储大小（字节） */
  totalFileSize: number
  /** 按业务类型统计 */
  byBusinessType: Record<string, FileTypeStatistics>
  /** 最近上传的文件 */
  recentFiles?: FileInfoResponse[]
}

/**
 * 文件类型统计
 */
export interface FileTypeStatistics {
  /** 文件数量 */
  count: number
  /** 总大小（字节） */
  totalSize: number
}

/**
 * 检查文件存在请求
 */
export interface FileExistsCheckRequest {
  /** 文件哈希 */
  fileHash: string
  /** 业务类型 */
  businessType: BusinessType
}

/**
 * 文件存在响应
 */
export interface FileExistsResponse {
  /** 是否存在 */
  exists: boolean
  /** 如果存在，返回文件信息 */
  fileInfo?: FileInfoResponse
}


/**
 * 回收站查询请求
 */
export interface RecycleBinQueryRequest {
  /** 页码 */
  page?: number
  /** 每页数量 */
  size?: number
}

/**
 * 回收站统计响应
 */
export interface TrashStatsResponse {
  /** 回收站文件数量 */
  count: number
  /** 回收站总大小（字节） */
  totalSize: number
}
