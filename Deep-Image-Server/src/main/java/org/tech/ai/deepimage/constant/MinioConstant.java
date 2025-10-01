package org.tech.ai.deepimage.constant;

/**
 * MinIO相关常量
 * 
 * @author zgq
 * @since 2025-10-02
 */
public class MinioConstant {

    /**
     * 默认预签名URL过期时间(秒) - 1小时
     */
    public static final int DEFAULT_PRESIGNED_EXPIRY = 3600;

    /**
     * 最大预签名URL过期时间(秒) - 7天
     */
    public static final int MAX_PRESIGNED_EXPIRY = 604800;

    /**
     * 最小预签名URL过期时间(秒) - 1分钟
     */
    public static final int MIN_PRESIGNED_EXPIRY = 60;

    private MinioConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

