package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.FileAccessLog;
import org.tech.ai.deepimage.mapper.FileAccessLogMapper;
import org.tech.ai.deepimage.service.FileAccessLogService;

/**
 * 文件访问日志Service实现类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Service
public class FileAccessLogServiceImpl extends ServiceImpl<FileAccessLogMapper, FileAccessLog> 
        implements FileAccessLogService {
    
}
