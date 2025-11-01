package org.tech.ai.deepimage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image information DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfo {

    /**
     * Image URL (parameters cleaned)
     */
    private String url;

    /**
     * Image title/description
     */
    private String title;

    /**
     * Image width
     */
    private Integer width;

    /**
     * Image height
     */
    private Integer height;

    /**
     * Original URL (not cleaned)
     */
    private String originalUrl;

    /**
     * Image file extension
     */
    private String extension;
}
