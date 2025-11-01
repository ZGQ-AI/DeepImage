package org.tech.ai.deepimage.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Image search response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchImageResponse {

    /**
     * Task ID, used for querying progress
     */
    private String taskId;

    /**
     * Task status: searching, downloading, completed, failed
     */
    private String status;

    /**
     * Download progress information
     */
    private ProgressInfo progress;

    /**
     * Final result information
     */
    private ResultInfo result;

    /**
     * Progress information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProgressInfo {
        /**
         * Total count
         */
        private Integer total;

        /**
         * Completed count
         */
        private Integer completed;

        /**
         * Failed count
         */
        private Integer failed;

        /**
         * Completion percentage (0-100)
         */
        private Integer percentage;

        /**
         * Estimated remaining time (seconds)
         */
        private Long estimatedTimeRemaining;
    }

    /**
     * Result information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultInfo {
        /**
         * Successfully downloaded image count
         */
        private Integer successCount;

        /**
         * Failed image count
         */
        private Integer failedCount;

        /**
         * Total elapsed time (seconds)
         */
        private Long totalTimeSeconds;

        /**
         * Successfully downloaded image ID list
         */
        private List<Long> downloadedFileIds;

        /**
         * Failed image information
         */
        private List<FailedImageInfo> failedImages;
    }

    /**
     * Failed image information
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FailedImageInfo {
        /**
         * Image URL
         */
        private String url;

        /**
         * Failure reason
         */
        private String errorMessage;
    }
}
