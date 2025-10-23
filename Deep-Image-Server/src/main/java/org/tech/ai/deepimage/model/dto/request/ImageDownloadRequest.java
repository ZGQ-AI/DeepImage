package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.tech.ai.deepimage.model.dto.ImageInfo;

import java.util.List;

/**
 * 图片下载请求 DTO
 */
@Data
public class ImageDownloadRequest {

    /**
     * 选中要下载的图片列表
     */
    @NotEmpty(message = "请选择要下载的图片")
    @Valid
    private List<ImageInfo> selectedImages;

    /**
     * 搜索关键词（用于记录来源）
     */
    private String keyword;

    /**
     * 标签ID列表（可选）
     */
    private List<Long> tagIds;
}
