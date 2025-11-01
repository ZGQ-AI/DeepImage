package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File access log response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileAccessLogResponse {
    
    /**
     * Log ID
     */
    private Long logId;
    
    /**
     * Access type (DOWNLOAD, PREVIEW, UPLOAD)
     */
    private String accessType;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * IP address
     */
    private String ipAddress;
    
    /**
     * User Agent
     */
    private String userAgent;
    
    /**
     * Access time
     */
    private LocalDateTime accessedAt;
}

