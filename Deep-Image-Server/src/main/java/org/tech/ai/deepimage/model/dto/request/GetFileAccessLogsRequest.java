package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 查询文件访问日志请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class GetFileAccessLogsRequest extends PageRequest {
    
    /**
     * 文件ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
}

