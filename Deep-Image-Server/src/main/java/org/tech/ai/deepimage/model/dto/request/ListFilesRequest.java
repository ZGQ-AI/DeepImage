package org.tech.ai.deepimage.model.dto.request;

import lombok.Data;
import org.tech.ai.deepimage.constant.FileConstant;

/**
 * 文件列表查询请求（统一接口）
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class ListFilesRequest extends PageRequest {
    
    /**
     * 业务类型（可选）
     * 可选值：AVATAR, DOCUMENT, IMAGE, VIDEO, TEMP
     */
    private String businessType;
    
    /**
     * 标签ID（可选）
     * 按标签筛选文件
     */
    private Long tagId;
    
    /**
     * 文件名搜索关键词（可选）
     * 支持模糊搜索
     */
    private String filename;
    
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

