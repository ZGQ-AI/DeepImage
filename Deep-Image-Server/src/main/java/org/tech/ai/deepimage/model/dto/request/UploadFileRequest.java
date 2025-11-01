package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * File upload request
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
public class UploadFileRequest {
    
    /**
     * File to upload (required)
     */
    @NotNull(message = "文件不能为空")
    private MultipartFile file;
    
    /**
     * Business type (required)
     * Optional values: AVATAR, DOCUMENT, IMAGE, VIDEO, TEMP
     */
    @NotBlank(message = "业务类型不能为空")
    private String businessType;
    
    /**
     * File description (optional)
     */
    @Size(max = 500, message = "文件描述不能超过500字符")
    private String description;
    
    /**
     * Tag ID list (optional)
     * Associate tags directly when uploading
     */
    private List<Long> tagIds;
}

