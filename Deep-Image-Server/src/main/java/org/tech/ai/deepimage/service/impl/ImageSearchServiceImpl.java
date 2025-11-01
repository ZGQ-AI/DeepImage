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
 * Image search service implementation
 */
@Slf4j
@Service
public class ImageSearchServiceImpl implements ImageSearchService {

    private static final String BING_SEARCH_URL = "https://cn.bing.com/images/async";
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";
    private static final int REQUEST_TIMEOUT = 30000; // 30 seconds timeout
    
    // Retry configuration
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long BASE_RETRY_DELAY_MS = 1000; // Base retry delay 1 second


    @Override
    public List<ImageInfo> searchImages(String keyword, int count) {
        List<ImageInfo> imageList = new ArrayList<>();
        
        try {
            log.info("Starting image search, keyword: {}, count: {}", keyword, count);
            
            // Build search URL
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = BING_SEARCH_URL + "?q=" + encodedKeyword + "&mmasync=1";
            
            // HTTP request with retry
            Document doc = executeWithRetry(() -> Jsoup.connect(searchUrl)
                    .userAgent(USER_AGENT)
                    .timeout(REQUEST_TIMEOUT)
                    .ignoreHttpErrors(true)
                    .get());
            
            // Extract image elements using CSS selector
            Elements imgElements = doc.select("div.img_cont img.mimg");
            log.info("Found image elements count: {}", imgElements.size());
            
            // Parse image information
            for (Element img : imgElements) {
                if (imageList.size() >= count) {
                    break;
                }
                
                try {
                    ImageInfo imageInfo = parseImageElement(img);
                    if (imageInfo != null && StringUtils.hasText(imageInfo.getUrl())) {
                        imageList.add(imageInfo);
                        log.debug("Parsed image successfully: {}", imageInfo.getUrl());
                    }
                } catch (Exception e) {
                    log.warn("Failed to parse image element: {}", e.getMessage());
                }
            }
            
            log.info("Image search completed, keyword: {}, actual count: {}/{}", keyword, imageList.size(), count);
            
        } catch (Exception e) {
            log.error("Image search exception, keyword: {}, error: {}", keyword, e.getMessage(), e);
            throw new RuntimeException("Image search failed: " + e.getMessage());
        }
        
        return imageList;
    }

    @Override
    public String cleanImageUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl)) {
            return originalUrl;
        }
        
        // 1. Remove query parameters
        String baseUrl = originalUrl.contains("?") ? 
            originalUrl.substring(0, originalUrl.indexOf("?")) : originalUrl;
        
        // 2. Convert HTTP to HTTPS
        if (baseUrl.startsWith("http://")) {
            baseUrl = baseUrl.replace("http://", "https://");
        }
        
        return baseUrl;
    }

    /**
     * Parse image element, extract image information
     */
    private ImageInfo parseImageElement(Element imgElement) {
        String originalUrl = imgElement.attr("src");
        if (!StringUtils.hasText(originalUrl)) {
            return null;
        }
        
        // Clean URL
        String cleanUrl = cleanImageUrl(originalUrl);
        
        // Extract title
        String title = imgElement.attr("alt");
        if (!StringUtils.hasText(title)) {
            title = "Search Image";
        }
        
        // Extract dimension information
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
            log.debug("Failed to parse image dimensions: {}", e.getMessage());
        }
        
        // Extract file extension
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
     * Extract file extension from URL
     */
    private String extractExtension(String url) {
        if (!StringUtils.hasText(url)) {
            return null;
        }
        
        // Extract extension from URL path
        try {
            String path = url.substring(url.lastIndexOf('/') + 1);
            int dotIndex = path.lastIndexOf('.');
            if (dotIndex > 0 && dotIndex < path.length() - 1) {
                String ext = path.substring(dotIndex + 1).toLowerCase();
                // Only return common image formats
                if (ext.matches("jpg|jpeg|png|gif|webp|bmp")) {
                    return ext;
                }
            }
        } catch (Exception e) {
            log.debug("Failed to extract file extension: {}", e.getMessage());
        }
        
        // Default return jpg
        return "jpg";
    }

    /**
     * Execute request with retry
     */
    private <T> T executeWithRetry(java.util.concurrent.Callable<T> operation) {
        Exception lastException;
        
        for (int attempt = 1; true; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                
                if (attempt == MAX_RETRY_ATTEMPTS) {
                    log.error("Retry limit reached, final failure, attempts: {}", attempt);
                    break;
                }
                
                // Calculate retry delay (exponential backoff)
                long delay = BASE_RETRY_DELAY_MS * (1L << (attempt - 1));
                log.warn("Request failed, retry attempt {}, retry after {} seconds, error: {}", attempt, delay / 1000.0, e.getMessage());
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrupted", ie);
                }
            }
        }
        
        throw new RuntimeException("Request retry failed: " + lastException.getMessage(), lastException);
    }
}
