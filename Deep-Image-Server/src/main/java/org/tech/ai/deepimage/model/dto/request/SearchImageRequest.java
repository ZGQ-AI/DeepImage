package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图片搜索请求 DTO
 */
@Data
public class SearchImageRequest {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 需要抓取的图片数量
     */
    @Min(value = 1, message = "图片数量不能少于1张")
    @Max(value = 30, message = "图片数量不能超过30张")
    private Integer count = 10;

    /**
     * 可选：为抓取的图片添加标签ID列表
     */
    private java.util.List<Long> tagIds;
}
