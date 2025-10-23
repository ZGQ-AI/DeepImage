package org.tech.ai.deepimage.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.tech.ai.deepimage.model.dto.ImageInfo;
import org.tech.ai.deepimage.service.ImageSearchService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 图片搜索服务实现类
 */
@Slf4j
@Service
public class ImageSearchServiceImpl implements ImageSearchService {

    private static final String BING_SEARCH_URL = "https://cn.bing.com/images/async";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int REQUEST_TIMEOUT = 30000; // 30秒超时
    
    // 重试配置
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long BASE_RETRY_DELAY_MS = 1000; // 基础重试延迟1秒


    @Override
    public List<ImageInfo> searchImages(String keyword, int count) {
        List<ImageInfo> imageList = new ArrayList<>();
        
        try {
            log.info("开始搜索图片，关键词：{}，数量：{}", keyword, count);
            
            // 构建搜索URL
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = BING_SEARCH_URL + "?q=" + encodedKeyword + "&mmasync=1";
            
            // 带重试的HTTP请求
            Document doc = executeWithRetry(() -> Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT)
                    .ignoreHttpErrors(true)
                    .get());
            
            // 使用CSS选择器提取图片元素
            Elements imgElements = doc.select("div.img_cont img.mimg");
            log.info("找到图片元素数量：{}", imgElements.size());
            
            // 解析图片信息
            for (Element img : imgElements) {
                if (imageList.size() >= count) {
                    break;
                }
                
                try {
                    ImageInfo imageInfo = parseImageElement(img);
                    if (imageInfo != null && StringUtils.hasText(imageInfo.getUrl())) {
                        imageList.add(imageInfo);
                        log.debug("解析图片成功：{}", imageInfo.getUrl());
                    }
                } catch (Exception e) {
                    log.warn("解析图片元素失败：{}", e.getMessage());
                }
            }
            
            log.info("图片搜索完成，关键词：{}，实际获取数量：{}/{}", keyword, imageList.size(), count);
            
        } catch (Exception e) {
            log.error("图片搜索异常，关键词：{}，错误：{}", keyword, e.getMessage(), e);
            throw new RuntimeException("图片搜索失败: " + e.getMessage());
        }
        
        return imageList;
    }

    @Override
    public String cleanImageUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl)) {
            return originalUrl;
        }
        
        // 1. 移除查询参数
        String baseUrl = originalUrl.contains("?") ? 
            originalUrl.substring(0, originalUrl.indexOf("?")) : originalUrl;
        
        // 2. HTTP转HTTPS
        if (baseUrl.startsWith("http://")) {
            baseUrl = baseUrl.replace("http://", "https://");
        }
        
        return baseUrl;
    }

    /**
     * 解析图片元素，提取图片信息
     */
    private ImageInfo parseImageElement(Element imgElement) {
        String originalUrl = imgElement.attr("src");
        if (!StringUtils.hasText(originalUrl)) {
            return null;
        }
        
        // 清理URL
        String cleanUrl = cleanImageUrl(originalUrl);
        
        // 提取标题
        String title = imgElement.attr("alt");
        if (!StringUtils.hasText(title)) {
            title = "搜索图片";
        }
        
        // 提取尺寸信息
        Integer width = null;
        Integer height = null;
        
        try {
            String widthAttr = imgElement.attr("width");
            String heightAttr = imgElement.attr("height");
            
            if (StringUtils.hasText(widthAttr) && StringUtils.hasText(heightAttr)) {
                width = Integer.parseInt(widthAttr);
                height = Integer.parseInt(heightAttr);
            }
        } catch (NumberFormatException e) {
            log.debug("解析图片尺寸失败：{}", e.getMessage());
        }
        
        // 提取文件扩展名
        String extension = extractExtension(cleanUrl);
        
        return ImageInfo.builder()
                .url(cleanUrl)
                .originalUrl(originalUrl)
                .title(title)
                .width(width)
                .height(height)
                .extension(extension)
                .build();
    }

    /**
     * 从URL中提取文件扩展名
     */
    private String extractExtension(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        
        // 从URL路径中提取扩展名
        try {
            String path = url.substring(url.lastIndexOf('/') + 1);
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < path.length() - 1) {
                String ext = path.substring(dotIndex + 1).toLowerCase();
                // 只返回常见的图片格式
                if (ext.matches("jpg|jpeg|png|gif|webp|bmp")) {
                    return ext;
                }
            }
        } catch (Exception e) {
            log.debug("提取文件扩展名失败：{}", e.getMessage());
        }
        
        // 默认返回jpg
        return "jpg";
    }

    /**
     * 带重试的请求执行
     */
    private <T> T executeWithRetry(java.util.concurrent.Callable<T> operation) {
        Exception lastException;
        
        for (int attempt = 1; true; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    log.error("重试次数已达上限，最终失败，尝试次数：{}", attempt);
                    break;
                }
                
                // 计算重试延迟（指数退避）
                long delay = BASE_RETRY_DELAY_MS * (1L << (attempt - 1));
                log.warn("请求失败，第{}次重试，{}秒后重试，错误：{}", attempt, delay / 1000.0, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("重试被中断", ie);
                }
            }
        }
        
        throw new RuntimeException("请求重试失败: " + lastException.getMessage(), lastException);
    }
}
