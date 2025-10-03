package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.entity.FileShare;
import org.tech.ai.deepimage.mapper.FileShareMapper;
import org.tech.ai.deepimage.service.FileShareService;

/**
 * 文件分享Service实现类
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileShareServiceImpl extends ServiceImpl<FileShareMapper, FileShare> implements FileShareService {
    
    @Override
    public FileShare getValidShareByFileAndUser(Long fileId, Long userId) {
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getFileId, fileId)
                .eq(FileShare::getShareToUserId, userId)
                .eq(FileShare::getRevoked, org.tech.ai.deepimage.enums.RevokedStatusEnum.NOT_REVOKED.getValue())
                .orderByDesc(FileShare::getCreatedAt)
                .last("LIMIT 1");
        
        return getOne(wrapper);
    }
    
}
