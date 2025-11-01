package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.model.dto.ImageInfo;
import org.tech.ai.deepimage.model.dto.response.DownloadResult;

import java.util.List;

/**
 * Image download service interface
 */
public interface ImageDownloadService {

    /**
     * Batch download images
     *
     * @param images  Image information list
     * @param userId  User ID
     * @param keyword Keyword
     * @param tagIds  Tag ID list (optional)
     * @return Download result details
     */
    DownloadResult downloadImages(List<ImageInfo> images, Long userId, String keyword, List<Long> tagIds);
}
