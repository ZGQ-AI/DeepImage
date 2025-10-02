/**
 * 标签管理相关API接口
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
 * 创建标签
 */
export function createTag(data: CreateTagRequest) {
  return request.post<ApiResponse<TagResponse>>('/api/tags', data)
}

/**
 * 查询当前用户所有标签
 */
export function listTags() {
  return request.get<ApiResponse<TagResponse[]>>('/api/tags')
}

/**
 * 更新标签
 */
export function updateTag(data: UpdateTagRequest) {
  return request.put<ApiResponse<TagResponse>>('/api/tags', data)
}

/**
 * 删除标签
 */
export function deleteTag(data: DeleteTagRequest) {
  return request.delete<ApiResponse<void>>('/api/tags', { data })
}

