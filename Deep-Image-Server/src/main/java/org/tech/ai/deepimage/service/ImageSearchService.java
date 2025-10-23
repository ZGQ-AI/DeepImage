package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.model.dto.ImageInfo;

import java.util.List;

/**
 * 图片搜索服务接口
 */
public interface ImageSearchService {

    /**
     * 搜索图片
     *
     * @param keyword 搜索关键词
     * @param count   需要的图片数量
     * @return 图片信息列表
     */
    List<ImageInfo> searchImages(String keyword, int count);

    /**
     * 清理图片URL，去除查询参数并处理协议
     *
     * @param originalUrl 原始URL
     * @return 清理后的URL
     */
    String cleanImageUrl(String originalUrl);
}
