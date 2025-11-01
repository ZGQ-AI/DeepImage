package org.tech.ai.deepimage.constant;

/**
 * MinIO related constants
 * 
 * @author zgq
 * @since 2025-10-02
 */
public class MinioConstant {

    /**
     * Default presigned URL expiration time (seconds) - 1 hour
     */
    public static final int DEFAULT_PRESIGNED_EXPIRY = 3600;

    /**
     * Maximum presigned URL expiration time (seconds) - 7 days
     */
    public static final int MAX_PRESIGNED_EXPIRY = 604800;

    /**
     * Minimum presigned URL expiration time (seconds) - 1 minute
     */
    public static final int MIN_PRESIGNED_EXPIRY = 60;

    private MinioConstant() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
