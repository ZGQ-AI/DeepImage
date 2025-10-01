package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * 分享权限级别枚举
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Getter
public enum PermissionLevelEnum {
    
    VIEW("仅查看"),
    DOWNLOAD("可下载"),
    EDIT("可编辑");
    
    private final String description;
    
    PermissionLevelEnum(String description) {
        this.description = description;
    }
}

