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
 * 图片下载服务实现类
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

        log.info("开始批量下载图片，用户：{}，关键词：{}，图片数量：{}", userId, keyword, images.size());

        DownloadResult.DownloadResultBuilder resultBuilder = DownloadResult.builder()
                .totalCount(images.size());

        // 批量操作：收集所有成功下载的文件记录
        List<FileRecord> fileRecordsToSave = new ArrayList<>();
        List<DownloadResult.FailedImageInfo> failedImages = new ArrayList<>();

        // 第一步：批量下载图片并准备文件记录
        for (ImageInfo imageInfo : images) {
            try {
                log.debug("开始下载图片：{}", imageInfo.getUrl());

                // 1. 下载图片数据
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
                        String.format("HTTP状态异常：%d", responseCode)
                );

                String contentType = connection.getContentType();
                BusinessException.assertTrue(
                        FileUtil.isImageType(contentType),
                        String.format("不是图片类型：%s", contentType)
                );

                int contentLength = connection.getContentLength();
                BusinessException.throwIf(
                        contentLength > downloadProperties.getMaxFileSize(),
                        String.format("文件过大：%.2fMB", contentLength / 1024.0 / 1024.0)
                );

                byte[] imageData;
                try (InputStream inputStream = connection.getInputStream()) {
                    imageData = IoUtil.readBytes(inputStream);
                }

                // 断言图片数据不为null且有内容
                BusinessException.assertNotNull(imageData, "图片数据为空");
                BusinessException.throwIf(imageData.length == 0, "图片数据长度为0");

                // 2. 生成对象名（遵循项目规范：{userId}/{businessType}/{date}/{uuid}.{extension}）
                String extension = StringUtils.hasText(imageInfo.getExtension())
                        ? imageInfo.getExtension()
                        : downloadProperties.getDefaultExtension();

                String objectName = FileUtil.generateObjectName(
                        userId,
                        downloadProperties.getBusinessType(),
                        extension
                );

                // 3. 上传到 MinIO
                String mimeType = FileUtil.getMimeType(extension);
                String fileUrl = minioService.uploadFile(new ByteArrayInputStream(imageData), objectName, mimeType);

                // 4. 创建文件记录
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

                // 计算文件哈希
                String fileHash = HashUtil.sha256(imageData);
                fileRecord.setFileHash(fileHash);

                // 将文件记录添加到批量保存列表（暂不保存到数据库）
                fileRecordsToSave.add(fileRecord);
                log.debug("图片下载成功，待保存：{}", imageInfo.getUrl());

            } catch (Exception e) {
                failedImages.add(DownloadResult.FailedImageInfo.builder()
                        .url(imageInfo.getUrl())
                        .errorMessage(e.getMessage())
                        .build());
                log.error("图片下载异常：{}，错误：{}", imageInfo.getUrl(), e.getMessage(), e);
            }
        }

        // 第二步：批量保存文件记录到数据库（一次性操作）
        List<Long> successFileIds = new ArrayList<>();
        if (!fileRecordsToSave.isEmpty()) {
            log.info("批量保存文件记录，数量：{}", fileRecordsToSave.size());
            boolean savedSuccess = fileRecordService.saveBatch(fileRecordsToSave);

            if (savedSuccess) {
                // 收集成功保存的文件ID
                fileRecordsToSave.forEach(record -> successFileIds.add(record.getId()));
                log.info("文件记录批量保存成功：{} 条", successFileIds.size());
                
                // 第三步：如果提供了标签，批量设置标签
                if (tagIds != null && !tagIds.isEmpty()) {
                    log.info("批量设置标签，文件数：{}，标签数：{}", successFileIds.size(), tagIds.size());
                    for (Long fileId : successFileIds) {
                        try {
                            fileTagService.batchSetFileTags(fileId, userId, tagIds);
                        } catch (Exception e) {
                            log.error("设置文件标签失败，fileId: {}，错误：{}", fileId, e.getMessage(), e);
                        }
                    }
                }
            } else {
                log.error("文件记录批量保存失败");
            }
        }

        long endTime = System.currentTimeMillis();
        long totalTimeSeconds = (endTime - startTime) / ImageDownloadConstant.MILLIS_TO_SECONDS;

        // 确定状态
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

        log.info("批量下载完成，用户：{}，成功：{}/{}, 耗时：{}秒",
                userId, successFileIds.size(), images.size(), totalTimeSeconds);

        return result;
    }
}
