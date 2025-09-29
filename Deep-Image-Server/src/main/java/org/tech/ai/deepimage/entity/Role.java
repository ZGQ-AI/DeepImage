package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 角色信息表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("roles")
public class Role {

    /**
     * 角色唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色显示名称，全局唯一
     */
    @TableField("role_name")
    private String roleName;

    /**
     * 角色代码，用于程序逻辑，全局唯一
     */
    @TableField("role_code")
    private String roleCode;

    /**
     * 角色描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 角色创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 角色信息最后更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 创建者用户ID
     */
    @TableField("created_by")
    private Long createdBy;

    /**
     * 最后更新者用户ID
     */
    @TableField("updated_by")
    private Long updatedBy;
}
