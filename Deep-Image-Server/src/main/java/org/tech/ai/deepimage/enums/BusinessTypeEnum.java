package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * File business type enum
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Getter
public enum BusinessTypeEnum {
    
    AVATAR("头像"),
    DOCUMENT("文档"),
    IMAGE("图片"),
    VIDEO("视频"),
    TEMP("临时文件");
    
    private final String description;
    
    BusinessTypeEnum(String description) {
        this.description = description;
    }
    
    /**
     * Get enum by name (case-insensitive)
     * 
     * @param name Enum name
     * @return Enum value
     */
    public static BusinessTypeEnum fromName(String name) {
        try {
            return BusinessTypeEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown business type: " + name);
        }
    }
    
    /**
     * Validate if business type is valid
     * 
     * @param name Enum name
     * @return Whether valid
     */
    public static boolean isValid(String name) {
        try {
            BusinessTypeEnum.valueOf(name.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}

