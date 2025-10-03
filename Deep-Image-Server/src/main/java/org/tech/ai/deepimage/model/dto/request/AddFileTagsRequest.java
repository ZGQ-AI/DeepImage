package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 添加文件标签请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class AddFileTagsRequest {
    
    /**
     * 文件ID
     */
    @NotNull(message = "文件ID不能为空")
    private Long fileId;
    
    /**
     * 标签ID列表
     */
    @NotEmpty(message = "标签ID列表不能为空")
    @Size(max = 10, message = "一次最多添加10个标签")
    private List<Long> tagIds;
}

