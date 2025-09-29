package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * 权限信息表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_permissions")
public class Permission {

    /**
     * 权限唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 权限显示名称，全局唯一
     */
    @TableField("permission_name")
    private String permissionName;

    /**
     * 权限代码，用于程序逻辑，全局唯一
     */
    @TableField("permission_code")
    private String permissionCode;

    /**
     * 权限描述信息
     */
    @TableField("description")
    private String description;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 权限创建时间
     */
    @TableField(value = "created_at")
    private OffsetDateTime createdAt;

    /**
     * 权限信息最后更新时间
     */
    @TableField(value = "updated_at")
    private OffsetDateTime updatedAt;

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
