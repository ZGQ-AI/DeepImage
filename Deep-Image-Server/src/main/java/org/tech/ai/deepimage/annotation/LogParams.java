package org.tech.ai.deepimage.annotation;

import java.lang.annotation.*;

/**
 * Method parameter logging annotation
 * Mark on class to automatically log input parameters and return values of all public methods in this class
 * 
 * @author zgq
 * @since 2025-10-22
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LogParams {
    
    /**
     * Whether to print return value
     */
    boolean printResult() default false;
    
    /**
     * Whether to print execution time
     */
    boolean printExecutionTime() default true;
}

