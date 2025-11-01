package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Query file access logs request
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class GetFileAccessLogsRequest extends PageRequest {
    
    /**
     * File ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
}

