package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件上传请求
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class UploadFileRequest {
    
    /**
     * 上传的文件（必填）
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    /**
     * 业务类型（必填）
     * 可选值：AVATAR, DOCUMENT, IMAGE, VIDEO, TEMP
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    
    /**
     * 文件描述（可选）
     */
    @Size(max = 500, message = "文件描述不能超过500字符")
    private String description;
    
    /**
     * 标签ID列表（可选）
     * 上传时直接关联标签
     */
    private List<Long> tagIds;
}

