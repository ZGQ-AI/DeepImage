package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.model.dto.ImageInfo;

import java.util.List;

/**
 * Image search service interface
 */
public interface ImageSearchService {

    /**
     * Search images
     *
     * @param keyword Search keyword
     * @param count   Number of images needed
     * @return Image information list
     */
    List<ImageInfo> searchImages(String keyword, int count);

    /**
     * Clean image URL, remove query parameters and handle protocol
     *
     * @param originalUrl Original URL
     * @return Cleaned URL
     */
    String cleanImageUrl(String originalUrl);
}
