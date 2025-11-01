package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * File existence check response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileExistsResponse {
    
    /**
     * Whether file exists
     */
    private Boolean exists;
    
    /**
     * File ID if exists
     */
    private Long fileId;
    
    /**
     * File URL if exists
     */
    private String fileUrl;
    
    /**
     * Message
     */
    private String message;
}

