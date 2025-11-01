package org.tech.ai.deepimage.util;

import org.tech.ai.deepimage.constant.RedisKeyConstant;

import jakarta.validation.constraints.NotNull;

/**
 * Factory class for generating Redis keys
 * Provides static methods to construct Redis keys from constants
 * 
 * @author zgq
 * @since 2025-11-01
 */
public class RedisKeyFactory {
    
    /**
     * Generate user file list cache key
     * 
     * @param userId User ID
     * @param businessType Business type (e.g., IMAGE, DOCUMENT, VIDEO, AVATAR, TEMP), null means all types
     * @param requestHash Hash of request parameters
     * @return Complete cache key string
     */
    public static String getUserFileListKey(@NotNull Long userId, @NotNull String businessType, @NotNull String requestHash) {
        String businessTypeStr = normalizeBusinessType(businessType);
        return String.format(RedisKeyConstant.USER_FILE_LIST_KEY_FORMAT, businessTypeStr, userId, requestHash);
    }
    
    /**
     * Generate user file list cache key pattern for invalidation by business type
     * 
     * @param userId User ID
     * @param businessType Business type (normalized to lowercase)
     * @return Cache key pattern string
     */
    public static String getUserFileListPattern(@NotNull Long userId, @NotNull String businessType) {
        String businessTypeStr = normalizeBusinessType(businessType);
        return String.format(RedisKeyConstant.USER_FILE_LIST_PATTERN_FORMAT, businessTypeStr, userId);
    }
    
    /**
     * Generate user file list cache key pattern for invalidation (all business types)
     * 
     * @param userId User ID
     * @return Cache key pattern string
     */
    public static String getUserFileListPatternAllTypes(@NotNull Long userId) {
        return String.format(RedisKeyConstant.USER_FILE_LIST_PATTERN_ALL_TYPES_FORMAT, userId);
    }
    
    /**
     * Generate public file list cache key
     * 
     * @param requestHash Hash of request parameters
     * @return Complete cache key string
     */
    public static String getPublicFileListKey(@NotNull String requestHash) {
        return String.format(RedisKeyConstant.PUBLIC_FILE_LIST_KEY_FORMAT, requestHash);
    }
    
    /**
     * Generate public file list cache key pattern for invalidation
     * 
     * @return Cache key pattern string
     */
    public static String getPublicFileListPattern() {
        return RedisKeyConstant.PUBLIC_FILE_LIST_PATTERN_FORMAT;
    }
    
    /**
     * Normalize business type for key generation
     * Convert to lowercase for consistent key format
     * 
     * @param businessType Business type string (must not be null)
     * @return Lowercase business type string
     */
    private static String normalizeBusinessType(String businessType) {
        return businessType.toLowerCase();
    }
    
    /**
     * Private constructor to prevent instantiation
     */
    private RedisKeyFactory() {
        throw new UnsupportedOperationException("Factory class cannot be instantiated");
    }
}

