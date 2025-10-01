package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分享表实体
 * 管理用户间的文件分享关系
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_shares")
public class FileShare {

    /**
     * 分享记录唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 被分享的文件ID，引用di_file_records表
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 分享者用户ID(谁分享的)，引用sys_users表
     */
    @TableField("share_from_user_id")
    private Long shareFromUserId;

    /**
     * 接收者用户ID(分享给谁)，引用sys_users表
     */
    @TableField("share_to_user_id")
    private Long shareToUserId;

    /**
     * 分享类型枚举: PERMANENT, TEMPORARY
     */
    @TableField("share_type")
    private String shareType;

    /**
     * 过期时间，仅TEMPORARY类型需要，NULL表示永久有效
     */
    @TableField("expires_at")
    private LocalDateTime expiresAt;

    /**
     * 分享权限级别枚举: VIEW, DOWNLOAD, EDIT
     */
    @TableField("permission_level")
    private String permissionLevel;

    /**
     * 撤销状态: 0=有效, 1=已撤销(软删除，保留分享历史)
     */
    @TableField("revoked")
    private Integer revoked;

    /**
     * 分享留言，分享者给接收者的消息
     */
    @TableField("message")
    private String message;

    /**
     * 查看次数统计
     */
    @TableField("view_count")
    private Integer viewCount;

    /**
     * 下载次数统计
     */
    @TableField("download_count")
    private Integer downloadCount;

    /**
     * 分享创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 分享信息最后更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

