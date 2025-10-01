package org.tech.ai.deepimage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.DeleteTagRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateTagRequest;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;
import org.tech.ai.deepimage.model.dto.response.TagResponse;
import org.tech.ai.deepimage.service.TagService;

import java.util.List;

/**
 * 标签管理Controller
 * 
 * @author zgq
 * @since 2025-10-01
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    /**
     * 创建标签
     * 
     * @param request 创建标签请求
     * @return 标签响应
     */
    @PostMapping
    public ApiResponse<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 查询当前用户所有标签
     * 
     * @return 标签列表
     */
    @GetMapping
    public ApiResponse<List<TagResponse>> listTags() {
        List<TagResponse> tags = tagService.listUserTags();
        return ApiResponse.success(tags);
    }
    
    /**
     * 更新标签
     * 
     * @param request 更新标签请求
     * @return 标签响应
     */
    @PutMapping
    public ApiResponse<TagResponse> updateTag(@Valid @RequestBody UpdateTagRequest request) {
        TagResponse response = tagService.updateTag(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 删除标签
     * 
     * @param request 删除标签请求
     * @return 成功响应
     */
    @DeleteMapping
    public ApiResponse<Void> deleteTag(@Valid @RequestBody DeleteTagRequest request) {
        tagService.deleteTag(request);
        return ApiResponse.success(null);
    }
}

