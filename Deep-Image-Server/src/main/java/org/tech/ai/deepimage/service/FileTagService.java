package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.FileTag;
import org.tech.ai.deepimage.model.dto.response.TagResponse;

import java.util.List;
import java.util.Set;

/**
 * File-tag association Service interface
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface FileTagService extends IService<FileTag> {
    
    /**
     * Batch set file tags (delete old tags first, then insert new tags)
     * 
     * @param fileId File ID
     * @param userId User ID
     * @param tagIds New tag ID list
     */
    void batchSetFileTags(Long fileId, Long userId, List<Long> tagIds);
    
    /**
     * Get all tags of a file
     * 
     * @param fileId File ID
     * @return Tag response list
     */
    List<TagResponse> getFileTagsResponse(Long fileId);
    
    /**
     * Delete all tag associations of a file
     * 
     * @param fileId File ID
     * @return Deleted tag ID set
     */
    Set<Long> deleteAllByFileId(Long fileId);
}
