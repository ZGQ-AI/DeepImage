/**
 * 文件相关API接口
 */
import request from '../request'
import type { ApiResponse } from '../types/api'
import type {
  FileUploadResponse,
  BusinessType,
  FileInfoResponse,
  ListFilesRequest,
  PageResponse,
  AddFileTagsRequest,
  BatchOperationResponse,
} from '../types/file'
import type { TagResponse } from '../types/tag'

/**
 * 上传文件
 * @param file 文件对象
 * @param businessType 业务类型
 * @param tagIds 标签ID列表（可选）
 */
export function uploadFile(file: File, businessType: BusinessType, tagIds?: number[]) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('businessType', businessType)

  if (tagIds && tagIds.length > 0) {
    tagIds.forEach((tagId) => formData.append('tagIds', String(tagId)))
  }

  return request.post<ApiResponse<FileUploadResponse>>('/api/files/upload', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })
}

/**
 * 查询文件列表（统一接口）
 * 支持按业务类型、标签ID筛选，支持自定义排序
 * @param requestData 查询参数
 */
export function listFiles(requestData: ListFilesRequest) {
  return request.post<ApiResponse<PageResponse<FileInfoResponse>>>(
    '/api/files/list',
    requestData,
  )
}

/**
 * 下载文件
 * @param fileId 文件ID
 */
export function downloadFile(fileId: number) {
  return request.get(`/api/files/download`, {
    params: { fileId },
    responseType: 'blob',
  })
}

/**
 * 批量删除文件（软删除）
 * @param fileIds 文件ID列表
 */
export function batchDeleteFiles(fileIds: number[]) {
  return request.delete<ApiResponse<import('../types/file').BatchOperationResponse>>('/api/files', {
    data: { fileIds }
  })
}

/**
 * 重命名文件
 * @param fileId 文件ID
 * @param newFilename 新文件名
 */
export function renameFile(fileId: number, newFilename: string) {
  return request.post<ApiResponse<FileInfoResponse>>('/api/files/rename', {
    fileId,
    newFilename,
  })
}

/**
 * 为文件添加标签
 * @param data 添加标签请求
 */
export function addFileTags(data: AddFileTagsRequest) {
  return request.post<ApiResponse<TagResponse[]>>('/api/files/add-tags', data)
}

/**
 * 移除文件标签
 * @param fileId 文件ID
 * @param tagId 标签ID
 */
export function removeFileTag(fileId: number, tagId: number) {
  return request.delete<ApiResponse<boolean>>('/api/files/remove-tag', {
    params: { fileId, tagId },
  })
}

/**
 * 查询文件的所有标签
 * @param fileId 文件ID
 */
export function getFileTags(fileId: number) {
  return request.get<ApiResponse<TagResponse[]>>('/api/files/tags', {
    params: { fileId },
  })
}

