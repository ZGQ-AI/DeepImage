package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.tech.ai.deepimage.config.MinioProperties;
import org.tech.ai.deepimage.constant.FileConstant;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.entity.*;
import org.tech.ai.deepimage.enums.*;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.mapper.FileRecordMapper;
import org.tech.ai.deepimage.mapper.FileTagMapper;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;
import org.tech.ai.deepimage.service.*;
import org.tech.ai.deepimage.util.FileUtil;
import org.tech.ai.deepimage.util.HashUtil;
import org.tech.ai.deepimage.util.HttpRequestUtil;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * File management Service implementation
 *
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    // MinIO related services
    private final MinioService minioService;
    private final MinioProperties minioProperties;

    private final FileTagService fileTagService;
    private final FileTagMapper fileTagMapper;
    private final TagService tagService;
    private final FileShareService fileShareService;
    private final FileAccessLogService fileAccessLogService;
    private final UserService userService;
    private final FileRecordService fileRecordService;

    // ========== File Upload ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse uploadFile(UploadFileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        MultipartFile file = request.getFile();

        log.info("Starting file upload: userId={}, filename={}, size={}, businessType={}",
                userId, file.getOriginalFilename(), file.getSize(), request.getBusinessType());

        try {
            // 1. Validate business type
            if (!BusinessTypeEnum.isValid(request.getBusinessType())) {
                throw BusinessException.badRequest(ResponseConstant.FILE_TYPE_INVALID_MESSAGE);
            }

            // 2. Validate file size
            if (file.getSize() > FileConstant.MAX_FILE_SIZE) {
                throw BusinessException.badRequest(ResponseConstant.FILE_SIZE_EXCEEDED_MESSAGE);
            }

            // 3. Read file content into memory (avoid InputStream reuse issues)
            byte[] fileBytes = file.getBytes();

            // 4. Calculate file hash
            String fileHash = HashUtil.sha256(fileBytes);

            // 5. Check if file already exists (global deduplication)
            FileRecord existingFile = checkExistingFile(fileHash);
            if (existingFile != null) {
                log.info("File already exists, reusing existing file: existingFileId={}, currentUserId={}",
                        existingFile.getId(), userId);
                // Reuse existing file, create new record for current user
                return buildUploadResponse(existingFile);
            }

            // 6. Generate object name
            String objectName = FileUtil.generateObjectName(userId, request.getBusinessType(),
                    FileUtil.getFileExtension(file.getOriginalFilename()));

            // 7. Upload to MinIO (create new InputStream from byte array)
            String fileUrl = minioService.uploadFile(
                    new ByteArrayInputStream(fileBytes), objectName, file.getContentType());

            // 8. Save file record
            FileRecord fileRecord = buildFileRecord(userId, file, objectName, fileUrl, fileHash, request);
            save(fileRecord);

            // 9. Associate tags (using FileTagService)
            if (CollectionUtils.isNotEmpty(request.getTagIds())) {
                fileTagService.batchSetFileTags(fileRecord.getId(), userId, request.getTagIds());
            }

            // 10. Log access
            logFileAccess(fileRecord.getId(), userId, AccessTypeEnum.UPLOAD.name());

            log.info("File upload successful: fileId={}", fileRecord.getId());
            return buildUploadResponse(fileRecord);

        } catch (Exception e) {
            log.error("File upload failed: userId={}, filename={}", userId, file.getOriginalFilename(), e);
            throw BusinessException.badRequest(ResponseConstant.FILE_UPLOAD_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    @Override
    public FileExistsResponse checkFileExists(FileExistsCheckRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query globally for files with the same hash
        FileRecord existingFile = checkExistingFile(request.getFileHash());

        if (existingFile != null) {
            log.info("File already exists: fileHash={}, userId={}", request.getFileHash(), userId);
            return FileExistsResponse.builder()
                    .exists(true)
                    .fileId(existingFile.getId())
                    .fileUrl(existingFile.getFileUrl())
                    .message(ResponseConstant.FILE_ALREADY_EXISTS_MESSAGE)
                    .build();
        }

        return FileExistsResponse.builder()
                .exists(false)
                .message(ResponseConstant.FILE_NOT_EXISTS_MESSAGE)
                .build();
    }

    // ========== File Query ==========

    @Override
    public Page<FileInfoResponse> listFiles(ListFilesRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // If tagId is specified, query associated file IDs first
        List<Long> fileIds = null;
        if (request.getTagId() != null) {
            // Validate tag permissions
            Tag userTag = tagService.getUserTag(request.getTagId(), userId);
            BusinessException.assertNotNull(userTag, ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

            // Query all file IDs associated with this tag
            LambdaQueryWrapper<FileTag> fileTagWrapper = new LambdaQueryWrapper<>();
            fileTagWrapper.eq(FileTag::getTagId, request.getTagId());
            List<FileTag> fileTags = fileTagService.list(fileTagWrapper);

            if (fileTags.isEmpty()) {
                // If no associated files, return empty result directly
                return new Page<>(request.getPage(), request.getPageSize(), 0);
            }

            fileIds = fileTags.stream()
                    .map(FileTag::getFileId)
                    .collect(Collectors.toList());
        }

        // Build query conditions
        Page<FileRecord> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUserId, userId);

        // If tag filter is applied, add fileIds condition
        if (fileIds != null) {
            wrapper.in(FileRecord::getId, fileIds);
        }

        // Business type filter
        if (StringUtils.isNotBlank(request.getBusinessType())) {
            wrapper.eq(FileRecord::getBusinessType, request.getBusinessType());
        }

        // Filename search
        if (StringUtils.isNotBlank(request.getFilename())) {
            wrapper.like(FileRecord::getOriginalFilename, request.getFilename());
        }

        // Sort
        if (FileConstant.SORT_BY_FILE_SIZE.equals(request.getSortBy())) {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getFileSize);
        } else if (FileConstant.SORT_BY_FILENAME.equals(request.getSortBy())) {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getOriginalFilename);
        } else if (FileConstant.SORT_BY_UPDATED_AT.equals(request.getSortBy())) {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getUpdatedAt);
        } else {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getCreatedAt);
        }
        
        // Always add secondary sort by updatedAt DESC (latest updated first)
        wrapper.orderByDesc(FileRecord::getUpdatedAt);

        Page<FileRecord> recordPage = page(page, wrapper);

        // Convert to response objects
        Page<FileInfoResponse> responsePage = new Page<>(recordPage.getCurrent(), recordPage.getSize(),
                recordPage.getTotal());
        responsePage.setRecords(recordPage.getRecords().stream()
                .map(this::buildFileInfoResponse)
                .collect(Collectors.toList()));

        return responsePage;
    }

    @Override
    public Page<FileInfoResponse> listPublicFiles(ListPublicFilesRequest request) {
        // Build query conditions
        Page<FileRecord> pageObj = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        
        // Only query PUBLIC visibility files
        wrapper.eq(FileRecord::getVisibility, FileVisibilityEnum.PUBLIC.name());
        
        // Only query image type files (business_type = 'image')
        wrapper.eq(FileRecord::getBusinessType, BusinessTypeEnum.IMAGE.name());
        
        // Note: @TableLogic will automatically filter out deleted files (deleteFlag = 1)

        // Default sort: created_at DESC (newest first)
        wrapper.orderByDesc(FileRecord::getCreatedAt);

        Page<FileRecord> recordPage = page(pageObj, wrapper);

        // Convert to response objects
        Page<FileInfoResponse> responsePage = new Page<>(recordPage.getCurrent(), recordPage.getSize(),
                recordPage.getTotal());
        responsePage.setRecords(recordPage.getRecords().stream()
                .map(this::buildFileInfoResponse)
                .collect(Collectors.toList()));

        return responsePage;
    }

    @Override
    public FileDetailResponse getFileDetail(Long fileId) {
        return getFileDetail(fileId, false);
    }
    
    @Override
    public FileDetailResponse getFileDetail(Long fileId, Boolean filterSensitive) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query file record
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // Permission check
        checkFilePermission(fileRecord, userId);

        // Build detail response
        return buildFileDetailResponse(fileRecord, filterSensitive != null && filterSensitive);
    }

    // ========== File Download ==========

    @Override
    public InputStream downloadFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query file record
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // Permission check
        checkFilePermission(fileRecord, userId);

        // Log access
        logFileAccess(fileId, userId, AccessTypeEnum.DOWNLOAD.name());

        // Download from MinIO
        return minioService.downloadFile(fileRecord.getObjectName());
    }

    @Override
    public FilePreviewResponse getPreviewUrl(Long fileId, Integer expirySeconds) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query file record
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // Default expiry time
        if (expirySeconds == null || expirySeconds <= 0) {
            expirySeconds = FileConstant.DEFAULT_PREVIEW_EXPIRY_SECONDS;
        }

        // If user is file owner, directly pass permission check
        if (fileRecord.getUserId().equals(userId)) {
            // File owner is not restricted by share time limit, directly generate presigned URL
            String previewUrl = minioService.getPresignedDownloadUrl(fileRecord.getObjectName(), expirySeconds);
            logFileAccess(fileId, userId, AccessTypeEnum.PREVIEW.name());

            return FilePreviewResponse.builder()
                    .previewUrl(previewUrl)
                    .expirySeconds(expirySeconds)
                    .expiresAt(LocalDateTime.now().plusSeconds(expirySeconds))
                    .build();
        }

        // If not file owner, check share permissions and time restrictions
        FileShare fileShare = fileShareService.getValidShareByFileAndUser(fileId, userId);

        // If no share record found, access denied
        BusinessException.assertNotNull(fileShare, ResponseConstant.FILE_ACCESS_DENIED_MESSAGE);

        // Check if share has expired
        LocalDateTime now = LocalDateTime.now();
        if (fileShare.getExpiresAt() != null) {
            BusinessException.throwIf(fileShare.getExpiresAt().isBefore(now) || fileShare.getExpiresAt().isEqual(now),
                    ResponseConstant.FORBIDDEN, ResponseConstant.SHARE_EXPIRED_MESSAGE);

            // Calculate remaining valid time of share (in seconds)
            long remainingSeconds = java.time.Duration.between(now, fileShare.getExpiresAt()).getSeconds();

            // Preview URL expiry cannot exceed remaining share expiry
            if (expirySeconds > remainingSeconds) {
                expirySeconds = (int) Math.max(60, remainingSeconds); // Ensure at least 60 seconds
                log.info("Preview URL expiry adjusted due to share limit: {} seconds", expirySeconds);
            }
        }

        // Generate presigned URL
        String previewUrl = minioService.getPresignedDownloadUrl(fileRecord.getObjectName(), expirySeconds);

        // Log access
        logFileAccess(fileId, userId, AccessTypeEnum.PREVIEW.name());

        return FilePreviewResponse.builder()
                .previewUrl(previewUrl)
                .expirySeconds(expirySeconds)
                .expiresAt(LocalDateTime.now().plusSeconds(expirySeconds))
                .build();
    }

    // ========== File Management ==========

    @Override
    public FileInfoResponse updateFileProperties(UpdateFilePropertiesRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // Validate at least one property is provided
        BusinessException.assertTrue(request.hasAnyPropertyToUpdate(),
                ResponseConstant.PARAM_ERROR, "至少需要提供一个要更新的字段");

        // Query file record
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // Permission check
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // Validate visibility value if provided
        if (request.getVisibility() != null && !request.getVisibility().trim().isEmpty()) {
            BusinessException.assertTrue(
                    request.getVisibility().equals(FileVisibilityEnum.PRIVATE.name()) ||
                    request.getVisibility().equals(FileVisibilityEnum.PUBLIC.name()),
                    ResponseConstant.PARAM_ERROR, "可见性值必须是 PRIVATE 或 PUBLIC");
        }

        // Update properties
        boolean hasChanges = false;
        if (request.getOriginalFilename() != null && !request.getOriginalFilename().trim().isEmpty()) {
            fileRecord.setOriginalFilename(request.getOriginalFilename().trim());
            hasChanges = true;
        }
        
        if (request.getVisibility() != null && !request.getVisibility().trim().isEmpty()) {
            fileRecord.setVisibility(request.getVisibility());
            hasChanges = true;
        }

        if (hasChanges) {
            fileRecord.setUpdatedAt(LocalDateTime.now());
            updateById(fileRecord);
            log.info("File properties updated: fileId={}, filename={}, visibility={}", 
                    fileId, request.getOriginalFilename(), request.getVisibility());
        }

        return buildFileInfoResponse(fileRecord);
    }

    @Override
    public Boolean deleteFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query file record
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // Permission check
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        LambdaUpdateWrapper<FileRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FileRecord::getId, fileId)
                .set(FileRecord::getStatus, FileStatusEnum.DELETED.name())
                .set(FileRecord::getDeleteFlag, DeleteStatusEnum.DELETED.getValue());

        update(updateWrapper);

        log.info("File deleted successfully (soft delete): fileId={}", fileId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse batchDeleteFiles(BatchOperationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> fileIds = request.getFileIds();

        log.info("Batch delete files: userId={}, fileIds={}", userId, fileIds);

        // Batch update directly, database level automatically filters user permissions
        LambdaUpdateWrapper<FileRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(FileRecord::getId, fileIds)
                .eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getDeleteFlag, DeleteStatusEnum.NOT_DELETED.getValue())
                .set(FileRecord::getDeleteFlag, DeleteStatusEnum.DELETED.getValue())
                .set(FileRecord::getStatus, FileStatusEnum.DELETED.name());
        int updatedCount = fileRecordService.getBaseMapper().update(updateWrapper);

        int failedCount = fileIds.size() - updatedCount;

        log.info("Batch delete completed: total={}, success={}, failed={}",
                fileIds.size(), updatedCount, failedCount);

        return BatchOperationResponse.builder()
                .total(fileIds.size())
                .success(updatedCount)
                .failed(failedCount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean permanentDeleteFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query file record
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // Permission check
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // Check reference count
        BusinessException.assertFalse(fileRecord.getReferenceCount() != null && fileRecord.getReferenceCount() > 0,
                ResponseConstant.FILE_BEING_REFERENCED_MESSAGE);

        // Delete from MinIO
        try {
            minioService.deleteFile(fileRecord.getObjectName());
        } catch (Exception e) {
            log.error("Failed to delete file from MinIO: fileId={}, objectName={}", fileId, fileRecord.getObjectName(), e);
        }

        // Delete database record
        removeById(fileId);

        // Delete associated data (using FileTagService) and decrease tag usage count
        Set<Long> deletedTagIds = fileTagService.deleteAllByFileId(fileId);
        if (CollectionUtils.isNotEmpty(deletedTagIds)) {
            tagService.batchDecreaseUsageCount(deletedTagIds);
        }

        log.info("File permanently deleted: fileId={}", fileId);
        return true;
    }

    // ========== File Tags ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TagResponse> addFileTags(AddFileTagsRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // Validate file permissions
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // Batch set tags (using FileTagService)
        fileTagService.batchSetFileTags(fileId, userId, request.getTagIds());

        // Return all tags of the file
        return fileTagService.getFileTagsResponse(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeFileTag(Long fileId, Long tagId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Validate file permissions
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // Delete association (using FileTagService)
        LambdaQueryWrapper<FileTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileTag::getFileId, fileId)
                .eq(FileTag::getTagId, tagId);
        fileTagService.remove(wrapper);

        // Decrease tag usage count (using TagService)
        tagService.batchDecreaseUsageCount(Set.of(tagId));

        log.info("File tag removed successfully: fileId={}, tagId={}", fileId, tagId);
        return true;
    }

    @Override
    public List<TagResponse> getFileTags(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query file record and validate permissions
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        checkFilePermission(fileRecord, userId);

        // Use FileTagService to get file tags
        return fileTagService.getFileTagsResponse(fileId);
    }

    // ========== File Sharing ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileShareResponse createFileShare(CreateFileShareRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // Validate file permissions
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.SHARE_PERMISSION_DENIED_MESSAGE);

        // Validate target user
        User targetUser = userService.getById(request.getShareToUserId());
        BusinessException.assertNotNull(targetUser, ResponseConstant.TARGET_USER_NOT_FOUND_MESSAGE);

        // Validate share type and expiry time
        BusinessException.assertFalse(ShareTypeEnum.TEMPORARY.name().equals(request.getShareType())
                        && request.getExpiresAt() == null,
                ResponseConstant.TEMPORARY_SHARE_REQUIRES_EXPIRY_MESSAGE);

        // Check if share already exists
        LambdaQueryWrapper<FileShare> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FileShare::getFileId, fileId)
                .eq(FileShare::getShareToUserId, request.getShareToUserId())
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue());
        BusinessException.throwIf(fileShareService.count(checkWrapper) > 0,
                ResponseConstant.PARAM_ERROR, ResponseConstant.SHARE_ALREADY_EXISTS_MESSAGE);

        // Create share record
        FileShare fileShare = new FileShare();
        fileShare.setFileId(fileId);
        fileShare.setShareFromUserId(userId);
        fileShare.setShareToUserId(request.getShareToUserId());
        fileShare.setShareType(request.getShareType());
        fileShare.setExpiresAt(request.getExpiresAt());
        fileShare.setPermissionLevel(request.getPermissionLevel());
        fileShare.setMessage(request.getMessage());
        // Database default values: revoked=0, view_count=0, download_count=0, created_at=now, updated_at=now
        fileShareService.save(fileShare);

        // Update file visibility
        if (FileVisibilityEnum.PRIVATE.name().equals(fileRecord.getVisibility())) {
            fileRecord.setVisibility(FileVisibilityEnum.SHARED.name());
            updateById(fileRecord);
        }

        log.info("File share created successfully: shareId={}, fileId={}, toUserId={}",
                fileShare.getId(), fileId, request.getShareToUserId());

        return buildFileShareResponse(fileShare);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelFileShare(Long shareId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query share record
        FileShare fileShare = fileShareService.getById(shareId);
        BusinessException.assertNotNull(fileShare, ResponseConstant.SHARE_NOT_FOUND_MESSAGE);

        // Permission check
        BusinessException.assertTrue(fileShare.getShareFromUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.CANCEL_SHARE_PERMISSION_DENIED_MESSAGE);

        // Revoke share
        fileShare.setRevoked(RevokedStatusEnum.REVOKED.getValue());
        fileShareService.updateById(fileShare);

        // Check if file has other valid shares
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getFileId, fileShare.getFileId())
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue());
        if (fileShareService.count(wrapper) == 0) {
            // No other shares, update file visibility to private
            FileRecord fileRecord = getById(fileShare.getFileId());
            if (fileRecord != null) {
                fileRecord.setVisibility(FileVisibilityEnum.PRIVATE.name());
                updateById(fileRecord);
            }
        }

        log.info("File share cancelled successfully: shareId={}", shareId);
        return true;
    }

    @Override
    public Page<FileShareResponse> listOutgoingShares(Integer page, Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();

        Page<FileShare> sharePage = new Page<>(page, size);
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareFromUserId, userId)
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue())
                .orderByDesc(FileShare::getCreatedAt);

        fileShareService.page(sharePage, wrapper);

        Page<FileShareResponse> responsePage = new Page<>(sharePage.getCurrent(), sharePage.getSize(),
                sharePage.getTotal());
        responsePage.setRecords(sharePage.getRecords().stream()
                .map(this::buildFileShareResponse)
                .collect(Collectors.toList()));

        return responsePage;
    }

    @Override
    public Page<FileShareResponse> listIncomingShares(Integer page, Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();

        Page<FileShare> sharePage = new Page<>(page, size);
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getShareToUserId, userId)
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue())
                .and(w -> w.isNull(FileShare::getExpiresAt)
                        .or()
                        .gt(FileShare::getExpiresAt, LocalDateTime.now()))
                .orderByDesc(FileShare::getCreatedAt);

        fileShareService.page(sharePage, wrapper);

        Page<FileShareResponse> responsePage = new Page<>(sharePage.getCurrent(), sharePage.getSize(),
                sharePage.getTotal());
        responsePage.setRecords(sharePage.getRecords().stream()
                .map(this::buildFileShareResponse)
                .collect(Collectors.toList()));

        return responsePage;
    }

    @Override
    public FileShareResponse getShareDetail(Long shareId) {
        Long userId = StpUtil.getLoginIdAsLong();

        FileShare fileShare = fileShareService.getById(shareId);
        BusinessException.assertNotNull(fileShare, ResponseConstant.SHARE_NOT_FOUND_MESSAGE);

        // Permission check (must be sharer or recipient)
        BusinessException.assertTrue(
                fileShare.getShareFromUserId().equals(userId) || fileShare.getShareToUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.VIEW_SHARE_PERMISSION_DENIED_MESSAGE);

        return buildFileShareResponse(fileShare);
    }

    // ========== Access Logs ==========

    @Override
    public Page<FileAccessLogResponse> getFileAccessLogs(GetFileAccessLogsRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // Validate permissions (must be file owner)
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.VIEW_ACCESS_LOG_PERMISSION_DENIED_MESSAGE);

        // Query access logs
        Page<FileAccessLog> logPage = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<FileAccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileAccessLog::getFileId, fileId)
                .orderByDesc(FileAccessLog::getCreatedAt);

        fileAccessLogService.page(logPage, wrapper);

        // Convert response
        Page<FileAccessLogResponse> responsePage = new Page<>(logPage.getCurrent(), logPage.getSize(),
                logPage.getTotal());
        responsePage.setRecords(logPage.getRecords().stream()
                .map(this::buildFileAccessLogResponse)
                .collect(Collectors.toList()));

        return responsePage;
    }

    @Override
    public FileStatisticsResponse getFileStatistics() {
        Long userId = StpUtil.getLoginIdAsLong();

        // Query total file count and total size
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getDeleteFlag, DeleteStatusEnum.NOT_DELETED.getValue());

        List<FileRecord> allFiles = list(wrapper);

        long totalFiles = allFiles.size();
        long totalSize = allFiles.stream()
                .mapToLong(FileRecord::getFileSize)
                .sum();

        // Statistics by business type
        Map<String, Long> typeCount = allFiles.stream()
                .collect(Collectors.groupingBy(FileRecord::getBusinessType, Collectors.counting()));

        // Share statistics
        LambdaQueryWrapper<FileShare> shareOutWrapper = new LambdaQueryWrapper<>();
        shareOutWrapper.eq(FileShare::getShareFromUserId, userId)
                .eq(FileShare::getRevoked, 0);
        long shareOutCount = fileShareService.count(shareOutWrapper);

        LambdaQueryWrapper<FileShare> shareInWrapper = new LambdaQueryWrapper<>();
        shareInWrapper.eq(FileShare::getShareToUserId, userId)
                .eq(FileShare::getRevoked, 0);
        long shareInCount = fileShareService.count(shareInWrapper);

        // Access statistics
        List<Long> fileIds = allFiles.stream()
                .map(FileRecord::getId)
                .collect(Collectors.toList());

        long totalDownloads = 0, totalViews = 0, totalUploads = 0;
        if (!fileIds.isEmpty()) {
            LambdaQueryWrapper<FileAccessLog> logWrapper = new LambdaQueryWrapper<>();
            logWrapper.in(FileAccessLog::getFileId, fileIds);
            List<FileAccessLog> logs = fileAccessLogService.list(logWrapper);

            Map<String, Long> accessTypeCount = logs.stream()
                    .collect(Collectors.groupingBy(FileAccessLog::getAccessType, Collectors.counting()));

            totalDownloads = accessTypeCount.getOrDefault(AccessTypeEnum.DOWNLOAD.name(), 0L);
            totalViews = accessTypeCount.getOrDefault(AccessTypeEnum.PREVIEW.name(), 0L);
            totalUploads = accessTypeCount.getOrDefault(AccessTypeEnum.UPLOAD.name(), 0L);
        }

        // Last upload time
        LocalDateTime lastUploadedAt = allFiles.stream()
                .map(FileRecord::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        return FileStatisticsResponse.builder()
                .totalFiles(totalFiles)
                .totalSize(totalSize)
                .imageCount(typeCount.getOrDefault("IMAGE", 0L))
                .documentCount(typeCount.getOrDefault("DOCUMENT", 0L))
                .videoCount(typeCount.getOrDefault("VIDEO", 0L))
                .avatarCount(typeCount.getOrDefault("AVATAR", 0L))
                .tempCount(typeCount.getOrDefault("TEMP", 0L))
                .shareOutCount(shareOutCount)
                .shareInCount(shareInCount)
                .totalDownloads(totalDownloads)
                .totalViews(totalViews)
                .totalUploads(totalUploads)
                .lastUploadedAt(lastUploadedAt)
                .build();
    }

    // ========== Private Helper Methods ==========

    /**
     * Check if a file with the same hash exists (global lookup)
     *
     * @param fileHash SHA256 hash value of the file
     * @return If exists, return the first record, otherwise return null
     */
    private FileRecord checkExistingFile(String fileHash) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getFileHash, fileHash)
                .eq(FileRecord::getDeleteFlag, DeleteStatusEnum.NOT_DELETED.getValue())
                .last("LIMIT 1"); // LIMIT 1 is necessary: same hash may have multiple records (different users), only need to find one
        return getOne(wrapper);
    }

    /**
     * Build FileRecord entity
     */
    private FileRecord buildFileRecord(Long userId, MultipartFile file, String objectName,
                                       String fileUrl, String fileHash, UploadFileRequest request) {
        FileRecord fileRecord = new FileRecord();
        fileRecord.setUserId(userId);
        fileRecord.setBucketName(minioProperties.getBucket());
        fileRecord.setObjectName(objectName);
        fileRecord.setOriginalFilename(file.getOriginalFilename());
        fileRecord.setFileSize(file.getSize());
        fileRecord.setContentType(file.getContentType());
        fileRecord.setFileExtension(FileUtil.getFileExtension(file.getOriginalFilename()));
        fileRecord.setBusinessType(request.getBusinessType());
        fileRecord.setStatus(FileStatusEnum.COMPLETED.name());
        fileRecord.setVisibility(FileVisibilityEnum.PRIVATE.name());
        fileRecord.setFileUrl(fileUrl);
        fileRecord.setFileHash(fileHash);
        // Database default values: delete_flag=0, reference_count=0, created_at=now, updated_at=now
        return fileRecord;
    }

    /**
     * Build upload response
     */
    private FileUploadResponse buildUploadResponse(FileRecord fileRecord) {
        return FileUploadResponse.builder()
                .fileId(fileRecord.getId())
                .originalFilename(fileRecord.getOriginalFilename())
                .fileUrl(fileRecord.getFileUrl())
                .thumbnailUrl(fileRecord.getThumbnailUrl())
                .fileSize(fileRecord.getFileSize())
                .contentType(fileRecord.getContentType())
                .fileHash(fileRecord.getFileHash())
                .uploadedAt(fileRecord.getCreatedAt())
                .build();
    }

    /**
     * Build file info response
     */
    private FileInfoResponse buildFileInfoResponse(FileRecord fileRecord) {
        // Query file tags
        List<TagResponse> tags = getFileTagsInternal(fileRecord.getId());

        return FileInfoResponse.builder()
                .fileId(fileRecord.getId())
                .originalFilename(fileRecord.getOriginalFilename())
                .fileUrl(fileRecord.getFileUrl())
                .thumbnailUrl(fileRecord.getThumbnailUrl())
                .fileSize(fileRecord.getFileSize())
                .contentType(fileRecord.getContentType())
                .fileExtension(fileRecord.getFileExtension())
                .businessType(fileRecord.getBusinessType())
                .status(fileRecord.getStatus())
                .visibility(fileRecord.getVisibility())
                .tags(tags)
                .referenceCount(fileRecord.getReferenceCount())
                .createdAt(fileRecord.getCreatedAt())
                .updatedAt(fileRecord.getUpdatedAt())
                .build();
    }

    /**
     * Build file detail response
     */
    private FileDetailResponse buildFileDetailResponse(FileRecord fileRecord) {
        return buildFileDetailResponse(fileRecord, false);
    }
    
    private FileDetailResponse buildFileDetailResponse(FileRecord fileRecord, boolean filterSensitive) {
        // Query file tags
        List<TagResponse> tags = getFileTagsInternal(fileRecord.getId());

        // Query access statistics
        LambdaQueryWrapper<FileAccessLog> logWrapper = new LambdaQueryWrapper<>();
        logWrapper.eq(FileAccessLog::getFileId, fileRecord.getId());
        List<FileAccessLog> logs = fileAccessLogService.list(logWrapper);

        Map<String, Long> accessTypeCount = logs.stream()
                .collect(Collectors.groupingBy(FileAccessLog::getAccessType, Collectors.counting()));

        int viewCount = accessTypeCount.getOrDefault(AccessTypeEnum.PREVIEW.name(), 0L).intValue();
        int downloadCount = accessTypeCount.getOrDefault(AccessTypeEnum.DOWNLOAD.name(), 0L).intValue();

        LocalDateTime lastAccessedAt = logs.stream()
                .map(FileAccessLog::getCreatedAt)
                .max(LocalDateTime::compareTo)
                .orElse(null);

        // Query share information
        LambdaQueryWrapper<FileShare> shareWrapper = new LambdaQueryWrapper<>();
        shareWrapper.eq(FileShare::getFileId, fileRecord.getId())
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue());
        List<FileShare> shares = fileShareService.list(shareWrapper);

        List<FileDetailResponse.FileShareInfo> shareInfos = shares.stream()
                .map(share -> {
                    User user = userService.getById(share.getShareToUserId());
                    return FileDetailResponse.FileShareInfo.builder()
                            .shareId(share.getId())
                            .shareToUsername(user != null ? user.getUsername() : "Unknown")
                            .permissionLevel(share.getPermissionLevel())
                            .createdAt(share.getCreatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        FileDetailResponse.FileDetailResponseBuilder builder = FileDetailResponse.builder()
                .fileId(fileRecord.getId())
                .originalFilename(fileRecord.getOriginalFilename())
                .fileUrl(fileRecord.getFileUrl())
                .thumbnailUrl(fileRecord.getThumbnailUrl())
                .fileSize(fileRecord.getFileSize())
                .contentType(fileRecord.getContentType())
                .fileExtension(fileRecord.getFileExtension())
                .businessType(fileRecord.getBusinessType())
                .status(fileRecord.getStatus())
                .visibility(fileRecord.getVisibility())
                .viewCount(viewCount)
                .downloadCount(downloadCount)
                .lastAccessedAt(lastAccessedAt)
                .tags(tags)
                .referenceCount(fileRecord.getReferenceCount())
                .shares(shareInfos)
                .createdAt(fileRecord.getCreatedAt())
                .updatedAt(fileRecord.getUpdatedAt());
        
        // Filter sensitive information if requested
        if (filterSensitive) {
            builder.fileHash(null)
                   .metadata(null);
        } else {
            builder.fileHash(fileRecord.getFileHash())
                   .metadata(fileRecord.getMetadata());
        }
        
        return builder.build();
    }

    /**
     * Build file share response
     */
    private FileShareResponse buildFileShareResponse(FileShare fileShare) {
        FileRecord fileRecord = getById(fileShare.getFileId());
        User fromUser = userService.getById(fileShare.getShareFromUserId());
        User toUser = userService.getById(fileShare.getShareToUserId());

        return FileShareResponse.builder()
                .shareId(fileShare.getId())
                .fileId(fileShare.getFileId())
                .originalFilename(fileRecord != null ? fileRecord.getOriginalFilename() : "Unknown")
                .fileUrl(fileRecord != null ? fileRecord.getFileUrl() : null)
                .thumbnailUrl(fileRecord != null ? fileRecord.getThumbnailUrl() : null)
                .shareFromUserId(fileShare.getShareFromUserId())
                .shareFromUsername(fromUser != null ? fromUser.getUsername() : "Unknown")
                .shareToUserId(fileShare.getShareToUserId())
                .shareToUsername(toUser != null ? toUser.getUsername() : "Unknown")
                .shareType(fileShare.getShareType())
                .expiresAt(fileShare.getExpiresAt())
                .permissionLevel(fileShare.getPermissionLevel())
                .message(fileShare.getMessage())
                .revoked(fileShare.getRevoked() == 1)
                .viewCount(fileShare.getViewCount())
                .downloadCount(fileShare.getDownloadCount())
                .createdAt(fileShare.getCreatedAt())
                .updatedAt(fileShare.getUpdatedAt())
                .build();
    }

    /**
     * Build file access log response
     */
    private FileAccessLogResponse buildFileAccessLogResponse(FileAccessLog log) {
        User user = userService.getById(log.getUserId());

        return FileAccessLogResponse.builder()
                .logId(log.getId())
                .accessType(log.getAccessType())
                .username(user != null ? user.getUsername() : "Unknown")
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .accessedAt(log.getCreatedAt())
                .build();
    }

    /**
     * Check file permissions (using already queried fileRecord)
     */
    private void checkFilePermission(FileRecord fileRecord, Long userId) {
        // Check if user is file owner
        if (fileRecord.getUserId().equals(userId)) {
            return;
        }

        // Check if file is shared (using FileShareService encapsulated method)
        FileShare fileShare = fileShareService.getValidShareByFileAndUser(fileRecord.getId(), userId);
        BusinessException.assertNotNull(fileShare, ResponseConstant.FILE_ACCESS_DENIED_MESSAGE);

        // Check if share has expired
        if (fileShare.getExpiresAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            BusinessException.throwIf(fileShare.getExpiresAt().isBefore(now) || fileShare.getExpiresAt().isEqual(now),
                    ResponseConstant.FORBIDDEN, ResponseConstant.SHARE_EXPIRED_MESSAGE);
        }
    }

    /**
     * Internal method: Get file tags (no permission check)
     */
    private List<TagResponse> getFileTagsInternal(Long fileId) {
        // Use FileTagService to get file tags
        return fileTagService.getFileTagsResponse(fileId);
    }

    /**
     * Log file access
     */
    private void logFileAccess(Long fileId, Long userId, String accessType) {
        try {
            FileAccessLog log = new FileAccessLog();
            log.setFileId(fileId);
            log.setUserId(userId);
            log.setAccessType(accessType);
            log.setIpAddress(HttpRequestUtil.extractClientIp());
            log.setUserAgent(HttpRequestUtil.extractUserAgent());
            log.setCreatedAt(LocalDateTime.now());
            fileAccessLogService.save(log);
        } catch (Exception e) {
            log.warn("Failed to log access", e);
        }
    }

    // ========== Recycle Bin Management ==========

    @Override
    public Page<FileInfoResponse> queryTrash(RecycleBinQueryRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        log.info("Query recycle bin: userId={}, page={}, size={}",
                userId, request.getPage(), request.getSize());

        // Create pagination object
        Page<FileRecord> page = new Page<>(request.getPage(), request.getSize());

        // Use custom Mapper method to query (bypass @TableLogic)
        Page<FileRecord> recordPage = baseMapper.selectTrashFiles(page, userId);

        // Convert to response objects
        Page<FileInfoResponse> responsePage = new Page<>();
        responsePage.setCurrent(recordPage.getCurrent());
        responsePage.setSize(recordPage.getSize());
        responsePage.setTotal(recordPage.getTotal());
        responsePage.setRecords(
                recordPage.getRecords().stream()
                        .map(this::buildFileInfoResponse)
                        .toList()
        );

        return responsePage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse batchRestoreFiles(BatchOperationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> fileIds = request.getFileIds();

        log.info("Batch restore files: userId={}, fileIds={}", userId, fileIds);

        // Query valid files in recycle bin (using custom Mapper to bypass @TableLogic)
        List<FileRecord> validFileRecords = baseMapper.selectTrashFilesByIds(fileIds, userId);
        List<Long> validFileIds = validFileRecords.stream()
                .map(FileRecord::getId)
                .toList();

        // Batch update valid files (restore)
        int updatedCount = 0;
        if (!validFileIds.isEmpty()) {
            updatedCount = baseMapper.batchUpdateDeleteFlag(
                    validFileIds,
                    userId,
                    DeleteStatusEnum.NOT_DELETED.getValue()
            );
        }

        int successCount = updatedCount;
        int failedCount = fileIds.size() - successCount;

        log.info("Batch restore completed: total={}, success={}, failed={}",
                fileIds.size(), successCount, failedCount);

        return BatchOperationResponse.builder()
                .total(fileIds.size())
                .success(successCount)
                .failed(failedCount)
                .build();
    }

    @Override
    @Transactional
    public BatchOperationResponse batchPermanentDeleteFiles(BatchOperationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> fileIds = request.getFileIds();

        log.info("Batch permanently delete files: userId={}, fileIds={}", userId, fileIds);

        // Query valid files in recycle bin (using custom Mapper to bypass @TableLogic)
        List<FileRecord> filesToDelete = baseMapper.selectTrashFilesByIds(fileIds, userId);

        if (filesToDelete.isEmpty()) {
            return BatchOperationResponse.builder().total(0).success(0).failed(0).build();
        }

        // Collect valid file IDs and object names
        List<Long> validFileIds = filesToDelete.stream().map(FileRecord::getId).collect(Collectors.toList());
        List<String> objectNames = filesToDelete.stream().map(FileRecord::getObjectName).collect(Collectors.toList());

        // 1. Batch delete files from MinIO (failure does not affect database operations)
        try {
            minioService.deleteFiles(objectNames);
            log.info("Batch delete MinIO files successful: count={}", objectNames.size());
        } catch (Exception e) {
            log.warn("Batch delete MinIO files failed (continue deleting database records): error={}", e.getMessage());
        }

        // 2. Query and count tag usage for all files
        LambdaQueryWrapper<FileTag> fileTagQuery = new LambdaQueryWrapper<>();
        fileTagQuery.in(FileTag::getFileId, validFileIds);
        List<FileTag> fileTags = fileTagService.list(fileTagQuery);

        // Count occurrences of each tag (a tag used in multiple files needs to subtract corresponding count)
        Map<Long, Integer> tagCountMap = fileTags.stream()
                .collect(Collectors.groupingBy(
                        FileTag::getTagId,
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));

        // 3. Batch delete file-tag associations
        fileTagMapper.deleteBatchByFileIds(validFileIds);

        // 4. Batch physically delete file records
        int deletedCount = baseMapper.permanentDeleteBatch(validFileIds, userId);

        // 5. Batch decrease tag usage count (by actual occurrence count)
        if (!tagCountMap.isEmpty()) {
            tagService.batchDecreaseUsageCountByAmount(tagCountMap);
        }

        int failedCount = fileIds.size() - deletedCount;

        log.info("Batch permanent delete completed: total={}, success={}, failed={}",
                fileIds.size(), deletedCount, failedCount);

        return BatchOperationResponse.builder()
                .total(fileIds.size())
                .success(deletedCount)
                .failed(failedCount)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse emptyRecycleBin() {
        Long userId = StpUtil.getLoginIdAsLong();

        log.info("Empty recycle bin: userId={}", userId);

        // Query all file IDs in user's recycle bin
        List<Long> fileIds = baseMapper.selectTrashFileIds(userId);

        if (fileIds.isEmpty()) {
            return BatchOperationResponse.builder()
                    .total(0)
                    .success(0)
                    .failed(0)
                    .build();
        }

        // Batch permanently delete
        BatchOperationRequest request = new BatchOperationRequest();
        request.setFileIds(fileIds);

        return batchPermanentDeleteFiles(request);
    }

    @Override
    public TrashStatsResponse getTrashStats() {
        Long userId = StpUtil.getLoginIdAsLong();

        log.info("Query recycle bin statistics: userId={}", userId);

        TrashStatsResponse stats = baseMapper.selectTrashStats(userId);

        if (stats == null) {
            return TrashStatsResponse.builder()
                    .count(0L)
                    .totalSize(0L)
                    .build();
        }

        return stats;
    }

}
