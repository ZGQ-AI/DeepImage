package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.tech.ai.deepimage.entity.FileTag;

import java.util.List;

/**
 * 文件-标签关联表 Mapper 接口
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Mapper
public interface FileTagMapper extends BaseMapper<FileTag> {

    /**
     * 批量删除文件的标签关联
     */
    int deleteBatchByFileIds(@Param("fileIds") List<Long> fileIds);
}

