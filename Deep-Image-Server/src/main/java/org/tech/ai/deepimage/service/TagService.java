package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.DeleteTagRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateTagRequest;
import org.tech.ai.deepimage.model.dto.response.TagResponse;

import java.util.List;
import java.util.Map;

/**
 * Tag Service interface
 * 
 * @author zgq
 * @since 2025-10-01
 */
public interface TagService extends IService<Tag> {
    
    /**
     * Create tag
     * 
     * @param request Create tag request
     * @return Tag response
     */
    TagResponse createTag(CreateTagRequest request);
    
    /**
     * Query all tags of current user
     * 
     * @return Tag list
     */
    List<TagResponse> listUserTags();
    
    /**
     * Update tag
     * 
     * @param request Update tag request
     * @return Tag response
     */
    TagResponse updateTag(UpdateTagRequest request);
    
    /**
     * Delete tag
     * 
     * @param request Delete tag request
     */
    void deleteTag(DeleteTagRequest request);
    
    /**
     * Batch increase tag usage count
     * 
     * @param tagIds Tag ID set
     */
    void batchIncreaseUsageCount(java.util.Set<Long> tagIds);
    
    /**
     * Batch decrease tag usage count (decrease by 1 for each tag)
     * 
     * @param tagIds Tag ID set
     */
    void batchDecreaseUsageCount(java.util.Set<Long> tagIds);

    /**
     * Batch decrease tag usage count (by specified amount)
     * 
     * @param tagCountMap Tag ID -> decrease amount mapping
     */
    void batchDecreaseUsageCountByAmount(Map<Long, Integer> tagCountMap);
    
    /**
     * Batch query tags (validate permissions)
     * 
     * @param tagIds Tag ID list
     * @param userId User ID
     * @return Valid tag list
     */
    List<Tag> listValidTagsByIds(List<Long> tagIds, Long userId);
    
    /**
     * Get user's tag (validate permissions)
     * Throws exception if tag does not exist or does not belong to this user
     * 
     * @param tagId Tag ID
     * @param userId User ID
     * @return Tag entity
     */
    Tag getUserTag(Long tagId, Long userId);
}

