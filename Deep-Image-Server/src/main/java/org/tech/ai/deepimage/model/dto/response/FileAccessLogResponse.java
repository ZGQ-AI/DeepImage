package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件访问日志响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileAccessLogResponse {
    
    /**
     * 日志ID
     */
    private Long logId;
    
    /**
     * 访问类型（DOWNLOAD, PREVIEW, UPLOAD）
     */
    private String accessType;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * User Agent
     */
    private String userAgent;
    
    /**
     * 访问时间
     */
    private LocalDateTime accessedAt;
}

