package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.tech.ai.deepimage.model.dto.ImageInfo;

import java.util.List;

/**
 * Image download request DTO
 */
@Data
public class ImageDownloadRequest {

    /**
     * Selected images to download
     */
    @NotEmpty(message = "请选择要下载的图片")
    @Valid
    private List<ImageInfo> selectedImages;

    /**
     * Search keyword (for recording source)
     */
    private String keyword;

    /**
     * Tag ID list (optional)
     */
    private List<Long> tagIds;
}
