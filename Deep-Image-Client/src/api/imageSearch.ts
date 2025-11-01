import request from '@/request'
import type { 
  SearchImageRequest, 
  SearchImageResponse,
  ImageDownloadRequest,
  DownloadResult
} from '@/types/imageSearch'
import type { ApiResponse } from '@/types/api'

/**
 * Image search API
 */
export class ImageSearchApi {
  
  /**
   * Search images
   */
  static async searchImages(params: SearchImageRequest): Promise<SearchImageResponse> {
    const response = await request.post<ApiResponse<SearchImageResponse>>(
      '/api/crawler/search', 
      params
    )
    return response.data.data
  }

  /**
   * Download selected images (synchronous)
   */
  static async downloadImages(params: ImageDownloadRequest): Promise<DownloadResult> {
    const response = await request.post<ApiResponse<DownloadResult>>(
      '/api/crawler/download', 
      params
    )
    return response.data.data
  }

}

export default ImageSearchApi
