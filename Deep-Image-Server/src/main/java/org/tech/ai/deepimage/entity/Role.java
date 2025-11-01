package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * Role information table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_roles")
public class Role {

    /**
     * Role unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Role display name, globally unique
     */
    @TableField("role_name")
    private String roleName;

    /**
     * Role code, used for program logic, globally unique
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * Role description information
     */
    @TableField("description")
    private String description;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * Role creation time
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * Role information last update time
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
