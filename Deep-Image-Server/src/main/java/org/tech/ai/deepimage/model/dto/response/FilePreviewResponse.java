package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件预览响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FilePreviewResponse {
    
    /**
     * 临时访问 URL（MinIO presigned URL）
     */
    private String previewUrl;
    
    /**
     * 有效期（秒）
     */
    private Integer expirySeconds;
    
    /**
     * 过期时间
     */
    private LocalDateTime expiresAt;
}

