package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.mapper.FileTagMapper;
import org.tech.ai.deepimage.mapper.TagMapper;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.DeleteTagRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateTagRequest;
import org.tech.ai.deepimage.model.dto.response.TagResponse;
import org.tech.ai.deepimage.service.TagService;

import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tag Service implementation
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    private final FileTagMapper fileTagMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagResponse createTag(CreateTagRequest request) {
        // Get current logged-in user ID from Sa-Token
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("Creating tag: userId={}, tagName={}", userId, request.getTagName());
        
        // Build tag entity
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setTagName(request.getTagName().trim());
        tag.setColor(request.getColor());
        tag.setUsageCount(0);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        
        try {
            // Save tag
            save(tag);
            log.info("Tag created successfully: tagId={}, tagName={}", tag.getId(), tag.getTagName());
            return TagResponse.from(tag);
        } catch (DuplicateKeyException e) {
            log.warn("Tag name already exists: userId={}, tagName={}", userId, request.getTagName());
            throw BusinessException.badRequest("Tag name already exists");
        }
    }

    @Override
    public List<TagResponse> listUserTags() {
        // Get current logged-in user ID from Sa-Token
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("Query all user tags: userId={}", userId);
        
        // Query all tags of the user, sorted by usage count descending
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByDesc(Tag::getUsageCount)
                .orderByDesc(Tag::getCreatedAt);
        
        List<Tag> tags = list(wrapper);
        log.info("Found user tags count: userId={}, count={}", userId, tags.size());
        
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagResponse updateTag(UpdateTagRequest request) {
        // Get current logged-in user ID from Sa-Token
        Long userId = StpUtil.getLoginIdAsLong();
        Long tagId = request.getTagId();
        log.info("Updating tag: userId={}, tagId={}", userId, tagId);
        
        // Query tag and validate permissions
        Tag tag = getById(tagId);
        if (tag == null) {
            log.warn("Tag not found: tagId={}", tagId);
            throw BusinessException.notFound("Tag not found");
        }
        
        if (!tag.getUserId().equals(userId)) {
            log.warn("No permission to operate this tag: userId={}, tagId={}, tagOwnerId={}", userId, tagId, tag.getUserId());
            throw BusinessException.forbidden("No permission to operate this tag");
        }
        
        // Build update conditions
        LambdaUpdateWrapper<Tag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Tag::getId, tagId);
        
        boolean needUpdate = false;
        
        // Update tag name
        if (StringUtils.hasText(request.getTagName())) {
            String newTagName = request.getTagName().trim();
            if (!newTagName.equals(tag.getTagName())) {
                updateWrapper.set(Tag::getTagName, newTagName);
                needUpdate = true;
            }
        }
        
        // Update color
        if (StringUtils.hasText(request.getColor())) {
            if (!request.getColor().equals(tag.getColor())) {
                updateWrapper.set(Tag::getColor, request.getColor());
                needUpdate = true;
            }
        }
        
        if (!needUpdate) {
            log.info("Tag does not need update: tagId={}", tagId);
            return TagResponse.from(tag);
        }
        
        // Update time
        updateWrapper.set(Tag::getUpdatedAt, LocalDateTime.now());
        
        try {
            update(updateWrapper);
            log.info("Tag updated successfully: tagId={}", tagId);
            
            // Query updated tag
            Tag updatedTag = getById(tagId);
            return TagResponse.from(updatedTag);
        } catch (DuplicateKeyException e) {
            log.warn("Tag name already exists: userId={}, tagName={}", userId, request.getTagName());
            throw BusinessException.badRequest("Tag name already exists");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(DeleteTagRequest request) {
        // Get current logged-in user ID from Sa-Token
        Long userId = StpUtil.getLoginIdAsLong();
        Long tagId = request.getTagId();
        log.info("Deleting tag: userId={}, tagId={}", userId, tagId);
        
        // Query tag and validate permissions
        Tag tag = getById(tagId);
        if (tag == null) {
            log.warn("Tag not found: tagId={}", tagId);
            throw BusinessException.notFound("Tag not found");
        }
        
        if (!tag.getUserId().equals(userId)) {
            log.warn("No permission to operate this tag: userId={}, tagId={}, tagOwnerId={}", userId, tagId, tag.getUserId());
            throw BusinessException.forbidden("No permission to operate this tag");
        }
        
        // Delete tag (cascade delete file-tag associations)
        removeById(tagId);
        
        // Delete all file associations of this tag
        LambdaQueryWrapper<org.tech.ai.deepimage.entity.FileTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(org.tech.ai.deepimage.entity.FileTag::getTagId, tagId);
        fileTagMapper.delete(wrapper);
        
        log.info("Tag deleted successfully: tagId={}, deleted {} file associations", tagId, tag.getUsageCount());
    }
    
    @Override
    public void batchIncreaseUsageCount(Set<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        
        // Use LambdaUpdateWrapper to execute SQL:
        // UPDATE di_tags SET usage_count = usage_count + 1 WHERE id IN (...)
        LambdaUpdateWrapper<Tag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Tag::getId, tagIds)
                .setSql("usage_count = usage_count + 1");
        
        update(updateWrapper);
        log.info("Batch increase tag usage count: tagIds={}", tagIds);
    }
    
    @Override
    public void batchDecreaseUsageCount(Set<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        
        // Use LambdaUpdateWrapper to execute SQL:
        // UPDATE di_tags SET usage_count = usage_count - 1 WHERE id IN (...) AND usage_count > 0
        LambdaUpdateWrapper<Tag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Tag::getId, tagIds)
                .gt(Tag::getUsageCount, 0)
                .setSql("usage_count = usage_count - 1");
        
        update(updateWrapper);
        log.info("Batch decrease tag usage count: tagIds={}", tagIds);
    }

    @Override
    public void batchDecreaseUsageCountByAmount(Map<Long, Integer> tagCountMap) {
        if (tagCountMap == null || tagCountMap.isEmpty()) {
            return;
        }

        // Update usage count for each tag one by one
        for (Map.Entry<Long, Integer> entry : tagCountMap.entrySet()) {
            Long tagId = entry.getKey();
            Integer count = entry.getValue();
            if (count <= 0) {
                continue;
            }

            // Directly subtract the corresponding amount
            LambdaUpdateWrapper<Tag> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Tag::getId, tagId)
                    .setSql("usage_count = usage_count - " + count);

            update(updateWrapper);
        }
        
        log.info("Batch decrease tag usage count (by amount): tagCountMap={}", tagCountMap);
    }
    
    @Override
    public List<Tag> listValidTagsByIds(List<Long> tagIds, Long userId) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return List.of();
        }
        
        // Batch query tags and validate permissions
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(Tag::getId, tagIds)
                .eq(Tag::getUserId, userId);
        
        return list(wrapper);
    }
    
    @Override
    public Tag getUserTag(Long tagId, Long userId) {
        // Query with userId to ensure tag belongs to this user
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getId, tagId)
                .eq(Tag::getUserId, userId);
        
        Tag tag = getOne(wrapper);
        BusinessException.assertNotNull(tag, ResponseConstant.TAG_NOT_FOUND_MESSAGE);
        
        return tag;
    }
}


