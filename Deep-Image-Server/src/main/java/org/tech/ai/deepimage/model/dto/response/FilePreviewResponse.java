package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File preview response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FilePreviewResponse {
    
    /**
     * Temporary access URL (MinIO presigned URL)
     */
    private String previewUrl;
    
    /**
     * Validity period (seconds)
     */
    private Integer expirySeconds;
    
    /**
     * Expiration time
     */
    private LocalDateTime expiresAt;
}

