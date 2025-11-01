package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * Recycle bin statistics response
 */
@Data
@Builder
public class TrashStatsResponse {

    /**
     * Recycle bin file count
     */
    private Long count;

    /**
     * Recycle bin total size (bytes)
     */
    private Long totalSize;
}

