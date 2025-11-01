package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Batch delete response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class BatchDeleteResponse {
    
    /**
     * Success count
     */
    private Integer successCount;
    
    /**
     * Failed count
     */
    private Integer failedCount;
    
    /**
     * Failed file ID list
     */
    private List<Long> failedFileIds;
}

