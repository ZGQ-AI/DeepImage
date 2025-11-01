package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File-tag association table entity
 * Many-to-many relationship, one file can have multiple tags
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_tags")
public class FileTag {

    /**
     * Association record unique identifier
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * File ID, references di_file_records table
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * Tag ID, references di_tags table
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * Tag addition time
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

