package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * User profile response
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class UserProfileResponse {
    
    /**
     * User ID
     */
    private Long id;
    
    /**
     * Username
     */
    private String username;
    
    /**
     * Email address
     */
    private String email;
    
    /**
     * Phone number
     */
    private String phone;
    
    /**
     * Avatar URL
     */
    private String avatarUrl;
    
    /**
     * Email verification status
     */
    private Boolean verified;
    
    /**
     * Registration time
     */
    private LocalDateTime createdAt;
    
    /**
     * Last update time
     */
    private LocalDateTime updatedAt;
}

