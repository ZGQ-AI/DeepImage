package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.FileShare;
import org.tech.ai.deepimage.mapper.FileShareMapper;
import org.tech.ai.deepimage.service.FileShareService;

/**
 * 文件分享Service实现类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Service
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> 
        implements FileShareService {
    
}
