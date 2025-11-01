package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tech.ai.deepimage.entity.FileTag;

import java.util.List;

/**
 * File-tag association table Mapper interface
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Mapper
public interface FileTagMapper extends BaseMapper<FileTag> {

    /**
     * Batch delete tag associations of files
     */
    int deleteBatchByFileIds(@Param("fileIds") List<Long> fileIds);
}

