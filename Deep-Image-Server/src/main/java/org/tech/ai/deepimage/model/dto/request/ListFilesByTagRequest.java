package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 按标签查询文件请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class ListFilesByTagRequest extends PageRequest {
    
    /**
     * 标签ID
     */
    @NotNull(message = "标签ID不能为空")
    private Long tagId;
}

