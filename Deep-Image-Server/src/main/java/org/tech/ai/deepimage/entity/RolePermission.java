package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 角色权限关联表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_role_permissions")
public class RolePermission {

    /**
     * 关联记录唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 角色ID，引用sys_roles表
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 权限ID，引用sys_permissions表
     */
    @TableField("permission_id")
    private Long permissionId;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 关联关系创建时间
     */
    @TableField(value = "created_at")
    private OffsetDateTime createdAt;

    /**
     * 关联关系最后更新时间
     */
    @TableField(value = "updated_at")
    private OffsetDateTime updatedAt;
}
