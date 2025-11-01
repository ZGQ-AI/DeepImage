package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File upload response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileUploadResponse {
    
    /**
     * File ID
     */
    private Long fileId;
    
    /**
     * Original file name
     */
    private String originalFilename;
    
    /**
     * File access URL
     */
    private String fileUrl;
    
    /**
     * Thumbnail URL (image types only)
     */
    private String thumbnailUrl;
    
    /**
     * File size (bytes)
     */
    private Long fileSize;
    
    /**
     * Content type
     */
    private String contentType;
    
    /**
     * File hash value
     */
    private String fileHash;
    
    /**
     * Upload time
     */
    private LocalDateTime uploadedAt;
}

