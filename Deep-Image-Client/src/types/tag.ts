/**
 * 标签管理相关类型定义
 */

/**
 * 创建标签请求
 */
export interface CreateTagRequest {
  /** 标签名称 */
  tagName: string
  /** 标签颜色（可选，格式：#RRGGBB） */
  color?: string
}

/**
 * 更新标签请求
 */
export interface UpdateTagRequest {
  /** 标签ID */
  tagId: number
  /** 标签名称（可选） */
  tagName?: string
  /** 标签颜色（可选） */
  color?: string
}

/**
 * 删除标签请求
 */
export interface DeleteTagRequest {
  /** 标签ID */
  tagId: number
}

/**
 * 标签响应
 */
export interface TagResponse {
  /** 标签ID */
  id: number
  /** 标签名称 */
  tagName: string
  /** 标签颜色 */
  color?: string
  /** 使用次数 */
  usageCount: number
  /** 创建时间 */
  createdAt: string
}

