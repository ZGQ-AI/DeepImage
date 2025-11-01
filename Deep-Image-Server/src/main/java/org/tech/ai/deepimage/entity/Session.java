package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User session table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_sessions")
public class Session {

    /**
     * Session unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User ID, references sys_users table
     */
    @TableField("user_id")
    private Long userId;

    /**
     * Access token hash value, not stored in plaintext
     */
    @TableField("access_token_hash")
    private String accessTokenHash;

    /**
     * Device information (device type, operating system, browser, etc.)
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * Login IP address
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * User agent string, used for device identification
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * Session active status: 0=revoked, 1=active
     */
    @TableField("active")
    private Integer active;

    /**
     * Last refresh time, used for cleaning up long-unused sessions
     */
    @TableField("last_refresh_at")
    private LocalDateTime lastRefreshAt;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * Session creation time
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * Session information last update time
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
