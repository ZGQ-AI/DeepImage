package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * File detail response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileDetailResponse {
    
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
     * File hash value
     */
    private String fileHash;
    
    /**
     * Extended metadata (JSON)
     */
    private String metadata;
    
    /**
     * Total view count
     */
    private Integer viewCount;
    
    /**
     * Total download count
     */
    private Integer downloadCount;
    
    /**
     * Last access time
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * Tags associated with file
     */
    private List<TagResponse> tags;
    
    /**
     * Reference count
     */
    private Integer referenceCount;
    
    /**
     * Share information
     */
    private List<FileShareInfo> shares;
    
    /**
     * Creation time
     */
    private LocalDateTime createdAt;
    
    /**
     * Update time
     */
    private LocalDateTime updatedAt;
    
    /**
     * File share information
     */
    @Data
    @Builder
    public static class FileShareInfo {
        /**
         * Share ID
         */
        private Long shareId;
        
        /**
         * Share target username
         */
        private String shareToUsername;
        
        /**
         * Permission level
         */
        private String permissionLevel;
        
        /**
         * Creation time
         */
        private LocalDateTime createdAt;
    }
}

