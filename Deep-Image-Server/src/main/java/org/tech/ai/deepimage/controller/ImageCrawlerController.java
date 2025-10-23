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
 * 图片抓取控制器
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
     * 搜索图片（同步操作，只返回图片URL列表）
     *
     * @param request 搜索请求
     * @return 图片列表
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

        log.info("图片搜索完成：找到 {} 张图片，耗时：{}ms", images.size(), timer.getElapsedMillis());
        return ApiResponse.success(response);
    }

    /**
     * 下载选中的图片（同步操作）
     *
     * @param request 下载请求
     * @return 下载结果
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

        log.info("图片下载完成：成功 {}/{}，标签数：{}", 
                result.getSuccessCount(), 
                result.getTotalCount(),
                request.getTagIds() != null ? request.getTagIds().size() : 0);
        return ApiResponse.success(result);
    }

}
