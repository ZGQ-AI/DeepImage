import request from '@/request'
import type { 
  SearchImageRequest, 
  SearchImageResponse,
  ImageDownloadRequest,
  DownloadResult
} from '@/types/imageSearch'
import type { ApiResponse } from '@/types/api'

/**
 * 图片搜索 API
 */
export class ImageSearchApi {
  
  /**
   * 搜索图片
   */
  static async searchImages(params: SearchImageRequest): Promise<SearchImageResponse> {
    const response = await request.post<ApiResponse<SearchImageResponse>>(
      '/api/crawler/search', 
      params
    )
    return response.data.data
  }

  /**
   * 下载选中的图片（同步）
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
