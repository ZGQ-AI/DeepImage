package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Refresh token table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_refresh_tokens")
public class RefreshToken {

    /**
     * Refresh token unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User ID, references sys_users table
     */
    @TableField("user_id")
    private Long userId;

    /**
     * Refresh token hash value, not stored in plaintext, globally unique
     */
    @TableField("token_hash")
    private String tokenHash;

    /**
     * Associated session ID, references sys_sessions table
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * Refresh token expiry time
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * Last used time, used for activity statistics
     */
    @TableField("last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * Usage status: 0=not revoked, 1=revoked
     */
    @TableField("revoked")
    private Integer revoked;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * Token creation time
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * Token information last update time
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
