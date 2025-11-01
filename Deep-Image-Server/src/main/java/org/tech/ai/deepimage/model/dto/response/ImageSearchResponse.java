package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tech.ai.deepimage.model.dto.ImageInfo;

import java.util.List;

/**
 * Image search response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchResponse {

    /**
     * Search keyword
     */
    private String keyword;

    /**
     * Found images list
     */
    private List<ImageInfo> images;

    /**
     * Actual found image count
     */
    private Integer totalCount;

    /**
     * Search elapsed time (milliseconds)
     */
    private Long searchTimeMs;
}
