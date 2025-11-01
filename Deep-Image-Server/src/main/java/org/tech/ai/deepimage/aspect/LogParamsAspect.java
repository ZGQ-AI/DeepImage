package org.tech.ai.deepimage.aspect;

import cn.dev33.satoken.stp.StpUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.tech.ai.deepimage.annotation.LogParams;

import java.lang.reflect.Method;

/**
 * Parameter logging aspect
 * Automatically logs input parameters and execution information for all methods in classes marked with @LogParams annotation
 *
 * @author zgq
 * @since 2025-10-22
 */
@Slf4j
@Aspect
@Component
public class LogParamsAspect {

    /**
     * Around advice: intercept all public methods in classes marked with @LogParams annotation
     */
    @Around("@within(logParams) && execution(public * *(..))")
    public Object logParams(ProceedingJoinPoint joinPoint, LogParams logParams) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        // Get method parameters
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        // Build parameter log
        StringBuilder paramsLog = new StringBuilder();
        paramsLog.append("User: ");
        try {
            paramsLog.append(StpUtil.getLoginId());
        } catch (Exception e) {
            paramsLog.append("Not logged in");
        }

        if (args != null && args.length > 0) {
            paramsLog.append(", Parameters: {");
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof MultipartFile) {
                    continue;
                }
                if (i > 0) {
                    paramsLog.append(", ");
                }
                paramsLog.append(paramNames[i]).append("=");
                // Use JSON serialization for parameters (more readable)
                try {
                    paramsLog.append(JSON.toJSONString(arg));
                } catch (Exception e) {
                    paramsLog.append(arg);
                }
            }
            paramsLog.append("}");
        }

        log.info(">>> [{}.{}] {}", className, methodName, paramsLog);

        // Execute target method
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // Build result log
            StringBuilder resultLog = new StringBuilder();

            if (logParams.printExecutionTime()) {
                resultLog.append("Elapsed time: ").append(executionTime).append("ms");
            }

            if (logParams.printResult() && result != null) {
                if (!resultLog.isEmpty()) {
                    resultLog.append(", ");
                }
                resultLog.append("Return: ");
                try {
                    String resultStr = JSON.toJSONString(result);
                    // If return value is too long, truncate
                    if (resultStr.length() > 500) {
                        resultLog.append(resultStr, 0, 500).append("...(truncated)");
                    } else {
                        resultLog.append(resultStr);
                    }
                } catch (Exception e) {
                    resultLog.append(result);
                }
            }

            if (!resultLog.isEmpty()) {
                log.info("<<< [{}.{}] {}", className, methodName, resultLog);
            }
        }
    }
}

