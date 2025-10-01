package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * 文件访问类型枚举
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Getter
public enum AccessTypeEnum {
    
    DOWNLOAD("下载"),
    PREVIEW("预览"),
    UPLOAD("上传");
    
    private final String description;
    
    AccessTypeEnum(String description) {
        this.description = description;
    }
}

