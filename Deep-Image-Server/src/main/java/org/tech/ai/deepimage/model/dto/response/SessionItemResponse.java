package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 会话项响应
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class SessionItemResponse {
    
    /**
     * 会话ID
     */
    private Long id;
    
    /**
     * 设备信息
     */
    private String deviceInfo;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * User-Agent
     */
    private String userAgent;
    
    /**
     * 会话状态：0=已撤销，1=活跃
     */
    private Integer active;
    
    /**
     * 最后刷新时间
     */
    private LocalDateTime lastRefreshAt;
    
    /**
     * 创建时间（登录时间）
     */
    private LocalDateTime createdAt;
    
    /**
     * 是否为当前会话
     */
    private Boolean isCurrent;
}

