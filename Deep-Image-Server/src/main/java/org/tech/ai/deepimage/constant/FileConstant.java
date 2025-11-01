package org.tech.ai.deepimage.constant;

/**
 * File-related constants
 * 
 * @author zgq
 * @since 2025-10-02
 */
public class FileConstant {
    
    private FileConstant() {
        // Private constructor to prevent instantiation
    }
    
    /**
     * File size limit (10MB)
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * Image file size limit (5MB)
     */
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    
    /**
     * Batch upload file count limit
     */
    public static final int MAX_BATCH_UPLOAD_COUNT = 10;
    
    /**
     * Batch delete file count limit
     */
    public static final int MAX_BATCH_DELETE_COUNT = 100;
    
    /**
     * File tag count limit
     */
    public static final int MAX_FILE_TAGS = 10;
    
    /**
     * Temporary URL default expiration time (seconds)
     */
    public static final int DEFAULT_PREVIEW_EXPIRY_SECONDS = 3600;
    
    /**
     * Thumbnail width
     */
    public static final int THUMBNAIL_WIDTH = 200;
    
    /**
     * Thumbnail height
     */
    public static final int THUMBNAIL_HEIGHT = 200;
    
    /**
     * File hash length (SHA-256)
     */
    public static final int FILE_HASH_LENGTH = 64;
    
    // ========== File sort field constants ==========
    
    /**
     * Sort field: creation time
     */
    public static final String SORT_BY_CREATED_AT = "createdAt";
    
    /**
     * Sort field: update time
     */
    public static final String SORT_BY_UPDATED_AT = "updatedAt";
    
    /**
     * Sort field: file size
     */
    public static final String SORT_BY_FILE_SIZE = "fileSize";
    
    /**
     * Sort field: filename
     */
    public static final String SORT_BY_FILENAME = "originalFilename";
    
    /**
     * Sort order: ascending
     */
    public static final String SORT_ORDER_ASC = "asc";
    
    /**
     * Sort order: descending
     */
    public static final String SORT_ORDER_DESC = "desc";
}
