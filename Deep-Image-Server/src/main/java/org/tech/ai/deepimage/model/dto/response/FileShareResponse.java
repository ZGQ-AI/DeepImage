package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分享响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileShareResponse {
    
    /**
     * 分享ID
     */
    private Long shareId;
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 文件URL
     */
    private String fileUrl;
    
    /**
     * 缩略图URL
     */
    private String thumbnailUrl;
    
    /**
     * 分享者用户ID
     */
    private Long shareFromUserId;
    
    /**
     * 分享者用户名
     */
    private String shareFromUsername;
    
    /**
     * 接收者用户ID
     */
    private Long shareToUserId;
    
    /**
     * 接收者用户名
     */
    private String shareToUsername;
    
    /**
     * 分享类型
     */
    private String shareType;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
    
    /**
     * 权限级别
     */
    private String permissionLevel;
    
    /**
     * 分享留言
     */
    private String message;
    
    /**
     * 是否已撤销
     */
    private Boolean revoked;
    
    /**
     * 查看次数
     */
    private Integer viewCount;
    
    /**
     * 下载次数
     */
    private Integer downloadCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

