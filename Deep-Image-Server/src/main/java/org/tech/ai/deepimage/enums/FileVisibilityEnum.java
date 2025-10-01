package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * 文件访问权限枚举
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Getter
public enum FileVisibilityEnum {
    
    PRIVATE("私有"),
    PUBLIC("公开"),
    SHARED("分享");
    
    private final String description;
    
    FileVisibilityEnum(String description) {
        this.description = description;
    }
}

