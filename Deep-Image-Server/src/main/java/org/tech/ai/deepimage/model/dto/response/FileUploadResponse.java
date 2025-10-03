package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件上传响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileUploadResponse {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 原始文件名
     */
    private String originalFilename;
    
    /**
     * 文件访问 URL
     */
    private String fileUrl;
    
    /**
     * 缩略图 URL（仅图片类型）
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
     * 文件哈希值
     */
    private String fileHash;
    
    /**
     * 上传时间
     */
    private LocalDateTime uploadedAt;
}

