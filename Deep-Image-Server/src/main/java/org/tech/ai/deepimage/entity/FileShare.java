package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File share table entity
 * Manages file sharing relationships between users
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_shares")
public class FileShare {

    /**
     * Share record unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Shared file ID, references di_file_records table
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * Sharer user ID (who shared), references sys_users table
     */
    @TableField("share_from_user_id")
    private Long shareFromUserId;

    /**
     * Recipient user ID (shared to whom), references sys_users table
     */
    @TableField("share_to_user_id")
    private Long shareToUserId;

    /**
     * Share type enum: PERMANENT, TEMPORARY
     */
    @TableField("share_type")
    private String shareType;

    /**
     * Expiry time, only needed for TEMPORARY type, NULL means permanently valid
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * Share permission level enum: VIEW, DOWNLOAD, EDIT
     */
    @TableField("permission_level")
    private String permissionLevel;

    /**
     * Revoked status: 0=valid, 1=revoked (soft delete, preserve share history)
     */
    @TableField("revoked")
    private Integer revoked;

    /**
     * Share message, message from sharer to recipient
     */
    @TableField("message")
    private String message;

    /**
     * View count statistics
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * Download count statistics
     */
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * Share creation time
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * Share information last update time
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

