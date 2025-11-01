package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Update user profile request
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class UpdateUserProfileRequest {
    
    /**
     * Username (optional, 3-50 characters, supports letters, numbers, underscores, Chinese)
     */
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", 
             message = "用户名只能包含字母、数字、下划线和中文")
    private String username;
    
    /**
     * Phone number (optional)
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * Avatar URL (optional)
     */
    @Size(max = 500, message = "头像URL长度不能超过500")
    @Pattern(regexp = "^https?://.*", message = "头像URL格式不正确")
    private String avatarUrl;
}

