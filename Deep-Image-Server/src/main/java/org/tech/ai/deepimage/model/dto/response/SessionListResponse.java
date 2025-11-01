package org.tech.ai.deepimage.model.dto.response;

import lombok.Data;

import java.util.List;

/**
 * Session list response
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class SessionListResponse {
    
    /**
     * Session list
     */
    private List<SessionItemResponse> sessions;
    
    /**
     * Total count
     */
    private Long total;
    
    /**
     * Current page number
     */
    private Integer page;
    
    /**
     * Page size
     */
    private Integer pageSize;
}

