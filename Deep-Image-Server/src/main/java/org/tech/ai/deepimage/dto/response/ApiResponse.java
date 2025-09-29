package org.tech.ai.deepimage.dto.response;

import org.tech.ai.deepimage.constant.ResponseConstant;

import lombok.Data;

@Data
public class ApiResponse<T> {

    private final int code;
    private final String message;
    private final T data;

    public ApiResponse(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(ResponseConstant.SUCCESS, ResponseConstant.SUCCESS_MESSAGE, data);
    }

    public static <T> ApiResponse<T> error() {
        return new ApiResponse<>(ResponseConstant.SYSTEM_ERROR, ResponseConstant.SYSTEM_ERROR_MESSAGE, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(ResponseConstant.SYSTEM_ERROR, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null);
    }

    public static <T> ApiResponse<T> error(int code, String message, T data) {
        return new ApiResponse<>(code, message, data);
    }

    public static <T> ApiResponse<T> paramError(String message) {
        return new ApiResponse<>(ResponseConstant.PARAM_ERROR, message, null);
    }

}
