package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 图片搜索响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchImageResponse {

    /**
     * 任务ID，用于查询进度
     */
    private String taskId;

    /**
     * 任务状态：searching(搜索中), downloading(下载中), completed(完成), failed(失败)
     */
    private String status;

    /**
     * 下载进度信息
     */
    private ProgressInfo progress;

    /**
     * 最终结果信息
     */
    private ResultInfo result;

    /**
     * 进度信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInfo {
        /**
         * 总数量
         */
        private Integer total;

        /**
         * 已完成数量
         */
        private Integer completed;

        /**
         * 失败数量
         */
        private Integer failed;

        /**
         * 完成百分比 (0-100)
         */
        private Integer percentage;

        /**
         * 预估剩余时间（秒）
         */
        private Long estimatedTimeRemaining;
    }

    /**
     * 结果信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultInfo {
        /**
         * 成功下载的图片数量
         */
        private Integer successCount;

        /**
         * 失败的图片数量
         */
        private Integer failedCount;

        /**
         * 总耗时（秒）
         */
        private Long totalTimeSeconds;

        /**
         * 成功下载的图片ID列表
         */
        private List<Long> downloadedFileIds;

        /**
         * 失败的图片信息
         */
        private List<FailedImageInfo> failedImages;
    }

    /**
     * 失败图片信息
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedImageInfo {
        /**
         * 图片URL
         */
        private String url;

        /**
         * 失败原因
         */
        private String errorMessage;
    }
}
