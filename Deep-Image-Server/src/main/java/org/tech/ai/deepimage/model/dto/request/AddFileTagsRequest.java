package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * Add file tags request
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class AddFileTagsRequest {
    
    /**
     * File ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    /**
     * Tag ID list
     */
    @NotEmpty(message = "标签ID列表不能为空")
    @Size(max = 10, message = "一次最多添加10个标签")
    private List<Long> tagIds;
}

