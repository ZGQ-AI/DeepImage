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
 * 参数日志切面
 * 自动打印被 @LogParams 注解标记的类的所有方法入参和执行信息
 *
 * @author zgq
 * @since 2025-10-22
 */
@Slf4j
@Aspect
@Component
public class LogParamsAspect {

    /**
     * 环绕通知：拦截被 @LogParams 注解标记的类的所有公共方法
     */
    @Around("@within(logParams) && execution(public * *(..))")
    public Object logParams(ProceedingJoinPoint joinPoint, LogParams logParams) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = method.getName();

        // 获取方法参数
        Object[] args = joinPoint.getArgs();
        String[] paramNames = signature.getParameterNames();

        // 构建参数日志
        StringBuilder paramsLog = new StringBuilder();
        paramsLog.append("用户：");
        try {
            paramsLog.append(StpUtil.getLoginId());
        } catch (Exception e) {
            paramsLog.append("未登录");
        }

        if (args != null && args.length > 0) {
            paramsLog.append("，参数：{");
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if (arg instanceof MultipartFile) {
                    continue;
                }
                if (i > 0) {
                    paramsLog.append(", ");
                }
                paramsLog.append(paramNames[i]).append("=");
                // 使用 JSON 序列化参数（更易读）
                try {
                    paramsLog.append(JSON.toJSONString(arg));
                } catch (Exception e) {
                    paramsLog.append(arg);
                }
            }
            paramsLog.append("}");
        }

        log.info(">>> [{}.{}] {}", className, methodName, paramsLog);

        // 执行目标方法
        long startTime = System.currentTimeMillis();
        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long executionTime = System.currentTimeMillis() - startTime;

            // 构建结果日志
            StringBuilder resultLog = new StringBuilder();

            if (logParams.printExecutionTime()) {
                resultLog.append("耗时：").append(executionTime).append("ms");
            }

            if (logParams.printResult() && result != null) {
                if (!resultLog.isEmpty()) {
                    resultLog.append("，");
                }
                resultLog.append("返回：");
                try {
                    String resultStr = JSON.toJSONString(result);
                    // 如果返回值太长，截断
                    if (resultStr.length() > 500) {
                        resultLog.append(resultStr, 0, 500).append("...(已截断)");
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

