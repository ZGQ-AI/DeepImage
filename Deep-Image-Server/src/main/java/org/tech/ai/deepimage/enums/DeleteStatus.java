package org.tech.ai.deepimage.enums;

public enum DeleteStatus {
    NOT_DELETED(0),
    DELETED(1);

    private final int value;

    DeleteStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}


