package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 删除其他会话响应
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@AllArgsConstructor
public class DeleteOtherSessionsResponse {
    
    /**
     * 成功删除的会话数量
     */
    private Integer deletedCount;
}

