package org.tech.ai.deepimage.enums;

/**
 * Refresh Token status enum
 * 
 * @author zgq
 * @since 2025-10-01
 */
public enum RefreshTokenStatusEnum {
    NOT_REVOKED(0),
    REVOKED(1);

    private final int value;

    RefreshTokenStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}

