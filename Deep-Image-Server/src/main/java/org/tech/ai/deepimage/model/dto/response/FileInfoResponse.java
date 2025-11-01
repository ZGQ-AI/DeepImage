package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * File information response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileInfoResponse {
    
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
     * Thumbnail URL
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
     * File extension
     */
    private String fileExtension;
    
    /**
     * Business type
     */
    private String businessType;
    
    /**
     * File status
     */
    private String status;
    
    /**
     * Access permission
     */
    private String visibility;
    
    /**
     * Tags associated with file
     */
    private List<TagResponse> tags;
    
    /**
     * Reference count
     */
    private Integer referenceCount;
    
    /**
     * Creation time
     */
    private LocalDateTime createdAt;
    
    /**
     * Update time
     */
    private LocalDateTime updatedAt;
}

