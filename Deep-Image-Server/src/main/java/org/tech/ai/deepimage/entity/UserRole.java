package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 用户角色关联表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("user_roles")
public class UserRole {

    /**
     * 关联记录唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，引用users表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 角色ID，引用roles表
     */
    @TableField("role_id")
    private Long roleId;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 关联关系创建时间
     */
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 关联关系最后更新时间
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
