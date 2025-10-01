package org.tech.ai.deepimage.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件-标签关联表实体
 * 多对多关系，一个文件可以有多个标签
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@TableName("di_file_tags")
public class FileTag {

    /**
     * 关联记录唯一标识
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文件ID，引用di_file_records表
     */
    @TableField("file_id")
    private Long fileId;

    /**
     * 标签ID，引用di_tags表
     */
    @TableField("tag_id")
    private Long tagId;

    /**
     * 标签添加时间
     */
    @TableField("created_at")
    private LocalDateTime createdAt;
}

