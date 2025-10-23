package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 图片搜索请求 DTO（仅搜索，不下载）
 */
@Data
public class ImageSearchRequest {

    /**
     * 搜索关键词
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * 需要搜索的图片数量
     */
    @Min(value = 1, message = "图片数量不能少于1张")
    @Max(value = 30, message = "图片数量不能超过30张")
    private Integer count = 20;
}
