package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Update tag request
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class UpdateTagRequest {
    
    /**
     * Tag ID
     */
    @NotNull(message = "标签ID不能为空")
    @Positive(message = "标签ID必须为正数")
    private Long tagId;
    
    /**
     * Tag name (optional, omit to keep unchanged)
     */
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String tagName;
    
    /**
     * Tag color (optional, omit to keep unchanged)
     * Hexadecimal format, e.g.: #FF5733
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，请使用#RRGGBB格式")
    private String color;
}

