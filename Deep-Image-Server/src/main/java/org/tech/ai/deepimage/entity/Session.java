package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户会话表
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Data
@TableName("sys_sessions")
public class Session {

    /**
     * 会话唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户ID，引用sys_users表
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 访问令牌哈希值，不以明文存储
     */
    @TableField("access_token_hash")
    private String accessTokenHash;

    /**
     * 设备信息（设备类型、操作系统、浏览器等）
     */
    @TableField("device_info")
    private String deviceInfo;

    /**
     * 登录IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理字符串，用于设备识别
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 会话活跃状态：0=已撤销，1=活跃
     */
    @TableField("active")
    private Integer active;

    /**
     * 最后访问时间，用于清理长期未使用的会话
     */
    @TableField("last_accessed_at")
    private LocalDateTime lastAccessedAt;

    /**
     * 删除标志：0=未删除，1=已删除
     */
    @TableLogic
    @TableField("delete_flag")
    private Integer deleteFlag;

    /**
     * 会话创建时间
     */
    @TableField(value = "created_at")
    private LocalDateTime createdAt;

    /**
     * 会话信息最后更新时间
     */
    @TableField(value = "updated_at")
    private LocalDateTime updatedAt;
}
