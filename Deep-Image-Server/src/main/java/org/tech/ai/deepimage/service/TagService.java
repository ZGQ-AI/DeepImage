package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateTagRequest;
import org.tech.ai.deepimage.model.dto.response.TagResponse;

import java.util.List;

/**
 * 标签Service接口
 * 
 * @author zgq
 * @since 2025-10-01
 */
public interface TagService extends IService<Tag> {
    
    /**
     * 创建标签
     * 
     * @param userId 用户ID
     * @param request 创建标签请求
     * @return 标签响应
     */
    TagResponse createTag(Long userId, CreateTagRequest request);
    
    /**
     * 查询用户所有标签
     * 
     * @param userId 用户ID
     * @return 标签列表
     */
    List<TagResponse> listUserTags(Long userId);
    
    /**
     * 更新标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @param request 更新标签请求
     * @return 标签响应
     */
    TagResponse updateTag(Long userId, Long tagId, UpdateTagRequest request);
    
    /**
     * 删除标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     */
    void deleteTag(Long userId, Long tagId);
}

