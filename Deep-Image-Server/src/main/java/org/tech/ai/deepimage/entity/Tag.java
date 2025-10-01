package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签表实体
 * 用户自定义标签用于文件分类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_tags")
public class Tag {

    /**
     * 标签唯一标识，主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 标签所属用户ID，用户隔离
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 标签名称，如：#工作, #旅行, #重要
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * 标签颜色，十六进制格式，如：#FF5733
     */
    @TableField("color")
    private String color;

    /**
     * 标签使用次数统计
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * 标签创建时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * 标签最后更新时间
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

