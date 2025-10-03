package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 文件存在性检查响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileExistsResponse {
    
    /**
     * 文件是否已存在
     */
    private Boolean exists;
    
    /**
     * 如果存在，返回文件ID
     */
    private Long fileId;
    
    /**
     * 如果存在，返回文件URL
     */
    private String fileUrl;
    
    /**
     * 提示信息
     */
    private String message;
}

