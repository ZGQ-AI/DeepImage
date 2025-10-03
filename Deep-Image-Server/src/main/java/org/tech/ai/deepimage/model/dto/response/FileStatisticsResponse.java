package org.tech.ai.deepimage.model.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件统计响应
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Builder
public class FileStatisticsResponse {
    
    /**
     * 总文件数
     */
    private Long totalFiles;
    
    /**
     * 总存储大小（字节）
     */
    private Long totalSize;
    
    /**
     * 按业务类型统计
     */
    private Long imageCount;
    private Long documentCount;
    private Long videoCount;
    private Long avatarCount;
    private Long tempCount;
    
    /**
     * 分享统计
     */
    private Long shareOutCount;  // 分享出去的文件数
    private Long shareInCount;   // 收到的分享数
    
    /**
     * 访问统计
     */
    private Long totalDownloads;  // 总下载次数
    private Long totalViews;      // 总查看次数
    private Long totalUploads;    // 总上传次数
    
    /**
     * 最近上传时间
     */
    private LocalDateTime lastUploadedAt;
}

