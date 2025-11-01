package org.tech.ai.deepimage.constant;

/**
 * Redis key format constants
 * Defines complete key formats used in the application
 * 
 * @author zgq
 * @since 2025-11-01
 */
public class RedisKeyConstant {
    
    /**
     * Application namespace prefix
     */
    private static final String DEEPIMAGE = "deepimage";
    
    /**
     * User file list cache key base format
     * Format: deepimage:user:{businessType}:list
     * The {businessType} will be dynamically replaced based on request parameter
     * If businessType is null, use "file" as default (querying all types)
     */
    private static final String USER_FILE_LIST_BASE_FORMAT = DEEPIMAGE + ":user:%s:list";
    
    /**
     * User file list cache key format template
     * Format: deepimage:user:{businessType}:list:{userId}:{requestHash}
     * Example: deepimage:user:image:list:123:abc123def456
     */
    public static final String USER_FILE_LIST_KEY_FORMAT = USER_FILE_LIST_BASE_FORMAT + ":%d:%s";
    
    /**
     * User file list cache key pattern format for invalidation by business type
     * Format: deepimage:user:{businessType}:list:{userId}:*
     * Example: deepimage:user:image:list:123:*
     */
    public static final String USER_FILE_LIST_PATTERN_FORMAT = USER_FILE_LIST_BASE_FORMAT + ":%d:*";
    
    /**
     * User file list cache key pattern format for invalidation (all business types)
     * Format: deepimage:user:*:list:{userId}:*
     * Example: deepimage:user:*:list:123:*
     */
    public static final String USER_FILE_LIST_PATTERN_ALL_TYPES_FORMAT = DEEPIMAGE + ":user:*:list:%d:*";
    
    /**
     * Public file list cache key format template
     * Format: deepimage:public:file:list:{requestHash}
     * Example: deepimage:public:file:list:abc123def456
     */
    public static final String PUBLIC_FILE_LIST_KEY_FORMAT = DEEPIMAGE + ":public:file:list:%s";
    
    /**
     * Public file list cache key pattern format for invalidation
     * Format: deepimage:public:file:list:*
     * Used to invalidate all public file list cache entries
     */
    public static final String PUBLIC_FILE_LIST_PATTERN_FORMAT = DEEPIMAGE + ":public:file:list:*";
    
    
    /**
     * Private constructor to prevent instantiation
     */
    private RedisKeyConstant() {
        throw new UnsupportedOperationException("Constant class cannot be instantiated");
    }
}

