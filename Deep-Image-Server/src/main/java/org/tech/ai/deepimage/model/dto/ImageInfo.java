package org.tech.ai.deepimage.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 图片信息 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageInfo {

    /**
     * 图片URL（已清理参数）
     */
    private String url;

    /**
     * 图片标题/描述
     */
    private String title;

    /**
     * 图片宽度
     */
    private Integer width;

    /**
     * 图片高度
     */
    private Integer height;

    /**
     * 原始URL（未清理）
     */
    private String originalUrl;

    /**
     * 图片文件扩展名
     */
    private String extension;
}
