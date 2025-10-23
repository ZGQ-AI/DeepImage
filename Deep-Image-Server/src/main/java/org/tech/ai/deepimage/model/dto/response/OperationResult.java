package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单个文件操作结果
 * 
 * @author zgq
 * @since 2025-10-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationResult {
    
    /**
     * 文件ID
     */
    private Long fileId;
    
    /**
     * 操作状态: success 或 failed
     */
    private String status;
    
    /**
     * 失败原因（仅在失败时有值）
     */
    private String reason;
}

