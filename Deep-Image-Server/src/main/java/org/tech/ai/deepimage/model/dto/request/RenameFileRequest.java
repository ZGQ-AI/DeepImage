package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * File rename request
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class RenameFileRequest {
    
    /**
     * File ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    /**
     * New file name
     */
    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过255字符")
    private String newFilename;
}

