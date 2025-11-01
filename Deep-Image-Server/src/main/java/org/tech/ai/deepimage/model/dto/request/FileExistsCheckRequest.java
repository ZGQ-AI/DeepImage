package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * File existence check request
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class FileExistsCheckRequest {
    
    /**
     * File SHA256 hash value
     */
    @NotBlank(message = "文件哈希不能为空")
    @Size(min = 64, max = 64, message = "哈希值长度必须为64位")
    private String fileHash;
    
    /**
     * Original file name (optional)
     */
    private String originalFilename;
    
    /**
     * Business type (optional)
     */
    private String businessType;
}

