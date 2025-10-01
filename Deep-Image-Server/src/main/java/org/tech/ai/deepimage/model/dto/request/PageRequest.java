package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * 分页请求基础类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class PageRequest {
    
    /**
     * 页码，默认第1页
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;
    
    /**
     * 每页数量，默认20条
     */
    @Min(value = 1, message = "每页数量必须大于0")
    @Max(value = 100, message = "每页数量不能超过100")
    private Integer pageSize = 20;
    
    /**
     * 计算偏移量
     * @return offset
     */
    public Integer getOffset() {
        return (page - 1) * pageSize;
    }
}

