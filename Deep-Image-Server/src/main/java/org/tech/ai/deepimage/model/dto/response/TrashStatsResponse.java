package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * 回收站统计响应
 */
@Data
@Builder
public class TrashStatsResponse {

    /**
     * 回收站文件数量
     */
    private Long count;

    /**
     * 回收站总大小（字节）
     */
    private Long totalSize;
}

