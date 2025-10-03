package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import org.tech.ai.deepimage.constant.FileConstant;

/**
 * 文件列表查询请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class ListFilesRequest extends PageRequest {
    
    /**
     * 业务类型（可选）
     */
    private String businessType;
    
    /**
     * 排序字段（可选）
     * 可选值：createdAt, fileSize, originalFilename
     */
    private String sortBy = FileConstant.SORT_BY_CREATED_AT;
    
    /**
     * 排序方向（可选）
     * 可选值：asc, desc
     */
    private String sortOrder = FileConstant.SORT_ORDER_DESC;
}

