package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File share response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileShareResponse {
    
    /**
     * Share ID
     */
    private Long shareId;
    
    /**
     * File ID
     */
    private Long fileId;
    
    /**
     * Original file name
     */
    private String originalFilename;
    
    /**
     * File URL
     */
    private String fileUrl;
    
    /**
     * Thumbnail URL
     */
    private String thumbnailUrl;
    
    /**
     * Sharer user ID
     */
    private Long shareFromUserId;
    
    /**
     * Sharer username
     */
    private String shareFromUsername;
    
    /**
     * Recipient user ID
     */
    private Long shareToUserId;
    
    /**
     * Recipient username
     */
    private String shareToUsername;
    
    /**
     * Share type
     */
    private String shareType;
    
    /**
     * Expiration time
     */
    private LocalDateTime expiresAt;
    
    /**
     * Permission level
     */
    private String permissionLevel;
    
    /**
     * Share message
     */
    private String message;
    
    /**
     * Whether revoked
     */
    private Boolean revoked;
    
    /**
     * View count
     */
    private Integer viewCount;
    
    /**
     * Download count
     */
    private Integer downloadCount;
    
    /**
     * Creation time
     */
    private LocalDateTime createdAt;
    
    /**
     * Update time
     */
    private LocalDateTime updatedAt;
}

