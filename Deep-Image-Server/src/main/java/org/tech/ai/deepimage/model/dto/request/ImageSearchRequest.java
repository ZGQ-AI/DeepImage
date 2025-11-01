package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Image search request DTO (search only, no download)
 */
@Data
public class ImageSearchRequest {

    /**
     * Search keyword
     */
    @NotBlank(message = "搜索关键词不能为空")
    private String keyword;

    /**
     * Number of images to search
     */
    @Min(value = 1, message = "图片数量不能少于1张")
    @Max(value = 30, message = "图片数量不能超过30张")
    private Integer count = 20;
}
