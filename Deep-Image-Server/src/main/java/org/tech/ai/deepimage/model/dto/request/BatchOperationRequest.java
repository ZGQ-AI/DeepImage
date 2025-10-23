package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量操作请求（统一）
 * 用于批量删除、批量恢复等操作
 * 
 * @author zgq
 * @since 2025-10-23
 */
@Data
public class BatchOperationRequest {
    
    /**
     * 文件ID列表
     */
    @NotEmpty(message = "文件ID列表不能为空")
    @Size(max = 100, message = "单次最多操作100个文件")
    private List<Long> fileIds;
}

