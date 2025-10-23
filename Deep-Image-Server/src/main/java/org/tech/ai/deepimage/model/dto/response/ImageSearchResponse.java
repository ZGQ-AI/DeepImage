package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.tech.ai.deepimage.model.dto.ImageInfo;

import java.util.List;

/**
 * 图片搜索响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageSearchResponse {

    /**
     * 搜索关键词
     */
    private String keyword;

    /**
     * 搜索到的图片列表
     */
    private List<ImageInfo> images;

    /**
     * 实际搜索到的图片数量
     */
    private Integer totalCount;

    /**
     * 搜索耗时（毫秒）
     */
    private Long searchTimeMs;
}
