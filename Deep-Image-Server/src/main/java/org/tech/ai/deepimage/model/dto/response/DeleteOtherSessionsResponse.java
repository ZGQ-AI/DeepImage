package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Delete other sessions response
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@AllArgsConstructor
public class DeleteOtherSessionsResponse {
    
    /**
     * Number of successfully deleted sessions
     */
    private Integer deletedCount;
}

