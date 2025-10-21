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
}

/**
 * 按业务类型查询文件请求
 */
export interface ListFilesByTypeRequest {
  /** 业务类型 */
  businessType: BusinessType
  /** 页码，默认第1页 */
  page?: number
  /** 每页数量，默认20条 */
  pageSize?: number
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
