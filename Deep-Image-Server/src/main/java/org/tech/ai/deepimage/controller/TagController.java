package org.tech.ai.deepimage.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.tech.ai.deepimage.annotation.LogParams;
import org.tech.ai.deepimage.model.dto.request.CreateTagRequest;
import org.tech.ai.deepimage.model.dto.request.DeleteTagRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateTagRequest;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;
import org.tech.ai.deepimage.model.dto.response.TagResponse;
import org.tech.ai.deepimage.service.TagService;

import java.util.List;

/**
 * Tag management Controller
 * 
 * @author zgq
 * @since 2025-10-01
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@LogParams
public class TagController {
    
    private final TagService tagService;
    
    /**
     * Create tag
     * 
     * @param request Create tag request
     * @return Tag response
     */
    @PostMapping
    public ApiResponse<TagResponse> createTag(@Valid @RequestBody CreateTagRequest request) {
        TagResponse response = tagService.createTag(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Query all tags of current user
     * 
     * @return Tag list
     */
    @GetMapping
    public ApiResponse<List<TagResponse>> listTags() {
        List<TagResponse> tags = tagService.listUserTags();
        return ApiResponse.success(tags);
    }
    
    /**
     * Update tag
     * 
     * @param request Update tag request
     * @return Tag response
     */
    @PutMapping
    public ApiResponse<TagResponse> updateTag(@Valid @RequestBody UpdateTagRequest request) {
        TagResponse response = tagService.updateTag(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Delete tag
     * 
     * @param request Delete tag request
     * @return Success response
     */
    @DeleteMapping
    public ApiResponse<Void> deleteTag(@Valid @RequestBody DeleteTagRequest request) {
        tagService.deleteTag(request);
        return ApiResponse.success(null);
    }
}

