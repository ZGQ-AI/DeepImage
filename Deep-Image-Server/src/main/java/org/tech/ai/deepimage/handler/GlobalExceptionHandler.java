package org.tech.ai.deepimage.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.tech.ai.deepimage.dto.response.ApiResponse;
import org.tech.ai.deepimage.exception.BusinessException;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
//
   @ExceptionHandler(value = BusinessException.class)
   public ApiResponse<String> handleBusinessException(BusinessException e) {
       log.error("Business exception: ", e);
       return ApiResponse.error(e.getCode(), e.getMessage());
   }

   @ExceptionHandler(value = RuntimeException.class)
   public ApiResponse<String> handleRuntimeException(RuntimeException e) {
       log.error("Runtime exception: ", e);
       return ApiResponse.error("A runtime error occurred: " + e.getMessage());
   }

    @ExceptionHandler(value = Exception.class)
    public ApiResponse<String> handleGenericException(Exception e) {
        log.error("System exception: ", e);
        return ApiResponse.error("A system error occurred: " + e.getMessage());
    }


}
