package org.tech.ai.deepimage.enums;

import lombok.Getter;

/**
 * Share type enum
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Getter
public enum ShareTypeEnum {
    
    PERMANENT("永久分享"),
    TEMPORARY("临时分享");
    
    private final String description;
    
    ShareTypeEnum(String description) {
        this.description = description;
    }
}

