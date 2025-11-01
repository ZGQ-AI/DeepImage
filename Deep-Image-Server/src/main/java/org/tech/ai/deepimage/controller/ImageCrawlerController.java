package org.tech.ai.deepimage.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.tech.ai.deepimage.annotation.LogParams;
import org.tech.ai.deepimage.model.dto.ImageInfo;
import org.tech.ai.deepimage.model.dto.request.ImageSearchRequest;
import org.tech.ai.deepimage.model.dto.request.ImageDownloadRequest;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;
import org.tech.ai.deepimage.model.dto.response.DownloadResult;
import org.tech.ai.deepimage.model.dto.response.ImageSearchResponse;
import org.tech.ai.deepimage.service.ImageSearchService;
import org.tech.ai.deepimage.service.ImageDownloadService;
import org.tech.ai.deepimage.util.Timer;

import java.util.List;

/**
 * Image crawler controller
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
@RequiredArgsConstructor
@LogParams
public class ImageCrawlerController {

    private final ImageSearchService imageSearchService;
    private final ImageDownloadService imageDownloadService;

    /**
     * Search images (synchronous operation, returns only image URL list)
     *
     * @param request Search request
     * @return Image list
     */
    @SaCheckLogin
    @PostMapping("/search")
    public ApiResponse<ImageSearchResponse> searchImages(@Valid @RequestBody ImageSearchRequest request) {
        Timer timer = Timer.start();
        List<ImageInfo> images = imageSearchService.searchImages(request.getKeyword(), request.getCount());
        timer.stop();
        
        ImageSearchResponse response = ImageSearchResponse.builder()
                .keyword(request.getKeyword())
                .images(images)
                .totalCount(images.size())
                .searchTimeMs(timer.getElapsedMillis())
                .build();

        log.info("Image search completed: found {} images, elapsed time: {}ms", images.size(), timer.getElapsedMillis());
        return ApiResponse.success(response);
    }

    /**
     * Download selected images (synchronous operation)
     *
     * @param request Download request
     * @return Download result
     */
    @SaCheckLogin
    @PostMapping("/download")
    public ApiResponse<DownloadResult> downloadImages(@Valid @RequestBody ImageDownloadRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        DownloadResult result = imageDownloadService.downloadImages(
                request.getSelectedImages(),
                userId,
                request.getKeyword(),
                request.getTagIds());

        log.info("Image download completed: success {}/{}, tag count: {}", 
                result.getSuccessCount(), 
                result.getTotalCount(),
                request.getTagIds() != null ? request.getTagIds().size() : 0);
        return ApiResponse.success(result);
    }

}
