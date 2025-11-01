package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Permission information table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_permissions")
public class Permission {

    /**
     * Permission unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Permission display name, globally unique
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * Permission code, used for program logic, globally unique
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * Permission description information
     */
    @TableField("description")
    private String description;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * Permission creation time
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * Permission information last update time
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Creator user ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * Last updater user ID
     */
    @TableField("updated_by")
    private Long updatedBy;
}
