package org.tech.ai.deepimage.model.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Recycle bin query request
 */
@Data
public class RecycleBinQueryRequest {

    /**
     * Page number (starts from 1)
     */
    @Min(value = 1, message = "页码必须大于0")
    private Integer page = 1;

    /**
     * Page size
     */
    @Min(value = 1, message = "每页数量必须大于0")
    private Integer size = 10;
}

