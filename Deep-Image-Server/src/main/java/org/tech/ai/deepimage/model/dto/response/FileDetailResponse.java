package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件详情响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileDetailResponse {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 文件访问URL
     */
    private String fileUrl;
    
    /**
     * 缩略图URL
     */
    private String thumbnailUrl;
    
    /**
     * 文件大小（字节）
     */
    private Long fileSize;
    
    /**
     * 内容类型
     */
    private String contentType;
    
    /**
     * 文件扩展名
     */
    private String fileExtension;
    
    /**
     * 业务类型
     */
    private String businessType;
    
    /**
     * 文件状态
     */
    private String status;
    
    /**
     * 访问权限
     */
    private String visibility;
    
    /**
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 扩展元数据（JSON）
     */
    private String metadata;
    
    /**
     * 总查看次数
     */
    private Integer viewCount;
    
    /**
     * 总下载次数
     */
    private Integer downloadCount;
    
    /**
     * 最后访问时间
     */
    private LocalDateTime lastAccessedAt;
    
    /**
     * 文件关联的标签
     */
    private List<TagResponse> tags;
    
    /**
     * 引用计数
     */
    private Integer referenceCount;
    
    /**
     * 分享信息
     */
    private List<FileShareInfo> shares;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
    
    /**
     * 文件分享信息
     */
    @Data
    @Builder
    public static class FileShareInfo {
        /**
         * 分享ID
         */
        private Long shareId;
        
        /**
         * 分享目标用户名
         */
        private String shareToUsername;
        
        /**
         * 权限级别
         */
        private String permissionLevel;
        
        /**
         * 创建时间
         */
        private LocalDateTime createdAt;
    }
}

