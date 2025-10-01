package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件记录表实体
 * 存储上传到MinIO的文件元数据信息
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_records")
public class FileRecord {

    /**
     * 文件记录唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件所属用户ID，引用sys_users表，用于用户隔离
     */
    @TableField("user_id")
    private Long userId;

    /**
     * MinIO存储桶名称
     */
    @TableField("bucket_name")
    private String bucketName;

    /**
     * MinIO对象名称(完整存储路径)，全局唯一
     * 格式: {user_id}/{business_type}/{date}/{uuid}.ext
     * 如: 1001/avatar/20251002/abc.jpg
     */
    @TableField("object_name")
    private String objectName;

    /**
     * MinIO ETag，用于版本控制和文件校验
     */
    @TableField("etag")
    private String etag;

    /**
     * 原始文件名，用户上传时的文件名
     */
    @TableField("original_filename")
    private String originalFilename;

    /**
     * 文件大小(字节)
     */
    @TableField("file_size")
    private Long fileSize;

    /**
     * 文件MIME类型，如: image/jpeg, application/pdf
     */
    @TableField("content_type")
    private String contentType;

    /**
     * 文件扩展名，如: jpg, pdf, png
     */
    @TableField("file_extension")
    private String fileExtension;

    /**
     * 业务类型: avatar(头像), document(文档), image(图片), video(视频), temp(临时文件)等
     */
    @TableField("business_type")
    private String businessType;

    /**
     * 文件状态枚举: UPLOADING, COMPLETED, FAILED, DELETED
     */
    @TableField("status")
    private String status;

    /**
     * 访问权限枚举: PRIVATE, PUBLIC, SHARED
     */
    @TableField("visibility")
    private String visibility;

    /**
     * 文件访问URL
     */
    @TableField("file_url")
    private String fileUrl;

    /**
     * 缩略图URL(仅图片类型)
     */
    @TableField("thumbnail_url")
    private String thumbnailUrl;

    /**
     * 文件SHA256哈希值，用于去重、秒传和完整性校验
     */
    @TableField("file_hash")
    private String fileHash;

    /**
     * 扩展元数据(JSON格式)，可存储图片宽高、视频时长等信息
     */
    @TableField("metadata")
    private String metadata;

    /**
     * 引用计数，记录文件被引用次数，为0时可安全删除
     */
    @TableField("reference_count")
    private Integer referenceCount;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 文件上传时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 记录最后更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

