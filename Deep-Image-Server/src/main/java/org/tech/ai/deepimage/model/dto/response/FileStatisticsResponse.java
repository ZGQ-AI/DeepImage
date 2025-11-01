package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * File statistics response
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileStatisticsResponse {
    
    /**
     * Total file count
     */
    private Long totalFiles;
    
    /**
     * Total storage size (bytes)
     */
    private Long totalSize;
    
    /**
     * Statistics by business type
     */
    private Long imageCount;
    private Long documentCount;
    private Long videoCount;
    private Long avatarCount;
    private Long tempCount;
    
    /**
     * Share statistics
     */
    private Long shareOutCount;  // Number of files shared out
    private Long shareInCount;   // Number of shares received
    
    /**
     * Access statistics
     */
    private Long totalDownloads;  // Total download count
    private Long totalViews;      // Total view count
    private Long totalUploads;    // Total upload count
    
    /**
     * Last upload time
     */
    private LocalDateTime lastUploadedAt;
}

