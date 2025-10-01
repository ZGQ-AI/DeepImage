package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建标签请求
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class CreateTagRequest {
    
    /**
     * 标签名称
     */
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String tagName;
    
    /**
     * 标签颜色（十六进制格式，如：#FF5733）
     * 可选字段
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，请使用#RRGGBB格式")
    private String color;
}

