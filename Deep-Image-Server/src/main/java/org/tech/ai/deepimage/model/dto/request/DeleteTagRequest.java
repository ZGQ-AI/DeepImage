package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

/**
 * Delete tag request
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class DeleteTagRequest {
    
    /**
     * Tag ID
     */
    @NotNull(message = "标签ID不能为空")
    @Positive(message = "标签ID必须为正数")
    private Long tagId;
}

