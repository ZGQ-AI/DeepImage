package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;
import org.tech.ai.deepimage.service.FileService;
import org.tech.ai.deepimage.util.CacheKeyUtil;
import org.tech.ai.deepimage.util.RandomRedisKeyTtlUtil;
import org.tech.ai.deepimage.util.RedisKeyFactory;
import org.tech.ai.deepimage.util.RedisUtil;

import java.util.List;
import java.util.Optional;

/**
 * File service with cache support (Decorator Pattern)
 * Wraps FileServiceImpl and adds caching layer
 *
 * @author zgq
 * @since 2025-11-01
 */
@Slf4j
@Service
@Primary
public class FileCacheServiceImpl implements FileService {

    @Qualifier("fileServiceImpl")
    @Autowired
    private FileService fileService;
    @Autowired
    private RedisUtil redisUtil;


    @Override
    public FileUploadResponse uploadFile(UploadFileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileUploadResponse response = fileService.uploadFile(request);
        // Invalidate cache for specific business type
        String businessType = CacheKeyUtil.getBusinessTypeForKey(request);
        String pattern = RedisKeyFactory.getUserFileListPattern(userId, businessType);
        long deletedCount = redisUtil.deleteByPattern(pattern);
        if (deletedCount > 0) {
            log.info("Invalidated {} cache entries for user: {}, businessType: {}", deletedCount, userId, businessType);
        }
        return response;
    }

    @Override
    public Page<FileInfoResponse> listFiles(ListFilesRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();

        // Generate cache key
        String businessType = CacheKeyUtil.getBusinessTypeForKey(request);
        String requestHash = CacheKeyUtil.generateRequestHash(request);
        String cacheKey = RedisKeyFactory.getUserFileListKey(userId, businessType, requestHash);

        // Check cache first
        Optional<Page<FileInfoResponse>> cachedResult = redisUtil.get(cacheKey);
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for file list query, userId: {}", userId);
            return cachedResult.get();
        }

        // Cache miss, query from service
        log.debug("Cache miss for file list query, userId: {}", userId);
        Page<FileInfoResponse> result = fileService.listFiles(request);

        // Cache the result
        int ttl = RandomRedisKeyTtlUtil.calculateRandomTTL();
        redisUtil.set(cacheKey, result, ttl);
        log.debug("Cached result for key: {} with TTL: {} seconds", cacheKey, ttl);

        return result;
    }

    @Override
    public Page<FileInfoResponse> listPublicFiles(ListPublicFilesRequest request) {
        // Generate cache key
        String requestHash = CacheKeyUtil.generateRequestHash(request);
        String cacheKey = RedisKeyFactory.getPublicFileListKey(requestHash);

        // Check cache first
        Optional<Page<FileInfoResponse>> cachedResult = redisUtil.get(cacheKey);
        if (cachedResult.isPresent()) {
            log.debug("Cache hit for public file list query");
            return cachedResult.get();
        }

        // Cache miss, query from service
        log.debug("Cache miss for public file list query");
        Page<FileInfoResponse> result = fileService.listPublicFiles(request);

        // Cache the result
        int ttl = RandomRedisKeyTtlUtil.calculateRandomTTL();
        redisUtil.set(cacheKey, result, ttl);
        log.debug("Cached result for key: {} with TTL: {} seconds", cacheKey, ttl);

        return result;
    }

    @Override
    public FileDownloadResponse downloadFile(Long fileId) {
        return fileService.downloadFile(fileId);
    }

    @Override
    public FilePreviewResponse getPreviewUrl(Long fileId, Integer expirySeconds) {
        return fileService.getPreviewUrl(fileId, expirySeconds);
    }


    @Override
    public FileInfoResponse updateFileProperties(UpdateFilePropertiesRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        FileInfoResponse response = fileService.updateFileProperties(request);
        
        // Invalidate file list cache for specific business type
        if (response.getBusinessType() != null) {
            String businessType = CacheKeyUtil.getBusinessTypeForKey(new BusinessRequest() {
                @Override
                public String getBusinessType() {
                    return response.getBusinessType();
                }
            });
            String pattern = RedisKeyFactory.getUserFileListPattern(userId, businessType);
            redisUtil.deleteByPattern(pattern);
        }
        
        
        return response;
    }

    @Override
    public Boolean deleteFile(Long fileId) {
        return fileService.deleteFile(fileId);
    }

    @Override
    public BatchOperationResponse batchDeleteFiles(BatchOperationRequest request) {
        return fileService.batchDeleteFiles(request);
    }

    @Override
    public Boolean permanentDeleteFile(Long fileId) {
        return fileService.permanentDeleteFile(fileId);
    }


    @Override
    public List<TagResponse> addFileTags(AddFileTagsRequest request) {
        return fileService.addFileTags(request);
    }

    @Override
    public Boolean removeFileTag(Long fileId, Long tagId) {
        return fileService.removeFileTag(fileId, tagId);
    }

    @Override
    public List<TagResponse> getFileTags(Long fileId) {
        return fileService.getFileTags(fileId);
    }




    @Override
    public Page<FileAccessLogResponse> getFileAccessLogs(GetFileAccessLogsRequest request) {
        return fileService.getFileAccessLogs(request);
    }

    @Override
    public FileStatisticsResponse getFileStatistics() {
        return fileService.getFileStatistics();
    }


    @Override
    public Page<FileInfoResponse> queryTrash(RecycleBinQueryRequest request) {
        return fileService.queryTrash(request);
    }

    @Override
    public BatchOperationResponse batchRestoreFiles(BatchOperationRequest request) {
        return fileService.batchRestoreFiles(request);
    }

    @Override
    public BatchOperationResponse batchPermanentDeleteFiles(BatchOperationRequest request) {
        return fileService.batchPermanentDeleteFiles(request);
    }

    @Override
    public BatchOperationResponse emptyRecycleBin() {
        return fileService.emptyRecycleBin();
    }

    @Override
    public TrashStatsResponse getTrashStats() {
        return fileService.getTrashStats();
    }

}

