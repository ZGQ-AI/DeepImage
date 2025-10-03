package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.FileShare;

/**
 * 文件分享Service接口
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface FileShareService extends IService<FileShare> {
    
    /**
     * 获取用户对指定文件的有效分享记录
     * 查询最新的、未撤销的分享记录
     * 
     * @param fileId 文件ID
     * @param userId 被分享用户ID
     * @return 分享记录，如果不存在则返回 null
     */
    FileShare getValidShareByFileAndUser(Long fileId, Long userId);
    
}
