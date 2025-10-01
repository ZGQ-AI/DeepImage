package org.tech.ai.deepimage.enums;

/**
 * 会话状态枚举
 * 
 * @author zgq
 * @since 2025-10-01
 */
public enum SessionStatusEnum {
    INACTIVE(0),
    ACTIVE(1);

    private final int value;

    SessionStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

