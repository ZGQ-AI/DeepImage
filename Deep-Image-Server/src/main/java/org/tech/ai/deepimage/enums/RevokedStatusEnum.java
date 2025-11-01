package org.tech.ai.deepimage.enums;

/**
 * Token status enum
 * 
 * @author zgq
 * @since 2025-10-01
 */
public enum RevokedStatusEnum {
    NOT_REVOKED(0),
    REVOKED(1);

    private final int value;

    RevokedStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

