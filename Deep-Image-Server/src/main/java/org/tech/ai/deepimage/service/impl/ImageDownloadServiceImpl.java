package org.tech.ai.deepimage.service.impl;

import cn.hutool.core.io.IoUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.tech.ai.deepimage.config.ImageDownloadProperties;
import org.tech.ai.deepimage.config.MinioProperties;
import org.tech.ai.deepimage.constant.ImageDownloadConstant;
import org.tech.ai.deepimage.entity.FileRecord;
import org.tech.ai.deepimage.enums.FileStatusEnum;
import org.tech.ai.deepimage.enums.FileVisibilityEnum;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.model.dto.ImageInfo;
import org.tech.ai.deepimage.model.dto.response.DownloadResult;
import org.tech.ai.deepimage.service.FileRecordService;
import org.tech.ai.deepimage.service.FileTagService;
import org.tech.ai.deepimage.service.ImageDownloadService;
import org.tech.ai.deepimage.service.MinioService;
import org.tech.ai.deepimage.util.FileUtil;
import org.tech.ai.deepimage.util.HashUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Image download service implementation class
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageDownloadServiceImpl implements ImageDownloadService {

    private final MinioService minioService;
    private final FileRecordService fileRecordService;
    private final FileTagService fileTagService;
    private final MinioProperties minioProperties;
    private final ImageDownloadProperties downloadProperties;

    @Override
    @Transactional
    public DownloadResult downloadImages(List<ImageInfo> images, Long userId, String keyword, List<Long> tagIds) {
        long startTime = System.currentTimeMillis();

        log.info("Starting batch image download, user: {}, keyword: {}, image count: {}", userId, keyword, images.size());

        DownloadResult.DownloadResultBuilder resultBuilder = DownloadResult.builder()
                .totalCount(images.size());

        // Batch operation: collect all successfully downloaded file records
        List<FileRecord> fileRecordsToSave = new ArrayList<>();
        List<DownloadResult.FailedImageInfo> failedImages = new ArrayList<>();

        // Step 1: Batch download images and prepare file records
        for (ImageInfo imageInfo : images) {
            try {
                log.debug("Starting image download: {}", imageInfo.getUrl());

                // 1. Download image data
                URL url = new URL(imageInfo.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(downloadProperties.getTimeout());
                connection.setReadTimeout(downloadProperties.getTimeout());
                connection.setRequestProperty(ImageDownloadConstant.HEADER_USER_AGENT, downloadProperties.getUserAgent());
                connection.setRequestProperty(ImageDownloadConstant.HEADER_ACCEPT, ImageDownloadConstant.ACCEPT_IMAGE);

                int responseCode = connection.getResponseCode();
                BusinessException.assertTrue(
                        responseCode == HttpURLConnection.HTTP_OK,
                        String.format("HTTP status error: %d", responseCode)
                );

                String contentType = connection.getContentType();
                BusinessException.assertTrue(
                        FileUtil.isImageType(contentType),
                        String.format("Not an image type: %s", contentType)
                );

                int contentLength = connection.getContentLength();
                BusinessException.throwIf(
                        contentLength > downloadProperties.getMaxFileSize(),
                        String.format("File too large: %.2fMB", contentLength / 1024.0 / 1024.0)
                );

                byte[] imageData;
                try (InputStream inputStream = connection.getInputStream()) {
                    imageData = IoUtil.readBytes(inputStream);
                }

                // Assert image data is not null and has content
                BusinessException.assertNotNull(imageData, "Image data is empty");
                BusinessException.throwIf(imageData.length == 0, "Image data length is 0");

                // 2. Generate object name (follow project specification: {userId}/{businessType}/{date}/{uuid}.{extension})
                String extension = StringUtils.hasText(imageInfo.getExtension())
                        ? imageInfo.getExtension()
                        : downloadProperties.getDefaultExtension();

                String objectName = FileUtil.generateObjectName(
                        userId,
                        downloadProperties.getBusinessType()+"/search/"+keyword,
                        extension
                );

                // 3. Upload to MinIO
                String mimeType = FileUtil.getMimeType(extension);
                String fileUrl = minioService.uploadFile(new ByteArrayInputStream(imageData), objectName, mimeType);

                // 4. Create file record
                FileRecord fileRecord = new FileRecord();
                fileRecord.setUserId(userId);
                fileRecord.setOriginalFilename(imageInfo.getTitle() != null ? imageInfo.getTitle() : "search_image");
                fileRecord.setObjectName(objectName);
                fileRecord.setFileSize((long) imageData.length);
                fileRecord.setContentType(mimeType);
                fileRecord.setStatus(FileStatusEnum.COMPLETED.name());
                fileRecord.setVisibility(FileVisibilityEnum.PRIVATE.name());
                fileRecord.setBusinessType(downloadProperties.getBusinessType());
                fileRecord.setFileExtension(extension);
                fileRecord.setBucketName(minioProperties.getBucket());
                fileRecord.setFileUrl(fileUrl);

                // Calculate file hash
                String fileHash = HashUtil.sha256(imageData);
                fileRecord.setFileHash(fileHash);

                // Add file record to batch save list (not saved to database yet)
                fileRecordsToSave.add(fileRecord);
                log.debug("Image download successful, pending save: {}", imageInfo.getUrl());

            } catch (Exception e) {
                failedImages.add(DownloadResult.FailedImageInfo.builder()
                        .url(imageInfo.getUrl())
                        .errorMessage(e.getMessage())
                        .build());
                log.error("Image download exception: {}, error: {}", imageInfo.getUrl(), e.getMessage(), e);
            }
        }

        // Step 2: Batch save file records to database (single operation)
        List<Long> successFileIds = new ArrayList<>();
        if (!fileRecordsToSave.isEmpty()) {
            log.info("Batch saving file records, count: {}", fileRecordsToSave.size());
            boolean savedSuccess = fileRecordService.saveBatch(fileRecordsToSave);

            if (savedSuccess) {
                // Collect successfully saved file IDs
                fileRecordsToSave.forEach(record -> successFileIds.add(record.getId()));
                log.info("File records batch save successful: {} records", successFileIds.size());
                
                // Step 3: If tags are provided, batch set tags
                if (tagIds != null && !tagIds.isEmpty()) {
                    log.info("Batch setting tags, file count: {}, tag count: {}", successFileIds.size(), tagIds.size());
                    for (Long fileId : successFileIds) {
                        try {
                            fileTagService.batchSetFileTags(fileId, userId, tagIds);
                        } catch (Exception e) {
                            log.error("Failed to set file tags, fileId: {}, error: {}", fileId, e.getMessage(), e);
                        }
                    }
                }
            } else {
                log.error("File records batch save failed");
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTimeSeconds = (endTime - startTime) / ImageDownloadConstant.MILLIS_TO_SECONDS;

        // Determine status
        String status;
        if (failedImages.isEmpty()) {
            status = ImageDownloadConstant.STATUS_COMPLETED;
        } else if (successFileIds.isEmpty()) {
            status = ImageDownloadConstant.STATUS_FAILED;
        } else {
            status = ImageDownloadConstant.STATUS_PARTIAL;
        }

        DownloadResult result = resultBuilder
                .status(status)
                .successCount(successFileIds.size())
                .failedCount(failedImages.size())
                .totalTimeSeconds(totalTimeSeconds)
                .downloadedFileIds(successFileIds)
                .failedImages(failedImages)
                .build();

        log.info("Batch download completed, user: {}, success: {}/{}, elapsed time: {} seconds",
                userId, successFileIds.size(), images.size(), totalTimeSeconds);

        return result;
    }
}
