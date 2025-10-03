package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 批量删除文件请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class BatchDeleteFilesRequest {
    
    /**
     * 文件ID列表
     */
    @NotEmpty(message = "文件ID列表不能为空")
    @Size(max = 100, message = "一次最多删除100个文件")
    private List<Long> fileIds;
}

