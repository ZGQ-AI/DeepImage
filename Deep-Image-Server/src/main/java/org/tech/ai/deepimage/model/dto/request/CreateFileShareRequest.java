package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建文件分享请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class CreateFileShareRequest {
    
    /**
     * 文件ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    /**
     * 分享目标用户ID
     */
    @NotNull(message = "分享目标用户ID不能为空")
    private Long shareToUserId;
    
    /**
     * 分享类型
     * 可选值：PERMANENT, TEMPORARY
     */
    @NotBlank(message = "分享类型不能为空")
    private String shareType;
    
    /**
     * 过期时间（TEMPORARY 类型必填）
     */
    private LocalDateTime expiresAt;
    
    /**
     * 权限级别
     * 可选值：VIEW, DOWNLOAD, EDIT
     */
    @NotBlank(message = "权限级别不能为空")
    private String permissionLevel;
    
    /**
     * 分享留言（可选）
     */
    @Size(max = 500, message = "留言不能超过500字符")
    private String message;
}

