package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.DeleteTagRequest;
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
     * @param request 创建标签请求
     * @return 标签响应
     */
    TagResponse createTag(CreateTagRequest request);
    
    /**
     * 查询当前用户所有标签
     * 
     * @return 标签列表
     */
    List<TagResponse> listUserTags();
    
    /**
     * 更新标签
     * 
     * @param request 更新标签请求
     * @return 标签响应
     */
    TagResponse updateTag(UpdateTagRequest request);
    
    /**
     * 删除标签
     * 
     * @param request 删除标签请求
     */
    void deleteTag(DeleteTagRequest request);
    
    /**
     * 批量增加标签使用计数
     * 
     * @param tagIds 标签ID集合
     */
    void batchIncreaseUsageCount(java.util.Set<Long> tagIds);
    
    /**
     * 批量减少标签使用计数
     * 
     * @param tagIds 标签ID集合
     */
    void batchDecreaseUsageCount(java.util.Set<Long> tagIds);
    
    /**
     * 批量查询标签（验证权限）
     * 
     * @param tagIds 标签ID列表
     * @param userId 用户ID
     * @return 有效的标签列表
     */
    List<Tag> listValidTagsByIds(List<Long> tagIds, Long userId);
    
    /**
     * 获取用户的标签（校验权限）
     * 如果标签不存在或不属于该用户，抛出异常
     * 
     * @param tagId 标签ID
     * @param userId 用户ID
     * @return 标签实体
     */
    Tag getUserTag(Long tagId, Long userId);
}

