package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.FileRecord;
import org.tech.ai.deepimage.mapper.FileRecordMapper;
import org.tech.ai.deepimage.service.FileRecordService;

/**
 * 文件记录Service实现类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Service
public class FileRecordServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> 
        implements FileRecordService {
    
}
