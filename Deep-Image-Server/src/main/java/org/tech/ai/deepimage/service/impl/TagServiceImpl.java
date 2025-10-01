package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.mapper.TagMapper;
import org.tech.ai.deepimage.service.TagService;

/**
 * 标签Service实现类
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

}

