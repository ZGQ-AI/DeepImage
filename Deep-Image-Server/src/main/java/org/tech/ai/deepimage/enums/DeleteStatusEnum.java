package org.tech.ai.deepimage.enums;

import lombok.Getter;

@Getter
public enum DeleteStatusEnum {
    NOT_DELETED(0),
    DELETED(1);

    private final int value;

    DeleteStatusEnum(int value) {
        this.value = value;
    }

}


