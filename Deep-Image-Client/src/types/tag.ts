/**
 * Tag management-related type definitions
 */

/**
 * Create tag request
 */
export interface CreateTagRequest {
  /** Tag name */
  tagName: string
  /** Tag color (optional, format: #RRGGBB) */
  color?: string
}

/**
 * Update tag request
 */
export interface UpdateTagRequest {
  /** Tag ID */
  tagId: number
  /** Tag name (optional) */
  tagName?: string
  /** Tag color (optional) */
  color?: string
}

/**
 * Delete tag request
 */
export interface DeleteTagRequest {
  /** Tag ID */
  tagId: number
}

/**
 * Tag response
 */
export interface TagResponse {
  /** Tag ID */
  id: number
  /** Tag name */
  tagName: string
  /** Tag color */
  color?: string
  /** Usage count */
  usageCount: number
  /** Creation time */
  createdAt: string
}
