package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Role-permission association table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_role_permissions")
public class RolePermission {

    /**
     * Association record unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Role ID, references sys_roles table
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * Permission ID, references sys_permissions table
     */
    @TableField("permission_id")
    private Long permissionId;

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
