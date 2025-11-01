package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * File properties update request
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Data
public class UpdateFilePropertiesRequest {
    
    /**
     * File ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    /**
     * New file name (optional)
     */
    @Size(max = 255, message = "文件名不能超过255字符")
    private String originalFilename;
    
    /**
     * File visibility: PRIVATE or PUBLIC (optional)
     */
    @Pattern(regexp = "PRIVATE|PUBLIC", message = "可见性值必须是 PRIVATE 或 PUBLIC")
    private String visibility;
    
    /**
     * Custom validation: at least one property must be provided
     */
    @AssertTrue(message = "至少需要提供一个要更新的字段")
    public boolean hasAnyPropertyToUpdate() {
        return (originalFilename != null && !originalFilename.trim().isEmpty()) || 
               (visibility != null && !visibility.trim().isEmpty());
    }
}

