package org.tech.ai.deepimage.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.tech.ai.deepimage.entity.FileAccessLog;

/**
 * File access log table Mapper interface
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Mapper
public interface FileAccessLogMapper extends BaseMapper<FileAccessLog> {
    
}

