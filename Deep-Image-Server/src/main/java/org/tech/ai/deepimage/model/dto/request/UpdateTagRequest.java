package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新标签请求
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
public class UpdateTagRequest {
    
    /**
     * 标签ID
     */
    @NotNull(message = "标签ID不能为空")
    @Positive(message = "标签ID必须为正数")
    private Long tagId;
    
    /**
     * 标签名称（可选，不传表示不更新）
     */
    @Size(max = 50, message = "标签名称不能超过50个字符")
    private String tagName;
    
    /**
     * 标签颜色（可选，不传表示不更新）
     * 十六进制格式，如：#FF5733
     */
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "颜色格式不正确，请使用#RRGGBB格式")
    private String color;
}

