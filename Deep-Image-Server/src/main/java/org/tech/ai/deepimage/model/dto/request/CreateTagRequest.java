package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Create tag request
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class CreateTagRequest {
    
    /**
     * Tag name
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String tagName;
    
    /**
     * Tag color (hexadecimal format, e.g.: #FF5733)
     * Optional field
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，请使用#RRGGBB格式")
    private String color;
}

