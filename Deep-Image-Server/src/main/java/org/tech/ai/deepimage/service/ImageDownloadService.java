package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.model.dto.ImageInfo;
import org.tech.ai.deepimage.model.dto.response.DownloadResult;

import java.util.List;

/**
 * 图片下载服务接口
 */
public interface ImageDownloadService {

    /**
     * 批量下载图片
     *
     * @param images  图片信息列表
     * @param userId  用户ID
     * @param keyword 关键词
     * @param tagIds  标签ID列表（可选）
     * @return 下载结果详情
     */
    DownloadResult downloadImages(List<ImageInfo> images, Long userId, String keyword, List<Long> tagIds);
}
