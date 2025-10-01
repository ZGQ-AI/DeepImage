package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息响应
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class UserProfileResponse {
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 邮箱地址
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 头像URL
     */
    private String avatarUrl;
    
    /**
     * 邮箱验证状态
     */
    private Boolean verified;
    
    /**
     * 注册时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 最后更新时间
     */
    private LocalDateTime updatedAt;
}

