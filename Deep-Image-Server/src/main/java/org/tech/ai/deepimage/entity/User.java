package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * User information table
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_users")
public class User {

    /**
     * User unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Username, used for login, globally unique
     */
    @TableField("username")
    private String username;

    /**
     * Email address, used for login and notifications, globally unique
     */
    @TableField("email")
    private String email;

    /**
     * Password hash value, encrypted by application layer
     */
    @TableField("password_hash")
    private String passwordHash;

    /**
     * Phone number
     */
    @TableField("phone")
    private String phone;

    /**
     * Avatar image URL address
     */
    @TableField("avatar_url")
    private String avatarUrl;

    /**
     * Delete flag: 0=not deleted, 1=deleted
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * Email verification status: false=unverified, true=verified
     */
    @TableField("verified")
    private Boolean verified;

    /**
     * Account creation time
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * Account information last update time
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
