package org.tech.ai.deepimage.metadata;

import com.alibaba.fastjson2.JSON;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Image metadata implementation
 * Stores metadata information extracted from image files
 * 
 * @author zgq
 * @since 2025-11-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageMetaData implements FileMetaData {
    
    /**
     * Image width in pixels
     */
    private Integer width;
    
    /**
     * Image height in pixels
     */
    private Integer height;
    
    /**
     * Image format (e.g., JPEG, PNG, GIF, BMP, WEBP)
     */
    private String format;
    
    /**
     * Color space (e.g., sRGB, Adobe RGB)
     */
    private String colorSpace;
    
    /**
     * Image orientation (if available from EXIF)
     */
    private Integer orientation;
    
    /**
     * Whether image has alpha channel (transparency)
     */
    private Boolean hasAlpha;
    
    /**
     * Bit depth (if available)
     */
    private Integer bitDepth;
    
    @Override
    public String toJson() {
        try {
            return JSON.toJSONString(this);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public FileMetaData fromJson(String json) {
        try {
            return JSON.parseObject(json, ImageMetaData.class);
        } catch (Exception e) {
            return null;
        }
    }
}

