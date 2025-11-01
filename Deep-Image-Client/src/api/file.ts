/**
 * File-related API interfaces
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
 * Upload file
 * @param file File object
 * @param businessType Business type
 * @param tagIds Tag ID list (optional)
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
 * Query file list (unified interface)
 * Supports filtering by business type and tag ID, supports custom sorting
 * @param requestData Query parameters
 */
export function listFiles(requestData: ListFilesRequest) {
  return request.post<ApiResponse<PageResponse<FileInfoResponse>>>(
    '/api/files/list',
    requestData,
  )
}

/**
 * Query public files (no authentication required)
 * Returns all files with visibility = PUBLIC, sorted by creation time descending (newest first)
 * @param page Page number (default: 1)
 * @param pageSize Page size (default: 20)
 */
export function listPublicFiles(page = 1, pageSize = 20) {
  return request.get<ApiResponse<PageResponse<FileInfoResponse>>>('/api/files/public', {
    params: {
      page,
      pageSize,
    },
  })
}

/**
 * Download file
 * @param fileId File ID
 */
export function downloadFile(fileId: number) {
  return request.get(`/api/files/download`, {
    params: { fileId },
    responseType: 'blob',
  })
}

/**
 * Batch delete files (soft delete)
 * @param fileIds File ID list
 */
export function batchDeleteFiles(fileIds: number[]) {
  return request.delete<ApiResponse<import('../types/file').BatchOperationResponse>>('/api/files', {
    data: { fileIds }
  })
}

/**
 * Get file detail
 * @param fileId File ID
 * @param filterSensitive Whether to filter sensitive information (optional)
 */
export function getFileDetail(fileId: number, filterSensitive?: boolean) {
  return request.get<ApiResponse<import('../types/file').FileDetailResponse>>('/api/files/detail', {
    params: {
      fileId,
      ...(filterSensitive !== undefined && { filterSensitive }),
    },
  })
}

/**
 * Update file properties (name, visibility, etc.)
 * @param fileId File ID
 * @param data Update properties data (originalFilename and/or visibility)
 */
export function updateFileProperties(
  fileId: number,
  data: {
    originalFilename?: string
    visibility?: 'PRIVATE' | 'PUBLIC'
  }
) {
  return request.put<ApiResponse<FileInfoResponse>>('/api/files/update-properties', {
    fileId,
    ...data,
  })
}


/**
 * Add tags to file
 * @param data Add tags request
 */
export function addFileTags(data: AddFileTagsRequest) {
  return request.post<ApiResponse<TagResponse[]>>('/api/files/add-tags', data)
}

/**
 * Remove file tag
 * @param fileId File ID
 * @param tagId Tag ID
 */
export function removeFileTag(fileId: number, tagId: number) {
  return request.delete<ApiResponse<boolean>>('/api/files/remove-tag', {
    params: { fileId, tagId },
  })
}

/**
 * Query all tags of a file
 * @param fileId File ID
 */
export function getFileTags(fileId: number) {
  return request.get<ApiResponse<TagResponse[]>>('/api/files/tags', {
    params: { fileId },
  })
}

/**
 * Query recycle bin file list (supports pagination and sorting)
 * @param params Pagination query parameters
 */
export function getTrash(params?: import('../types/file').RecycleBinQueryRequest) {
  return request.get<ApiResponse<import('../types/file').PageResponse<import('../types/file').FileInfoResponse>>>('/api/files/trash', { params })
}

/**
 * Batch restore files
 * @param fileIds File ID list
 */
export function batchRestoreFiles(fileIds: number[]) {
  return request.post<ApiResponse<import('../types/file').BatchOperationResponse>>('/api/files/restore', {
    fileIds
  })
}

/**
 * Batch permanently delete files
 * @param fileIds File ID list
 */
export function batchPermanentDeleteFiles(fileIds: number[]) {
  return request.delete<ApiResponse<import('../types/file').BatchOperationResponse>>('/api/files/permanent', {
    data: { fileIds }
  })
}

/**
 * Empty recycle bin
 */
export function emptyRecycleBin() {
  return request.delete<ApiResponse<import('../types/file').BatchOperationResponse>>('/api/files/trash/empty')
}

/**
 * Get recycle bin statistics
 */
export function getTrashStats() {
  return request.get<ApiResponse<import('../types/file').TrashStatsResponse>>('/api/files/trash/stats')
}

