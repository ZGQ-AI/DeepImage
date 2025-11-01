package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Session item response
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class SessionItemResponse {
    
    /**
     * Session ID
     */
    private Long id;
    
    /**
     * Device information
     */
    private String deviceInfo;
    
    /**
     * IP address
     */
    private String ipAddress;
    
    /**
     * User-Agent
     */
    private String userAgent;
    
    /**
     * Session status: 0=revoked, 1=active
     */
    private Integer active;
    
    /**
     * Last refresh time
     */
    private LocalDateTime lastRefreshAt;
    
    /**
     * Creation time (login time)
     */
    private LocalDateTime createdAt;
    
    /**
     * Whether it is the current session
     */
    private Boolean isCurrent;
}

