package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 同步下载结果DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadResult {

    /**
     * 下载状态
     */
    private String status; // "completed", "partial", "failed"
    
    /**
     * 成功下载数量
     */
    private int successCount;
    
    /**
     * 失败下载数量
     */
    private int failedCount;
    
    /**
     * 总数量
     */
    private int totalCount;
    
    /**
     * 耗时（秒）
     */
    private long totalTimeSeconds;
    
    /**
     * 成功下载的文件ID列表
     */
    @Builder.Default
    private List<Long> downloadedFileIds = new ArrayList<>();
    
    /**
     * 失败图片信息
     */
    @Builder.Default
    private List<FailedImageInfo> failedImages = new ArrayList<>();
    
    /**
     * 失败图片信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedImageInfo {
        private String url;
        private String errorMessage;
    }
}
