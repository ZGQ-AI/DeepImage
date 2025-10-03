package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 文件存在性检查请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class FileExistsCheckRequest {
    
    /**
     * 文件 SHA256 哈希值
     */
    @NotBlank(message = "文件哈希不能为空")
    @Size(min = 64, max = 64, message = "哈希值长度必须为64位")
    private String fileHash;
    
    /**
     * 原始文件名（可选）
     */
    private String originalFilename;
    
    /**
     * 业务类型（可选）
     */
    private String businessType;
}

