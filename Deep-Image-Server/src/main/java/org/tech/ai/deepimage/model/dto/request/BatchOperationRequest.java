package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Batch operation request (unified)
 * Used for batch delete, batch restore, etc.
 * 
 * @author zgq
 * @since 2025-10-23
 */
@Data
public class BatchOperationRequest {
    
    /**
     * File ID list
     */
    @NotEmpty(message = "文件ID列表不能为空")
    @Size(max = 100, message = "单次最多操作100个文件")
    private List<Long> fileIds;
}

