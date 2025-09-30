package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_users")
public class User {

    /**
     * 用户唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名，用于登录，全局唯一
     */
    @TableField("username")
    private String username;

    /**
     * 邮箱地址，用于登录和通知，全局唯一
     */
    @TableField("email")
    private String email;

    /**
     * 密码哈希值，由应用层加密
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * 电话号码
     */
    @TableField("phone")
    private String phone;

    /**
     * 头像图片URL地址
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 邮箱验证状态：false=未验证，true=已验证
     */
    @TableField("verified")
    private Boolean verified;

    /**
     * 账户创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 账户信息最后更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
