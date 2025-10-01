package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.FileTag;
import org.tech.ai.deepimage.mapper.FileTagMapper;
import org.tech.ai.deepimage.service.FileTagService;

/**
 * 文件-标签关联Service实现类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Service
public class FileTagServiceImpl extends ServiceImpl<FileTagMapper, FileTag> implements FileTagService {

}

