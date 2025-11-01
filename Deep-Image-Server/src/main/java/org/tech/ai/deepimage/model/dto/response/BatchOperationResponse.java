package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Batch operation response (unified)
 * 
 * @author zgq
 * @since 2025-10-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResponse {
    
    /**
     * Total operation count
     */
    private Integer total;
    
    /**
     * Success count
     */
    private Integer success;
    
    /**
     * Failed count
     */
    private Integer failed;
}

