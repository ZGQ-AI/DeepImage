package org.tech.ai.deepimage.constant;

/**
 * 文件相关常量
 * 
 * @author zgq
 * @since 2025-10-02
 */
public class FileConstant {
    
    private FileConstant() {
        // 私有构造函数，防止实例化
    }
    
    /**
     * 文件大小限制（10MB）
     */
    public static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    
    /**
     * 图片文件大小限制（5MB）
     */
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;
    
    /**
     * 批量上传文件数量限制
     */
    public static final int MAX_BATCH_UPLOAD_COUNT = 10;
    
    /**
     * 批量删除文件数量限制
     */
    public static final int MAX_BATCH_DELETE_COUNT = 100;
    
    /**
     * 文件标签数量限制
     */
    public static final int MAX_FILE_TAGS = 10;
    
    /**
     * 临时URL默认有效期（秒）
     */
    public static final int DEFAULT_PREVIEW_EXPIRY_SECONDS = 3600;
    
    /**
     * 缩略图宽度
     */
    public static final int THUMBNAIL_WIDTH = 200;
    
    /**
     * 缩略图高度
     */
    public static final int THUMBNAIL_HEIGHT = 200;
    
    /**
     * 文件哈希长度（SHA-256）
     */
    public static final int FILE_HASH_LENGTH = 64;
    
    // ========== 文件排序字段常量 ==========
    
    /**
     * 排序字段：创建时间
     */
    public static final String SORT_BY_CREATED_AT = "createdAt";
    
    /**
     * 排序字段：文件大小
     */
    public static final String SORT_BY_FILE_SIZE = "fileSize";
    
    /**
     * 排序字段：文件名
     */
    public static final String SORT_BY_FILENAME = "originalFilename";
    
    /**
     * 排序方向：升序
     */
    public static final String SORT_ORDER_ASC = "asc";
    
    /**
     * 排序方向：降序
     */
    public static final String SORT_ORDER_DESC = "desc";
}

