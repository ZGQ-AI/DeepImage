package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 回收站查询请求
 */
@Data
public class RecycleBinQueryRequest {

    /**
     * 页码（从1开始）
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * 每页数量
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer size = 10;
}

