package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 批量删除响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class BatchDeleteResponse {
    
    /**
     * 成功数量
     */
    private Integer successCount;
    
    /**
     * 失败数量
     */
    private Integer failedCount;
    
    /**
     * 失败的文件ID列表
     */
    private List<Long> failedFileIds;
}

