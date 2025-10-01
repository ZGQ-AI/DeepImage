package org.tech.ai.deepimage.enums;

public enum DeleteStatusEnum {
    NOT_DELETED(0),
    DELETED(1);

    private final int value;

    DeleteStatusEnum(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}


