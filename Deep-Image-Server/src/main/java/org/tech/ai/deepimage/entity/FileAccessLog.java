package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件访问日志表实体
 * 记录文件访问行为
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_access_logs")
public class FileAccessLog {

    /**
     * 日志记录唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被访问的文件ID
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 访问用户ID，未登录用户为NULL
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 访问类型枚举: DOWNLOAD, PREVIEW, UPLOAD
     */
    @TableField("access_type")
    private String accessType;

    /**
     * 访问IP地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 用户代理信息
     */
    @TableField("user_agent")
    private String userAgent;

    /**
     * 分享ID，如果是通过分享访问
     */
    @TableField("share_id")
    private Long shareId;

    /**
     * 访问时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

