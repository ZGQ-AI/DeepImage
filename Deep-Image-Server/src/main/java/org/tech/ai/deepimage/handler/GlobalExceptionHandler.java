package org.tech.ai.deepimage.handler;

import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.dto.response.ApiResponse;
import org.tech.ai.deepimage.exception.BusinessException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException e) {
        return ApiResponse.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class, HttpMessageNotReadableException.class})
    public ApiResponse<Void> handleParam(Exception e) {
        return ApiResponse.error(ResponseConstant.PARAM_ERROR, ResponseConstant.PARAM_INVALID_MESSAGE);
    }

    @ExceptionHandler(NotLoginException.class)
    public ApiResponse<Void> handleNotLogin(NotLoginException e) {
        return ApiResponse.error(ResponseConstant.UNAUTHORIZED, ResponseConstant.NOT_LOGIN_MESSAGE);
    }

    @ExceptionHandler({NotRoleException.class, NotPermissionException.class})
    public ApiResponse<Void> handleAuth(Exception e) {
        return ApiResponse.error(ResponseConstant.FORBIDDEN, ResponseConstant.FORBIDDEN_MESSAGE_DETAIL);
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleOther(Exception e) {
        return ApiResponse.error(ResponseConstant.SYSTEM_ERROR, e.getMessage());
    }
}
