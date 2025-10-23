package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tech.ai.deepimage.entity.FileTag;
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.mapper.FileTagMapper;
import org.tech.ai.deepimage.model.dto.response.TagResponse;
import org.tech.ai.deepimage.service.FileTagService;
import org.tech.ai.deepimage.service.TagService;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 文件标签关联Service实现类
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileTagServiceImpl extends ServiceImpl<FileTagMapper, FileTag> implements FileTagService {
    
    private final TagService tagService;
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void batchSetFileTags(Long fileId, Long userId, List<Long> tagIds) {
        // 1. 查询文件当前的所有标签关联（用于后续减少使用计数）
        Set<Long> oldTagIds = deleteAllByFileId(fileId);
        
        // 2. 如果新标签列表为空，直接返回（只删除不添加）
        if (CollectionUtils.isEmpty(tagIds)) {
            if (CollectionUtils.isNotEmpty(oldTagIds)) {
                tagService.batchDecreaseUsageCount(oldTagIds);
            }
            log.info("清空文件所有标签: fileId={}", fileId);
            return;
        }
        
        // 3. 批量查询新标签，验证标签权限
        List<Tag> validTags = tagService.listValidTagsByIds(tagIds, userId);
        
        if (CollectionUtils.isEmpty(validTags)) {
            log.warn("没有找到有效的标签: fileId={}, tagIds={}", fileId, tagIds);
            if (CollectionUtils.isNotEmpty(oldTagIds)) {
                tagService.batchDecreaseUsageCount(oldTagIds);
            }
            return;
        }
        
        // 提取有效的标签ID
        List<Long> validTagIds = validTags.stream()
                .map(Tag::getId)
                .toList();
        
        // 4. 批量插入新的关联关系
        LocalDateTime now = LocalDateTime.now();
        List<FileTag> newFileTags = validTagIds.stream()
                .map(tagId -> {
                    FileTag fileTag = new FileTag();
                    fileTag.setFileId(fileId);
                    fileTag.setTagId(tagId);
                    fileTag.setCreatedAt(now);
                    return fileTag;
                })
                .collect(Collectors.toList());
        
        saveBatch(newFileTags);
        
        // 5. 更新标签使用计数
        Set<Long> newTagIdSet = new HashSet<>(validTagIds);
        
        // 减少旧标签中不再使用的标签计数
        Set<Long> removedTagIds = oldTagIds.stream()
                .filter(tagId -> !newTagIdSet.contains(tagId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(removedTagIds)) {
            tagService.batchDecreaseUsageCount(removedTagIds);
        }
        
        // 增加新标签中之前没有的标签计数
        Set<Long> addedTagIds = validTagIds.stream()
                .filter(tagId -> !oldTagIds.contains(tagId))
                .collect(Collectors.toSet());
        if (CollectionUtils.isNotEmpty(addedTagIds)) {
            tagService.batchIncreaseUsageCount(addedTagIds);
        }
        
        log.info("批量设置文件标签成功: fileId={}, 新增{}个, 移除{}个", 
                fileId, addedTagIds.size(), removedTagIds.size());
    }
    
    @Override
    public List<TagResponse> getFileTagsResponse(Long fileId) {
        // 查询文件的所有标签关联
        LambdaQueryWrapper<FileTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileTag::getFileId, fileId);
        List<FileTag> fileTags = list(wrapper);
        
        if (CollectionUtils.isEmpty(fileTags)) {
            return List.of();
        }
        
        // 提取标签ID列表
        List<Long> tagIds = fileTags.stream()
                .map(FileTag::getTagId)
                .collect(Collectors.toList());
        
        // 批量查询标签详情
        List<Tag> tags = tagService.listByIds(tagIds);
        
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Set<Long> deleteAllByFileId(Long fileId) {
        // 查询文件当前的所有标签关联
        LambdaQueryWrapper<FileTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileTag::getFileId, fileId);
        List<FileTag> oldFileTags = list(wrapper);
        
        // 提取旧标签ID
        Set<Long> oldTagIds = oldFileTags.stream()
                .map(FileTag::getTagId)
                .collect(Collectors.toSet());
        
        // 删除文件的所有旧标签关联
        if (CollectionUtils.isNotEmpty(oldFileTags)) {
            remove(wrapper);
            log.info("删除文件旧标签关联: fileId={}, 删除{}个", fileId, oldFileTags.size());
        }
        
        return oldTagIds;
    }
}
