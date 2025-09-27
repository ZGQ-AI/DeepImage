package org.tech.ai.deepimage.exception;

import org.tech.ai.deepimage.constant.ResponseConstant;

import lombok.Data;

@Data
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException of(String message) {
        return new BusinessException(ResponseConstant.SYSTEM_ERROR, message);
    }

    public static BusinessException of() {
        return new BusinessException(ResponseConstant.SYSTEM_ERROR, ResponseConstant.SYSTEM_ERROR_MESSAGE);
    }

    public static void throwIf(boolean condition, int code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }

    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(ResponseConstant.SYSTEM_ERROR, message);
        }
    }

}
