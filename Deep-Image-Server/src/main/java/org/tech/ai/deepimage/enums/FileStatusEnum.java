package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * 文件状态枚举
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Getter
public enum FileStatusEnum {
    
    UPLOADING("上传中"),
    COMPLETED("已完成"),
    FAILED("失败"),
    DELETED("已删除");
    
    private final String description;
    
    FileStatusEnum(String description) {
        this.description = description;
    }
}

