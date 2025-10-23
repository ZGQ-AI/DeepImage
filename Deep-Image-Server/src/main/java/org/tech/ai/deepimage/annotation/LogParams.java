package org.tech.ai.deepimage.annotation;

import java.lang.annotation.*;

/**
 * 方法参数日志注解
 * 标记在类上，自动打印该类所有公共方法的入参和返回值
 * 
 * @author zgq
 * @since 2025-10-22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogParams {
    
    /**
     * 是否打印返回值
     */
    boolean printResult() default false;
    
    /**
     * 是否打印执行时间
     */
    boolean printExecutionTime() default true;
}

