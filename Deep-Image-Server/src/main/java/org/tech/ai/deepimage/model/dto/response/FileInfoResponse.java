package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 文件信息响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileInfoResponse {
    
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
     * 文件关联的标签
     */
    private List<TagResponse> tags;
    
    /**
     * 引用计数
     */
    private Integer referenceCount;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
}

