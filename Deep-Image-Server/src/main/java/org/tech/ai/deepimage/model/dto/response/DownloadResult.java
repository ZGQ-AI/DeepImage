package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Synchronous download result DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DownloadResult {

    /**
     * Download status
     */
    private String status; // "completed", "partial", "failed"
    
    /**
     * Successfully downloaded count
     */
    private int successCount;
    
    /**
     * Failed download count
     */
    private int failedCount;
    
    /**
     * Total count
     */
    private int totalCount;
    
    /**
     * Elapsed time (seconds)
     */
    private long totalTimeSeconds;
    
    /**
     * Successfully downloaded file ID list
     */
    @Builder.Default
    private List<Long> downloadedFileIds = new ArrayList<>();
    
    /**
     * Failed image information
     */
    @Builder.Default
    private List<FailedImageInfo> failedImages = new ArrayList<>();
    
    /**
     * Failed image information
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
