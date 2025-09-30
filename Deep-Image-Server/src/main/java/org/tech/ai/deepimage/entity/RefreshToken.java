package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 刷新令牌表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_refresh_tokens")
public class RefreshToken {

    /**
     * 刷新令牌唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，引用sys_users表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 刷新令牌哈希值，不以明文存储，全局唯一
     */
    @TableField("token_hash")
    private String tokenHash;

    /**
     * 关联的会话ID，引用sys_sessions表
     */
    @TableField("session_id")
    private Long sessionId;

    /**
     * 刷新令牌过期时间
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 最后使用时间，用于活动统计
     */
    @TableField("last_used_at")
    private LocalDateTime lastUsedAt;

    /**
     * 使用状态：0=未使用，1=已使用
     */
    @TableField("revoked")
    private Integer revoked;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 令牌创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 令牌信息最后更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
