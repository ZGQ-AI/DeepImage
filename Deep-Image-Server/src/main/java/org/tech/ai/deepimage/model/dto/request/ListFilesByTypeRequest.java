package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 按业务类型查询文件请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class ListFilesByTypeRequest extends PageRequest {
    
    /**
     * 业务类型
     * 可选值：AVATAR, DOCUMENT, IMAGE, VIDEO, TEMP
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
}

