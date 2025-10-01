package org.tech.ai.deepimage.exception;

import org.tech.ai.deepimage.constant.ResponseConstant;

import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * 业务异常类
 * 提供优雅的异常抛出方式
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Getter
@EqualsAndHashCode(callSuper = false)
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }

    // ========== 基础工厂方法 ==========

    public static BusinessException of(int code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException of(String message) {
        return new BusinessException(ResponseConstant.SYSTEM_ERROR, message);
    }

    public static BusinessException of() {
        return new BusinessException(ResponseConstant.SYSTEM_ERROR, ResponseConstant.SYSTEM_ERROR_MESSAGE);
    }

    // ========== 条件抛出方法 ==========

    /**
     * 如果条件为true，则抛出异常
     */
    public static void throwIf(boolean condition, int code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 如果条件为true，则抛出异常（默认500错误码）
     */
    public static void throwIf(boolean condition, String message) {
        if (condition) {
            throw new BusinessException(ResponseConstant.SYSTEM_ERROR, message);
        }
    }

    // ========== 断言式方法 ==========

    /**
     * 断言对象不为null，否则抛出异常
     */
    public static <T> T assertNotNull(T object, int code, String message) {
        if (object == null) {
            throw new BusinessException(code, message);
        }
        return object;
    }

    /**
     * 断言对象不为null，否则抛出404异常
     */
    public static <T> T assertNotNull(T object, String message) {
        return assertNotNull(object, ResponseConstant.NOT_FOUND, message);
    }

    /**
     * 断言条件为true，否则抛出异常
     */
    public static void assertTrue(boolean condition, int code, String message) {
        if (!condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 断言条件为true，否则抛出异常（默认400错误码）
     */
    public static void assertTrue(boolean condition, String message) {
        assertTrue(condition, ResponseConstant.PARAM_ERROR, message);
    }

    /**
     * 断言条件为false，否则抛出异常
     */
    public static void assertFalse(boolean condition, int code, String message) {
        if (condition) {
            throw new BusinessException(code, message);
        }
    }

    /**
     * 断言条件为false，否则抛出异常（默认400错误码）
     */
    public static void assertFalse(boolean condition, String message) {
        assertFalse(condition, ResponseConstant.PARAM_ERROR, message);
    }

    // ========== 常见HTTP状态码快捷方法 ==========

    /**
     * 抛出400错误（参数错误）
     */
    public static BusinessException badRequest(String message) {
        return new BusinessException(ResponseConstant.PARAM_ERROR, message);
    }

    /**
     * 抛出400错误（格式化消息）
     */
    public static BusinessException badRequest(String format, Object... args) {
        return new BusinessException(ResponseConstant.PARAM_ERROR, String.format(format, args));
    }

    /**
     * 抛出401错误（未授权）
     */
    public static BusinessException unauthorized(String message) {
        return new BusinessException(ResponseConstant.UNAUTHORIZED, message);
    }

    /**
     * 抛出401错误（默认消息）
     */
    public static BusinessException unauthorized() {
        return new BusinessException(ResponseConstant.UNAUTHORIZED, ResponseConstant.UNAUTHORIZED_MESSAGE);
    }

    /**
     * 抛出403错误（无权限）
     */
    public static BusinessException forbidden(String message) {
        return new BusinessException(ResponseConstant.FORBIDDEN, message);
    }

    /**
     * 抛出403错误（默认消息）
     */
    public static BusinessException forbidden() {
        return new BusinessException(ResponseConstant.FORBIDDEN, ResponseConstant.FORBIDDEN_MESSAGE);
    }

    /**
     * 抛出404错误（资源不存在）
     */
    public static BusinessException notFound(String message) {
        return new BusinessException(ResponseConstant.NOT_FOUND, message);
    }

    /**
     * 抛出404错误（格式化消息）
     */
    public static BusinessException notFound(String format, Object... args) {
        return new BusinessException(ResponseConstant.NOT_FOUND, String.format(format, args));
    }

    /**
     * 抛出500错误（系统错误）
     */
    public static BusinessException serverError(String message) {
        return new BusinessException(ResponseConstant.SYSTEM_ERROR, message);
    }

    /**
     * 抛出500错误（默认消息）
     */
    public static BusinessException serverError() {
        return new BusinessException(ResponseConstant.SYSTEM_ERROR, ResponseConstant.SYSTEM_ERROR_MESSAGE);
    }

}
