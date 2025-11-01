package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Create file share request
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class CreateFileShareRequest {
    
    /**
     * File ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    /**
     * Target user ID to share with
     */
    @NotNull(message = "分享目标用户ID不能为空")
    private Long shareToUserId;
    
    /**
     * Share type
     * Optional values: PERMANENT, TEMPORARY
     */
    @NotBlank(message = "分享类型不能为空")
    private String shareType;
    
    /**
     * Expiration time (required for TEMPORARY type)
     */
    private LocalDateTime expiresAt;
    
    /**
     * Permission level
     * Optional values: VIEW, DOWNLOAD, EDIT
     */
    @NotBlank(message = "权限级别不能为空")
    private String permissionLevel;
    
    /**
     * Share message (optional)
     */
    @Size(max = 500, message = "留言不能超过500字符")
    private String message;
}

