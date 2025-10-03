package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.FileTag;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.model.dto.response.TagResponse;

import java.util.List;
import java.util.Set;

/**
 * 文件标签关联Service接口
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface FileTagService extends IService<FileTag> {
    
    /**
     * 批量设置文件标签（先删除旧标签，再插入新标签）
     * 
     * @param fileId 文件ID
     * @param userId 用户ID
     * @param tagIds 新的标签ID列表
     */
    void batchSetFileTags(Long fileId, Long userId, List<Long> tagIds);
    
    /**
     * 获取文件的所有标签
     * 
     * @param fileId 文件ID
     * @return 标签响应列表
     */
    List<TagResponse> getFileTagsResponse(Long fileId);
    
    /**
     * 删除文件的所有标签关联
     * 
     * @param fileId 文件ID
     * @return 删除的标签ID集合
     */
    Set<Long> deleteAllByFileId(Long fileId);
}
