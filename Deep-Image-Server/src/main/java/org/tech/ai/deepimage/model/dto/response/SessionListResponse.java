package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * 会话列表响应
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class SessionListResponse {
    
    /**
     * 会话列表
     */
    private List<SessionItemResponse> sessions;
    
    /**
     * 总数
     */
    private Long total;
    
    /**
     * 当前页码
     */
    private Integer page;
    
    /**
     * 每页数量
     */
    private Integer pageSize;
}

