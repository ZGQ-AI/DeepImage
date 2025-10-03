/**
 * 文件相关API接口
 */
import request from '../request'
import type { ApiResponse } from '../types/api'
import type {
  FileUploadResponse,
  BusinessType,
  FileInfoResponse,
  ListFilesByTypeRequest,
  PageResponse,
} from '../types/file'

/**
 * 上传文件
 * @param file 文件对象
 * @param businessType 业务类型
 * @param tagIds 标签ID列表（可选）
 */
export function uploadFile(
  file: File,
  businessType: BusinessType,
  tagIds?: number[]
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)
  
  if (tagIds && tagIds.length > 0) {
    tagIds.forEach(tagId => formData.append('tagIds', String(tagId)))
  }

  return request.post<ApiResponse<FileUploadResponse>>(
    '/api/files/upload',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    }
  )
}

/**
 * 按业务类型查询文件列表
 * @param requestData 查询参数
 */
export function listFilesByType(requestData: ListFilesByTypeRequest) {
  return request.post<ApiResponse<PageResponse<FileInfoResponse>>>(
    '/api/files/list-by-type',
    requestData
  )
}

