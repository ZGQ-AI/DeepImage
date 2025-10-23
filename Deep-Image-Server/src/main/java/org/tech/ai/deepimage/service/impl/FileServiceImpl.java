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
 * 文件管理Service实现类
 *
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl extends ServiceImpl<FileRecordMapper, FileRecord> implements FileService {

    // MinIO 相关服务
    private final MinioService minioService;
    private final MinioProperties minioProperties;

    private final FileTagService fileTagService;
    private final TagService tagService;
    private final FileShareService fileShareService;
    private final FileAccessLogService fileAccessLogService;
    private final UserService userService;
    private final FileRecordService fileRecordService;

    // ========== 文件上传 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileUploadResponse uploadFile(UploadFileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        MultipartFile file = request.getFile();

        log.info("开始上传文件: userId={}, filename={}, size={}, businessType={}",
                userId, file.getOriginalFilename(), file.getSize(), request.getBusinessType());

        try {
            // 1. 校验业务类型
            if (!BusinessTypeEnum.isValid(request.getBusinessType())) {
                throw BusinessException.badRequest(ResponseConstant.FILE_TYPE_INVALID_MESSAGE);
            }

            // 2. 校验文件大小
            if (file.getSize() > FileConstant.MAX_FILE_SIZE) {
                throw BusinessException.badRequest(ResponseConstant.FILE_SIZE_EXCEEDED_MESSAGE);
            }

            // 3. 读取文件内容到内存（避免 InputStream 重复使用问题）
            byte[] fileBytes = file.getBytes();

            // 4. 计算文件哈希
            String fileHash = HashUtil.sha256(fileBytes);

            // 5. 检查文件是否已存在（全局去重）
            FileRecord existingFile = checkExistingFile(fileHash);
            if (existingFile != null) {
                log.info("文件已存在，复用现有文件: existingFileId={}, currentUserId={}",
                        existingFile.getId(), userId);
                // 复用现有文件，为当前用户创建新的记录
                return buildUploadResponse(existingFile);
            }

            // 6. 生成对象名称
            String objectName = FileUtil.generateObjectName(userId, request.getBusinessType(),
                    FileUtil.getFileExtension(file.getOriginalFilename()));

            // 7. 上传到MinIO（使用字节数组创建新的 InputStream）
            String fileUrl = minioService.uploadFile(
                    new ByteArrayInputStream(fileBytes), objectName, file.getContentType());

            // 8. 保存文件记录
            FileRecord fileRecord = buildFileRecord(userId, file, objectName, fileUrl, fileHash, request);
            save(fileRecord);

            // 9. 关联标签（使用 FileTagService）
            if (CollectionUtils.isNotEmpty(request.getTagIds())) {
                fileTagService.batchSetFileTags(fileRecord.getId(), userId, request.getTagIds());
            }

            // 10. 记录访问日志
            logFileAccess(fileRecord.getId(), userId, AccessTypeEnum.UPLOAD.name());

            log.info("文件上传成功: fileId={}", fileRecord.getId());
            return buildUploadResponse(fileRecord);

        } catch (Exception e) {
            log.error("文件上传失败: userId={}, filename={}", userId, file.getOriginalFilename(), e);
            throw BusinessException.badRequest(ResponseConstant.FILE_UPLOAD_FAILED_MESSAGE + ": " + e.getMessage());
        }
    }

    @Override
    public FileExistsResponse checkFileExists(FileExistsCheckRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 全局查询是否已有相同哈希的文件
        FileRecord existingFile = checkExistingFile(request.getFileHash());

        if (existingFile != null) {
            log.info("文件已存在: fileHash={}, userId={}", request.getFileHash(), userId);
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

    // ========== 文件查询 ==========

    @Override
    public Page<FileInfoResponse> listFiles(ListFilesRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 如果指定了标签ID，先查询关联的文件ID列表
        List<Long> fileIds = null;
        if (request.getTagId() != null) {
            // 校验标签权限
            Tag userTag = tagService.getUserTag(request.getTagId(), userId);
            BusinessException.assertNotNull(userTag, ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

            // 查询该标签关联的所有文件ID
            LambdaQueryWrapper<FileTag> fileTagWrapper = new LambdaQueryWrapper<>();
            fileTagWrapper.eq(FileTag::getTagId, request.getTagId());
            List<FileTag> fileTags = fileTagService.list(fileTagWrapper);

            if (fileTags.isEmpty()) {
                // 如果没有关联的文件，直接返回空结果
                return new Page<>(request.getPage(), request.getPageSize(), 0);
            }

            fileIds = fileTags.stream()
                    .map(FileTag::getFileId)
                    .collect(Collectors.toList());
        }

        // 构建查询条件
        Page<FileRecord> page = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUserId, userId);

        // 如果有标签筛选，添加 fileIds 条件
        if (fileIds != null) {
            wrapper.in(FileRecord::getId, fileIds);
        }

        // 业务类型筛选
        if (StringUtils.isNotBlank(request.getBusinessType())) {
            wrapper.eq(FileRecord::getBusinessType, request.getBusinessType());
        }

        // 文件名搜索
        if (StringUtils.isNotBlank(request.getFilename())) {
            wrapper.like(FileRecord::getOriginalFilename, request.getFilename());
        }

        // 排序
        if (FileConstant.SORT_BY_FILE_SIZE.equals(request.getSortBy())) {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getFileSize);
        } else if (FileConstant.SORT_BY_FILENAME.equals(request.getSortBy())) {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getOriginalFilename);
        } else {
            wrapper.orderBy(true, FileConstant.SORT_ORDER_ASC.equals(request.getSortOrder()), FileRecord::getCreatedAt);
        }

        Page<FileRecord> recordPage = page(page, wrapper);

        // 转换为响应对象
        Page<FileInfoResponse> responsePage = new Page<>(recordPage.getCurrent(), recordPage.getSize(),
                recordPage.getTotal());
        responsePage.setRecords(recordPage.getRecords().stream()
                .map(this::buildFileInfoResponse)
                .collect(Collectors.toList()));

        return responsePage;
    }

    @Override
    public FileDetailResponse getFileDetail(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文件记录
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // 权限校验
        checkFilePermission(fileRecord, userId);

        // 构建详情响应
        return buildFileDetailResponse(fileRecord);
    }

    // ========== 文件下载 ==========

    @Override
    public InputStream downloadFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文件记录
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // 权限校验
        checkFilePermission(fileRecord, userId);

        // 记录访问日志
        logFileAccess(fileId, userId, AccessTypeEnum.DOWNLOAD.name());

        // 从MinIO下载
        return minioService.downloadFile(fileRecord.getObjectName());
    }

    @Override
    public FilePreviewResponse getPreviewUrl(Long fileId, Integer expirySeconds) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文件记录
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // 默认有效期
        if (expirySeconds == null || expirySeconds <= 0) {
            expirySeconds = FileConstant.DEFAULT_PREVIEW_EXPIRY_SECONDS;
        }

        // 如果是文件所有者，直接通过权限校验
        if (fileRecord.getUserId().equals(userId)) {
            // 文件所有者不受分享时间限制，直接生成预签名URL
            String previewUrl = minioService.getPresignedDownloadUrl(fileRecord.getObjectName(), expirySeconds);
            logFileAccess(fileId, userId, AccessTypeEnum.PREVIEW.name());

            return FilePreviewResponse.builder()
                    .previewUrl(previewUrl)
                    .expirySeconds(expirySeconds)
                    .expiresAt(LocalDateTime.now().plusSeconds(expirySeconds))
                    .build();
        }

        // 如果不是文件所有者，检查分享权限和时间限制
        FileShare fileShare = fileShareService.getValidShareByFileAndUser(fileId, userId);

        // 如果没有找到分享记录，无权访问
        BusinessException.assertNotNull(fileShare, ResponseConstant.FILE_ACCESS_DENIED_MESSAGE);

        // 检查分享是否过期
        LocalDateTime now = LocalDateTime.now();
        if (fileShare.getExpiresAt() != null) {
            BusinessException.throwIf(fileShare.getExpiresAt().isBefore(now) || fileShare.getExpiresAt().isEqual(now),
                    ResponseConstant.FORBIDDEN, ResponseConstant.SHARE_EXPIRED_MESSAGE);

            // 计算分享剩余的有效时间（秒）
            long remainingSeconds = java.time.Duration.between(now, fileShare.getExpiresAt()).getSeconds();

            // 预览URL有效期不能超过分享的剩余有效期
            if (expirySeconds > remainingSeconds) {
                expirySeconds = (int) Math.max(60, remainingSeconds); // 确保至少 60 秒
                log.info("预览URL有效期受分享限制，调整为: {} 秒", expirySeconds);
            }
        }

        // 生成预签名URL
        String previewUrl = minioService.getPresignedDownloadUrl(fileRecord.getObjectName(), expirySeconds);

        // 记录访问日志
        logFileAccess(fileId, userId, AccessTypeEnum.PREVIEW.name());

        return FilePreviewResponse.builder()
                .previewUrl(previewUrl)
                .expirySeconds(expirySeconds)
                .expiresAt(LocalDateTime.now().plusSeconds(expirySeconds))
                .build();
    }

    // ========== 文件管理 ==========

    @Override
    public FileInfoResponse renameFile(RenameFileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // 查询文件记录
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // 权限校验
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // 更新文件名
        fileRecord.setOriginalFilename(request.getNewFilename());
        fileRecord.setUpdatedAt(LocalDateTime.now());
        updateById(fileRecord);

        log.info("文件重命名成功: fileId={}, newFilename={}", fileId, request.getNewFilename());
        return buildFileInfoResponse(fileRecord);
    }

    @Override
    public Boolean deleteFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文件记录
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // 权限校验
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        LambdaUpdateWrapper<FileRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(FileRecord::getId, fileId)
                .set(FileRecord::getStatus, FileStatusEnum.DELETED.name())
                .set(FileRecord::getDeleteFlag, DeleteStatusEnum.DELETED.getValue());

        update(updateWrapper);

        log.info("文件删除成功（软删除）: fileId={}", fileId);
        return true;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BatchOperationResponse batchDeleteFiles(BatchOperationRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        List<Long> fileIds = request.getFileIds();

        log.info("批量删除文件: userId={}, fileIds={}", userId, fileIds);

        // 直接批量更新，数据库层面自动过滤用户权限
        LambdaUpdateWrapper<FileRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(FileRecord::getId, fileIds)
                .eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getDeleteFlag, DeleteStatusEnum.NOT_DELETED.getValue())
                .set(FileRecord::getDeleteFlag, DeleteStatusEnum.DELETED.getValue())
                .set(FileRecord::getStatus, FileStatusEnum.DELETED.name());
        int updatedCount = fileRecordService.getBaseMapper().update(updateWrapper);

        int failedCount = fileIds.size() - updatedCount;

        log.info("批量删除完成: total={}, success={}, failed={}",
                fileIds.size(), updatedCount, failedCount);

        return BatchOperationResponse.builder()
                .total(fileIds.size())
                .success(updatedCount)
                .failed(failedCount)
                .results(null)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean permanentDeleteFile(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文件记录
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);

        // 权限校验
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // 检查引用计数
        BusinessException.assertFalse(fileRecord.getReferenceCount() != null && fileRecord.getReferenceCount() > 0,
                ResponseConstant.FILE_BEING_REFERENCED_MESSAGE);

        // 从MinIO删除
        try {
            minioService.deleteFile(fileRecord.getObjectName());
        } catch (Exception e) {
            log.error("从MinIO删除文件失败: fileId={}, objectName={}", fileId, fileRecord.getObjectName(), e);
        }

        // 删除数据库记录
        removeById(fileId);

        // 删除关联数据（使用 FileTagService）并减少标签使用计数
        Set<Long> deletedTagIds = fileTagService.deleteAllByFileId(fileId);
        if (CollectionUtils.isNotEmpty(deletedTagIds)) {
            tagService.batchDecreaseUsageCount(deletedTagIds);
        }

        log.info("文件彻底删除成功: fileId={}", fileId);
        return true;
    }

    // ========== 文件标签 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<TagResponse> addFileTags(AddFileTagsRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // 校验文件权限
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // 批量设置标签（使用 FileTagService）
        fileTagService.batchSetFileTags(fileId, userId, request.getTagIds());

        // 返回文件的所有标签
        return fileTagService.getFileTagsResponse(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean removeFileTag(Long fileId, Long tagId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 校验文件权限
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.FILE_PERMISSION_DENIED_MESSAGE);

        // 删除关联（使用 FileTagService）
        LambdaQueryWrapper<FileTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileTag::getFileId, fileId)
                .eq(FileTag::getTagId, tagId);
        fileTagService.remove(wrapper);

        // 减少标签使用计数（使用 TagService）
        tagService.batchDecreaseUsageCount(Set.of(tagId));

        log.info("文件标签移除成功: fileId={}, tagId={}", fileId, tagId);
        return true;
    }

    @Override
    public List<TagResponse> getFileTags(Long fileId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询文件记录并校验权限
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        checkFilePermission(fileRecord, userId);

        // 使用 FileTagService 获取文件标签
        return fileTagService.getFileTagsResponse(fileId);
    }

    // ========== 文件分享 ==========

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileShareResponse createFileShare(CreateFileShareRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // 校验文件权限
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.SHARE_PERMISSION_DENIED_MESSAGE);

        // 校验目标用户
        User targetUser = userService.getById(request.getShareToUserId());
        BusinessException.assertNotNull(targetUser, ResponseConstant.TARGET_USER_NOT_FOUND_MESSAGE);

        // 校验分享类型和过期时间
        BusinessException.assertFalse(ShareTypeEnum.TEMPORARY.name().equals(request.getShareType())
                        && request.getExpiresAt() == null,
                ResponseConstant.TEMPORARY_SHARE_REQUIRES_EXPIRY_MESSAGE);

        // 检查是否已存在分享
        LambdaQueryWrapper<FileShare> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FileShare::getFileId, fileId)
                .eq(FileShare::getShareToUserId, request.getShareToUserId())
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue());
        BusinessException.throwIf(fileShareService.count(checkWrapper) > 0,
                ResponseConstant.PARAM_ERROR, ResponseConstant.SHARE_ALREADY_EXISTS_MESSAGE);

        // 创建分享记录
        FileShare fileShare = new FileShare();
        fileShare.setFileId(fileId);
        fileShare.setShareFromUserId(userId);
        fileShare.setShareToUserId(request.getShareToUserId());
        fileShare.setShareType(request.getShareType());
        fileShare.setExpiresAt(request.getExpiresAt());
        fileShare.setPermissionLevel(request.getPermissionLevel());
        fileShare.setMessage(request.getMessage());
        // 数据库默认值：revoked=0, view_count=0, download_count=0, created_at=now, updated_at=now
        fileShareService.save(fileShare);

        // 更新文件可见性
        if (FileVisibilityEnum.PRIVATE.name().equals(fileRecord.getVisibility())) {
            fileRecord.setVisibility(FileVisibilityEnum.SHARED.name());
            updateById(fileRecord);
        }

        log.info("文件分享创建成功: shareId={}, fileId={}, toUserId={}",
                fileShare.getId(), fileId, request.getShareToUserId());

        return buildFileShareResponse(fileShare);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean cancelFileShare(Long shareId) {
        Long userId = StpUtil.getLoginIdAsLong();

        // 查询分享记录
        FileShare fileShare = fileShareService.getById(shareId);
        BusinessException.assertNotNull(fileShare, ResponseConstant.SHARE_NOT_FOUND_MESSAGE);

        // 权限校验
        BusinessException.assertTrue(fileShare.getShareFromUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.CANCEL_SHARE_PERMISSION_DENIED_MESSAGE);

        // 撤销分享
        fileShare.setRevoked(RevokedStatusEnum.REVOKED.getValue());
        fileShareService.updateById(fileShare);

        // 检查文件是否还有其他有效分享
        LambdaQueryWrapper<FileShare> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileShare::getFileId, fileShare.getFileId())
                .eq(FileShare::getRevoked, RevokedStatusEnum.NOT_REVOKED.getValue());
        if (fileShareService.count(wrapper) == 0) {
            // 没有其他分享，更新文件可见性为私有
            FileRecord fileRecord = getById(fileShare.getFileId());
            if (fileRecord != null) {
                fileRecord.setVisibility(FileVisibilityEnum.PRIVATE.name());
                updateById(fileRecord);
            }
        }

        log.info("文件分享取消成功: shareId={}", shareId);
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

        // 权限校验（必须是分享者或接收者）
        BusinessException.assertTrue(
                fileShare.getShareFromUserId().equals(userId) || fileShare.getShareToUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.VIEW_SHARE_PERMISSION_DENIED_MESSAGE);

        return buildFileShareResponse(fileShare);
    }

    // ========== 访问日志 ==========

    @Override
    public Page<FileAccessLogResponse> getFileAccessLogs(GetFileAccessLogsRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        Long fileId = request.getFileId();

        // 校验权限（必须是文件所有者）
        FileRecord fileRecord = getById(fileId);
        BusinessException.assertNotNull(fileRecord, ResponseConstant.FILE_NOT_FOUND_MESSAGE);
        BusinessException.assertTrue(fileRecord.getUserId().equals(userId),
                ResponseConstant.FORBIDDEN, ResponseConstant.VIEW_ACCESS_LOG_PERMISSION_DENIED_MESSAGE);

        // 查询访问日志
        Page<FileAccessLog> logPage = new Page<>(request.getPage(), request.getPageSize());
        LambdaQueryWrapper<FileAccessLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileAccessLog::getFileId, fileId)
                .orderByDesc(FileAccessLog::getCreatedAt);

        fileAccessLogService.page(logPage, wrapper);

        // 转换响应
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

        // 查询总文件数和总大小
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getUserId, userId)
                .eq(FileRecord::getDeleteFlag, DeleteStatusEnum.NOT_DELETED.getValue());

        List<FileRecord> allFiles = list(wrapper);

        long totalFiles = allFiles.size();
        long totalSize = allFiles.stream()
                .mapToLong(FileRecord::getFileSize)
                .sum();

        // 按业务类型统计
        Map<String, Long> typeCount = allFiles.stream()
                .collect(Collectors.groupingBy(FileRecord::getBusinessType, Collectors.counting()));

        // 分享统计
        LambdaQueryWrapper<FileShare> shareOutWrapper = new LambdaQueryWrapper<>();
        shareOutWrapper.eq(FileShare::getShareFromUserId, userId)
                .eq(FileShare::getRevoked, 0);
        long shareOutCount = fileShareService.count(shareOutWrapper);

        LambdaQueryWrapper<FileShare> shareInWrapper = new LambdaQueryWrapper<>();
        shareInWrapper.eq(FileShare::getShareToUserId, userId)
                .eq(FileShare::getRevoked, 0);
        long shareInCount = fileShareService.count(shareInWrapper);

        // 访问统计
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

        // 最近上传时间
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

    // ========== 私有辅助方法 ==========

    /**
     * 检查是否存在相同哈希的文件（全局查找）
     *
     * @param fileHash 文件SHA256哈希值
     * @return 如果存在返回第一条记录，否则返回null
     */
    private FileRecord checkExistingFile(String fileHash) {
        LambdaQueryWrapper<FileRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileRecord::getFileHash, fileHash)
                .eq(FileRecord::getDeleteFlag, DeleteStatusEnum.NOT_DELETED.getValue())
                .last("LIMIT 1"); // LIMIT 1 是必要的：相同哈希可能有多条记录（不同用户），只需要找到一条即可
        return getOne(wrapper);
    }

    /**
     * 构建FileRecord实体
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
        // 数据库默认值：delete_flag=0, reference_count=0, created_at=now, updated_at=now
        return fileRecord;
    }

    /**
     * 构建上传响应
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
     * 构建文件信息响应
     */
    private FileInfoResponse buildFileInfoResponse(FileRecord fileRecord) {
        // 查询文件的标签
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
     * 构建文件详情响应
     */
    private FileDetailResponse buildFileDetailResponse(FileRecord fileRecord) {
        // 查询文件的标签
        List<TagResponse> tags = getFileTagsInternal(fileRecord.getId());

        // 查询访问统计
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

        // 查询分享信息
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

        return FileDetailResponse.builder()
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
                .fileHash(fileRecord.getFileHash())
                .metadata(fileRecord.getMetadata())
                .viewCount(viewCount)
                .downloadCount(downloadCount)
                .lastAccessedAt(lastAccessedAt)
                .tags(tags)
                .referenceCount(fileRecord.getReferenceCount())
                .shares(shareInfos)
                .createdAt(fileRecord.getCreatedAt())
                .updatedAt(fileRecord.getUpdatedAt())
                .build();
    }

    /**
     * 构建文件分享响应
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
     * 构建文件访问日志响应
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
     * 检查文件权限（使用已查询的 fileRecord）
     */
    private void checkFilePermission(FileRecord fileRecord, Long userId) {
        // 检查是否为文件所有者
        if (fileRecord.getUserId().equals(userId)) {
            return;
        }

        // 检查是否被分享（使用 FileShareService 封装的方法）
        FileShare fileShare = fileShareService.getValidShareByFileAndUser(fileRecord.getId(), userId);
        BusinessException.assertNotNull(fileShare, ResponseConstant.FILE_ACCESS_DENIED_MESSAGE);

        // 检查分享是否过期
        if (fileShare.getExpiresAt() != null) {
            LocalDateTime now = LocalDateTime.now();
            BusinessException.throwIf(fileShare.getExpiresAt().isBefore(now) || fileShare.getExpiresAt().isEqual(now),
                    ResponseConstant.FORBIDDEN, ResponseConstant.SHARE_EXPIRED_MESSAGE);
        }
    }

    /**
     * 内部方法：获取文件标签（不做权限检查）
     */
    private List<TagResponse> getFileTagsInternal(Long fileId) {
        // 使用 FileTagService 获取文件标签
        return fileTagService.getFileTagsResponse(fileId);
    }

    /**
     * 记录文件访问日志
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
            log.warn("记录访问日志失败", e);
        }
    }

}
