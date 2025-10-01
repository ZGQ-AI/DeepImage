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
import org.tech.ai.deepimage.entity.Tag;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.mapper.FileTagMapper;
import org.tech.ai.deepimage.mapper.TagMapper;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.DeleteTagRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateTagRequest;
import org.tech.ai.deepimage.model.dto.response.TagResponse;
import org.tech.ai.deepimage.service.TagService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 标签Service实现类
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
        // 从Sa-Token获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("创建标签: userId={}, tagName={}", userId, request.getTagName());
        
        // 构建标签实体
        Tag tag = new Tag();
        tag.setUserId(userId);
        tag.setTagName(request.getTagName().trim());
        tag.setColor(request.getColor());
        tag.setUsageCount(0);
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUpdatedAt(LocalDateTime.now());
        
        try {
            // 保存标签
            save(tag);
            log.info("标签创建成功: tagId={}, tagName={}", tag.getId(), tag.getTagName());
            return TagResponse.from(tag);
        } catch (DuplicateKeyException e) {
            log.warn("标签名已存在: userId={}, tagName={}", userId, request.getTagName());
            throw BusinessException.badRequest("标签名已存在");
        }
    }

    @Override
    public List<TagResponse> listUserTags() {
        // 从Sa-Token获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("查询用户所有标签: userId={}", userId);
        
        // 查询用户的所有标签，按使用次数降序
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Tag::getUserId, userId)
                .orderByDesc(Tag::getUsageCount)
                .orderByDesc(Tag::getCreatedAt);
        
        List<Tag> tags = list(wrapper);
        log.info("查询到用户标签数量: userId={}, count={}", userId, tags.size());
        
        return tags.stream()
                .map(TagResponse::from)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TagResponse updateTag(UpdateTagRequest request) {
        // 从Sa-Token获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        Long tagId = request.getTagId();
        log.info("更新标签: userId={}, tagId={}", userId, tagId);
        
        // 查询标签并校验权限
        Tag tag = getById(tagId);
        if (tag == null) {
            log.warn("标签不存在: tagId={}", tagId);
            throw BusinessException.notFound("标签不存在");
        }
        
        if (!tag.getUserId().equals(userId)) {
            log.warn("无权操作该标签: userId={}, tagId={}, tagOwnerId={}", userId, tagId, tag.getUserId());
            throw BusinessException.forbidden("无权操作该标签");
        }
        
        // 构建更新条件
        LambdaUpdateWrapper<Tag> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Tag::getId, tagId);
        
        boolean needUpdate = false;
        
        // 更新标签名
        if (StringUtils.hasText(request.getTagName())) {
            String newTagName = request.getTagName().trim();
            if (!newTagName.equals(tag.getTagName())) {
                updateWrapper.set(Tag::getTagName, newTagName);
                needUpdate = true;
            }
        }
        
        // 更新颜色
        if (StringUtils.hasText(request.getColor())) {
            if (!request.getColor().equals(tag.getColor())) {
                updateWrapper.set(Tag::getColor, request.getColor());
                needUpdate = true;
            }
        }
        
        if (!needUpdate) {
            log.info("标签无需更新: tagId={}", tagId);
            return TagResponse.from(tag);
        }
        
        // 更新时间
        updateWrapper.set(Tag::getUpdatedAt, LocalDateTime.now());
        
        try {
            update(updateWrapper);
            log.info("标签更新成功: tagId={}", tagId);
            
            // 查询更新后的标签
            Tag updatedTag = getById(tagId);
            return TagResponse.from(updatedTag);
        } catch (DuplicateKeyException e) {
            log.warn("标签名已存在: userId={}, tagName={}", userId, request.getTagName());
            throw BusinessException.badRequest("标签名已存在");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTag(DeleteTagRequest request) {
        // 从Sa-Token获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        Long tagId = request.getTagId();
        log.info("删除标签: userId={}, tagId={}", userId, tagId);
        
        // 查询标签并校验权限
        Tag tag = getById(tagId);
        if (tag == null) {
            log.warn("标签不存在: tagId={}", tagId);
            throw BusinessException.notFound("标签不存在");
        }
        
        if (!tag.getUserId().equals(userId)) {
            log.warn("无权操作该标签: userId={}, tagId={}, tagOwnerId={}", userId, tagId, tag.getUserId());
            throw BusinessException.forbidden("无权操作该标签");
        }
        
        // 删除标签（级联删除文件-标签关联）
        removeById(tagId);
        
        // 删除该标签的所有文件关联
        LambdaQueryWrapper<org.tech.ai.deepimage.entity.FileTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(org.tech.ai.deepimage.entity.FileTag::getTagId, tagId);
        fileTagMapper.delete(wrapper);
        
        log.info("标签删除成功: tagId={}, 删除了 {} 个文件关联", tagId, tag.getUsageCount());
    }
}


