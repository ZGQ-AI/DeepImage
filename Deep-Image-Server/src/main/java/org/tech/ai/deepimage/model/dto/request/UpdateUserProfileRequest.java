package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新用户信息请求
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class UpdateUserProfileRequest {
    
    /**
     * 用户名（可选，3-50个字符，支持字母、数字、下划线、中文）
     */
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50之间")
    @Pattern(regexp = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$", 
             message = "用户名只能包含字母、数字、下划线和中文")
    private String username;
    
    /**
     * 手机号（可选）
     */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
    
    /**
     * 头像URL（可选）
     */
    @Size(max = 500, message = "头像URL长度不能超过500")
    @Pattern(regexp = "^https?://.*", message = "头像URL格式不正确")
    private String avatarUrl;
}

