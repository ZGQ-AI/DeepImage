package org.tech.ai.deepimage.util;

import org.apache.commons.lang3.StringUtils;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 条件工具类
 * 提供简洁的条件判断和赋值操作
 * 
 * @author zgq
 * @since 2025-10-03
 */
public class ConditionalUtil {

    /**
     * 如果字符串不为空，则执行设置操作
     * 
     * @param value 要检查的值
     * @param setter 设置方法（通常是对象的 setter）
     * 
     * @example
     * ConditionalUtil.setIfNotBlank(user.getName(), target::setUsername);
     */
    public static void setIfNotBlank(String value, Consumer<String> setter) {
        if (StringUtils.isNotBlank(value)) {
            setter.accept(value);
        }
    }

    /**
     * 如果字符串不为空，则执行设置操作，否则使用默认值
     * 
     * @param value 要检查的值
     * @param defaultValue 默认值
     * @param setter 设置方法
     * 
     * @example
     * ConditionalUtil.setIfNotBlankOrElse(user.getName(), "Anonymous", target::setUsername);
     */
    public static void setIfNotBlankOrElse(String value, String defaultValue, Consumer<String> setter) {
        setter.accept(StringUtils.isNotBlank(value) ? value : defaultValue);
    }

    /**
     * 如果字符串不为空，则执行设置操作，否则使用默认值提供者
     * 
     * @param value 要检查的值
     * @param defaultSupplier 默认值提供者
     * @param setter 设置方法
     * 
     * @example
     * ConditionalUtil.setIfNotBlankOrElse(
     *     user.getName(), 
     *     () -> user.getEmail().split("@")[0], 
     *     target::setUsername
     * );
     */
    public static void setIfNotBlankOrElse(String value, Supplier<String> defaultSupplier, Consumer<String> setter) {
        setter.accept(StringUtils.isNotBlank(value) ? value : defaultSupplier.get());
    }

    /**
     * 如果对象不为 null，则执行设置操作
     * 
     * @param value 要检查的值
     * @param setter 设置方法
     * @param <T> 值的类型
     * 
     * @example
     * ConditionalUtil.setIfNotNull(user.getAge(), target::setAge);
     */
    public static <T> void setIfNotNull(T value, Consumer<T> setter) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     * 如果对象不为 null，则执行设置操作，否则使用默认值
     * 
     * @param value 要检查的值
     * @param defaultValue 默认值
     * @param setter 设置方法
     * @param <T> 值的类型
     * 
     * @example
     * ConditionalUtil.setIfNotNullOrElse(user.getAge(), 18, target::setAge);
     */
    public static <T> void setIfNotNullOrElse(T value, T defaultValue, Consumer<T> setter) {
        setter.accept(value != null ? value : defaultValue);
    }

    /**
     * 如果对象不为 null，则执行设置操作，否则使用默认值提供者
     * 
     * @param value 要检查的值
     * @param defaultSupplier 默认值提供者
     * @param setter 设置方法
     * @param <T> 值的类型
     * 
     * @example
     * ConditionalUtil.setIfNotNullOrElse(
     *     user.getAge(), 
     *     () -> calculateDefaultAge(), 
     *     target::setAge
     * );
     */
    public static <T> void setIfNotNullOrElse(T value, Supplier<T> defaultSupplier, Consumer<T> setter) {
        setter.accept(value != null ? value : defaultSupplier.get());
    }

    /**
     * 如果条件为真，则执行设置操作
     * 
     * @param condition 条件
     * @param value 要设置的值
     * @param setter 设置方法
     * @param <T> 值的类型
     * 
     * @example
     * ConditionalUtil.setIf(user.isVip(), "VIP", target::setLevel);
     */
    public static <T> void setIf(boolean condition, T value, Consumer<T> setter) {
        if (condition) {
            setter.accept(value);
        }
    }

    /**
     * 根据条件设置不同的值
     * 
     * @param condition 条件
     * @param trueValue 条件为真时的值
     * @param falseValue 条件为假时的值
     * @param setter 设置方法
     * @param <T> 值的类型
     * 
     * @example
     * ConditionalUtil.setByCondition(user.isVip(), "VIP", "Normal", target::setLevel);
     */
    public static <T> void setByCondition(boolean condition, T trueValue, T falseValue, Consumer<T> setter) {
        setter.accept(condition ? trueValue : falseValue);
    }

    /**
     * 如果对象不为 null，则执行操作（不是赋值，而是执行一段逻辑）
     * 
     * @param value 要检查的值
     * @param action 要执行的操作
     * @param <T> 值的类型
     * 
     * @example
     * ConditionalUtil.ifNotNull(session, s -> {
     *     s.setActive(false);
     *     sessionService.updateById(s);
     * });
     */
    public static <T> void ifNotNull(T value, Consumer<T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    /**
     * 如果字符串不为空，则执行操作
     * 
     * @param value 要检查的值
     * @param action 要执行的操作
     * 
     * @example
     * ConditionalUtil.ifNotBlank(email, e -> sendEmail(e));
     */
    public static void ifNotBlank(String value, Consumer<String> action) {
        if (StringUtils.isNotBlank(value)) {
            action.accept(value);
        }
    }

    /**
     * 如果条件为真，则执行操作
     * 
     * @param condition 条件
     * @param action 要执行的操作
     * 
     * @example
     * ConditionalUtil.ifTrue(user.isVip(), () -> sendVipEmail(user));
     */
    public static void ifTrue(boolean condition, Runnable action) {
        if (condition) {
            action.run();
        }
    }

    /**
     * 链式条件设置构建器
     * 支持多个条件的链式调用
     * 
     * @param <T> 目标对象类型
     * 
     * @example
     * ConditionalUtil.builder(user)
     *     .setIfNotBlank(request.getName(), User::setUsername)
     *     .setIfNotBlank(request.getPicture(), User::setAvatarUrl)
     *     .setIfNotNull(request.getAge(), User::setAge)
     *     .build();
     */
    public static <T> ConditionalBuilder<T> builder(T target) {
        return new ConditionalBuilder<>(target);
    }

    /**
     * 条件设置构建器
     */
    public static class ConditionalBuilder<T> {
        private final T target;

        private ConditionalBuilder(T target) {
            this.target = target;
        }

        /**
         * 如果字符串不为空，则设置
         */
        public ConditionalBuilder<T> setIfNotBlank(String value, Consumer<String> setter) {
            ConditionalUtil.setIfNotBlank(value, setter);
            return this;
        }

        /**
         * 如果字符串不为空，则设置，否则使用默认值
         */
        public ConditionalBuilder<T> setIfNotBlankOrElse(String value, String defaultValue, Consumer<String> setter) {
            ConditionalUtil.setIfNotBlankOrElse(value, defaultValue, setter);
            return this;
        }

        /**
         * 如果字符串不为空，则设置，否则使用默认值提供者
         */
        public ConditionalBuilder<T> setIfNotBlankOrElse(String value, Supplier<String> defaultSupplier, Consumer<String> setter) {
            ConditionalUtil.setIfNotBlankOrElse(value, defaultSupplier, setter);
            return this;
        }

        /**
         * 如果对象不为 null，则设置
         */
        public <V> ConditionalBuilder<T> setIfNotNull(V value, Consumer<V> setter) {
            ConditionalUtil.setIfNotNull(value, setter);
            return this;
        }

        /**
         * 如果对象不为 null，则设置，否则使用默认值
         */
        public <V> ConditionalBuilder<T> setIfNotNullOrElse(V value, V defaultValue, Consumer<V> setter) {
            ConditionalUtil.setIfNotNullOrElse(value, defaultValue, setter);
            return this;
        }

        /**
         * 如果条件为真，则设置
         */
        public <V> ConditionalBuilder<T> setIf(boolean condition, V value, Consumer<V> setter) {
            ConditionalUtil.setIf(condition, value, setter);
            return this;
        }

        /**
         * 完成构建，返回目标对象
         */
        public T build() {
            return target;
        }
    }
}

