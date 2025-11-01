package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File access log table entity
 * Records file access behavior
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_access_logs")
public class FileAccessLog {

    /**
     * Log record unique identifier
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Accessed file ID
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * Access user ID, NULL for unauthenticated users
     */
    @TableField("user_id")
    private Long userId;

    /**
     * Access type enum: DOWNLOAD, PREVIEW, UPLOAD
     */
    @TableField("access_type")
    private String accessType;

    /**
     * Access IP address
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * User agent information
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * Share ID, if accessed through share
     */
    @TableField("share_id")
    private Long shareId;

    /**
     * Access time
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

