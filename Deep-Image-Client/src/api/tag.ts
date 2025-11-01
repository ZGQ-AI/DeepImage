/**
 * Tag management-related API interfaces
 */
import request from '../request'
import type { ApiResponse } from '../types/api'
import type {
  CreateTagRequest,
  UpdateTagRequest,
  DeleteTagRequest,
  TagResponse,
} from '../types/tag'

/**
 * Create tag
 */
export function createTag(data: CreateTagRequest) {
  return request.post<ApiResponse<TagResponse>>('/api/tags', data)
}

/**
 * Query all tags of current user
 */
export function listTags() {
  return request.get<ApiResponse<TagResponse[]>>('/api/tags')
}

/**
 * Update tag
 */
export function updateTag(data: UpdateTagRequest) {
  return request.put<ApiResponse<TagResponse>>('/api/tags', data)
}

/**
 * Delete tag
 */
export function deleteTag(data: DeleteTagRequest) {
  return request.delete<ApiResponse<void>>('/api/tags', { data })
}
