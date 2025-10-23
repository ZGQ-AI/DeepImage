package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 批量操作响应（统一）
 * 
 * @author zgq
 * @since 2025-10-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchOperationResponse {
    
    /**
     * 总操作数量
     */
    private Integer total;
    
    /**
     * 成功数量
     */
    private Integer success;
    
    /**
     * 失败数量
     */
    private Integer failed;
    
    /**
     * 操作结果详情列表
     */
    private List<OperationResult> results;
}

