package org.tech.ai.deepimage.model.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Google用户信息DTO
 * 从Google ID Token中提取的用户信息
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterGoogleUserRequest {
    
    /**
     * 用户邮箱（必需）
     */
    private String email;
    
    /**
     * 用户全名（可选）
     */
    private String name;
    
    /**
     * 用户头像URL（可选）
     */
    private String picture;
}

