package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User-role association table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_user_roles")
public class UserRole {

    /**
     * Association record unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * User ID, references sys_users table
     */
    @TableField("user_id")
    private Long userId;

    /**
     * Role ID, references sys_roles table
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * Association relationship creation time
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * Association relationship last update time
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
