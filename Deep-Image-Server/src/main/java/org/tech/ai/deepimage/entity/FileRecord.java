package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File record table entity
 * Stores metadata information of files uploaded to MinIO
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_records")
public class FileRecord {

    /**
     * File record unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * File owner user ID, references sys_users table, used for user isolation
     */
    @TableField("user_id")
    private Long userId;

    /**
     * MinIO bucket name
     */
    @TableField("bucket_name")
    private String bucketName;

    /**
     * MinIO object name (complete storage path), globally unique
     * Format: {user_id}/{business_type}/{date}/{uuid}.ext
     * Example: 1001/avatar/20251002/abc.jpg
     */
    @TableField("object_name")
    private String objectName;

    /**
     * MinIO ETag, used for version control and file verification
     */
    @TableField("etag")
    private String etag;

    /**
     * Original filename, filename when user uploaded
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * File size (bytes)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * File MIME type, e.g.: image/jpeg, application/pdf
     */
    @TableField("content_type")
    private String contentType;

    /**
     * File extension, e.g.: jpg, pdf, png
     */
    @TableField("file_extension")
    private String fileExtension;

    /**
     * Business type: avatar (avatar), document (document), image (image), video (video), temp (temporary file), etc.
     */
    @TableField("business_type")
    private String businessType;

    /**
     * File status enum: UPLOADING, COMPLETED, FAILED, DELETED
     */
    @TableField("status")
    private String status;

    /**
     * Access permission enum: PRIVATE, PUBLIC, SHARED
     */
    @TableField("visibility")
    private String visibility;

    /**
     * File access URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * Thumbnail URL (image type only)
     */
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * File SHA256 hash value, used for deduplication, instant upload and integrity verification
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * Extended metadata (JSON format), can store image width/height, video duration, etc.
     */
    @TableField("metadata")
    private String metadata;

    /**
     * Reference count, records how many times the file is referenced, can be safely deleted when 0
     */
    @TableField("reference_count")
    private Integer referenceCount;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * File upload time
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * Record last update time
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

