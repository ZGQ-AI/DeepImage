package org.tech.ai.deepimage.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Google user information DTO
 * User information extracted from Google ID Token
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterGoogleUserRequest {
    
    /**
     * User email (required)
     */
    private String email;
    
    /**
     * User full name (optional)
     */
    private String name;
    
    /**
     * User avatar URL (optional)
     */
    private String picture;
}

