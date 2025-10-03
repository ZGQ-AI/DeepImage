package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * 文件业务类型枚举
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
     * 根据名称获取枚举（忽略大小写）
     * 
     * @param name 枚举名称
     * @return 枚举值
     */
    public static BusinessTypeEnum fromName(String name) {
        try {
            return BusinessTypeEnum.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("未知的业务类型: " + name);
        }
    }
    
    /**
     * 验证业务类型是否合法
     * 
     * @param name 枚举名称
     * @return 是否合法
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

