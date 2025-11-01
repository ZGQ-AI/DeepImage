package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Tag table entity
 * User-defined tags for file categorization
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_tags")
public class Tag {

    /**
     * Tag unique identifier, primary key
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * Tag owner user ID, user isolation
     */
    @TableField("user_id")
    private Long userId;

    /**
     * Tag name, e.g.: #工作, #旅行, #重要
     */
    @TableField("tag_name")
    private String tagName;

    /**
     * Tag color, hexadecimal format, e.g.: #FF5733
     */
    @TableField("color")
    private String color;

    /**
     * Tag usage count statistics
     */
    @TableField("usage_count")
    private Integer usageCount;

    /**
     * Tag creation time
     */
    @TableField("created_at")
    private LocalDateTime createdAt;

    /**
     * Tag last update time
     */
    @TableField("updated_at")
    private LocalDateTime updatedAt;
}

