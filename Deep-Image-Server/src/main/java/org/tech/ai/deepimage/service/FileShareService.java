package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.FileShare;

/**
 * File share Service interface
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface FileShareService extends IService<FileShare> {
    
    /**
     * Get user's valid share record for specified file
     * Query latest, non-revoked share record
     * 
     * @param fileId File ID
     * @param userId Shared user ID
     * @return Share record, returns null if not exists
     */
    FileShare getValidShareByFileAndUser(Long fileId, Long userId);
    
}
