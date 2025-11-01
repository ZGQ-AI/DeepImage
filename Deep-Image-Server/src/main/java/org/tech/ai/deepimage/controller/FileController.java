package org.tech.ai.deepimage.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.tech.ai.deepimage.annotation.LogParams;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;
import org.tech.ai.deepimage.service.FileService;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * File Management Controller
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@LogParams
public class FileController {
    
    private final FileService fileService;
    
    // ========== File Upload ==========
    
    /**
     * Single file upload
     * POST /api/files/upload
     */
    @PostMapping("/upload")
    public ApiResponse<FileUploadResponse> uploadFile(
            @RequestParam MultipartFile file,
            @RequestParam String businessType,
            @RequestParam(required = false) List<Long> tagIds) {
        
        UploadFileRequest request = new UploadFileRequest();
        request.setFile(file);
        request.setBusinessType(businessType);
        request.setTagIds(tagIds);
        
        FileUploadResponse response = fileService.uploadFile(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Check if file exists
     * POST /api/files/check-exists
     */
    @PostMapping("/check-exists")
    public ApiResponse<FileExistsResponse> checkFileExists(@Valid @RequestBody FileExistsCheckRequest request) {
        FileExistsResponse response = fileService.checkFileExists(request);
        return ApiResponse.success(response);
    }
    
    // ========== File Query ==========
    
    /**
     * Paginated file list query (unified interface)
     * POST /api/files/list
     * Supports filtering by business type and tag ID, supports custom sorting
     */
    @PostMapping("/list")
    public ApiResponse<Page<FileInfoResponse>> listFiles(@Valid @RequestBody ListFilesRequest request) {
        Page<FileInfoResponse> response = fileService.listFiles(request);
        return ApiResponse.success(response);
    }

    /**
     * Query public files (no authentication required)
     * GET /api/files/public?page=1&pageSize=20
     * Returns all files with visibility = PUBLIC, sorted by creation time descending (newest first)
     */
    @GetMapping("/public")
    public ApiResponse<Page<FileInfoResponse>> listPublicFiles(@Valid @ModelAttribute ListPublicFilesRequest request) {
        Page<FileInfoResponse> response = fileService.listPublicFiles(request);
        return ApiResponse.success(response);
    }

    /**
     * Query file details
     * GET /api/files/detail
     */
    @GetMapping("/detail")
    public ApiResponse<FileDetailResponse> getFileDetail(
            @RequestParam Long fileId,
            @RequestParam(required = false) Boolean filterSensitive) {
        FileDetailResponse response = fileService.getFileDetail(fileId, filterSensitive);
        return ApiResponse.success(response);
    }

    // ========== File Download ==========
    
    /**
     * Download file
     * GET /api/files/download
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam Long fileId) {
        // Get file details
        FileDetailResponse fileDetail = fileService.getFileDetail(fileId);
        
        // Download file stream
        InputStream inputStream = fileService.downloadFile(fileId);
        
        // Set response headers
        String encodedFilename = URLEncoder.encode(fileDetail.getOriginalFilename(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                "attachment; filename*=UTF-8''" + encodedFilename);
        headers.add(HttpHeaders.CONTENT_TYPE, fileDetail.getContentType());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(new InputStreamResource(inputStream));
    }
    
    /**
     * Get file preview URL
     * GET /api/files/preview-url
     */
    @GetMapping("/preview-url")
    public ApiResponse<FilePreviewResponse> getPreviewUrl(
            @RequestParam Long fileId,
            @RequestParam(required = false) Integer expirySeconds) {
        FilePreviewResponse response = fileService.getPreviewUrl(fileId, expirySeconds);
        return ApiResponse.success(response);
    }
    
    // ========== File Management ==========
    
    /**
     * Update file properties (name, visibility, etc.)
     * PUT /api/files/update-properties
     */
    @PutMapping("/update-properties")
    public ApiResponse<FileInfoResponse> updateFileProperties(@Valid @RequestBody UpdateFilePropertiesRequest request) {
        FileInfoResponse response = fileService.updateFileProperties(request);
        return ApiResponse.success(response);
    }
    
    
    /**
     * Batch delete files
     * DELETE /api/files
     */
    @DeleteMapping
    public ApiResponse<BatchOperationResponse> batchDeleteFiles(@Valid @RequestBody BatchOperationRequest request) {
        BatchOperationResponse response = fileService.batchDeleteFiles(request);
        return ApiResponse.success(response);
    }
    
    // ========== File Tags ==========
    
    /**
     * Add tags to file
     * POST /api/files/add-tags
     */
    @PostMapping("/add-tags")
    public ApiResponse<List<TagResponse>> addFileTags(@Valid @RequestBody AddFileTagsRequest request) {
        List<TagResponse> response = fileService.addFileTags(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Remove file tag
     * DELETE /api/files/remove-tag
     */
    @DeleteMapping("/remove-tag")
    public ApiResponse<Boolean> removeFileTag(
            @RequestParam Long fileId,
            @RequestParam Long tagId) {
        Boolean result = fileService.removeFileTag(fileId, tagId);
        return ApiResponse.success(result);
    }
    
    /**
     * Query all tags of a file
     * GET /api/files/tags
     */
    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> getFileTags(@RequestParam Long fileId) {
        List<TagResponse> response = fileService.getFileTags(fileId);
        return ApiResponse.success(response);
    }
    
    // ========== File Sharing ==========
    
    /**
     * Create file share
     * POST /api/files/create-share
     */
    @PostMapping("/create-share")
    public ApiResponse<FileShareResponse> createFileShare(@Valid @RequestBody CreateFileShareRequest request) {
        FileShareResponse response = fileService.createFileShare(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Cancel share
     * DELETE /api/files/cancel-share
     */
    @DeleteMapping("/cancel-share")
    public ApiResponse<Boolean> cancelFileShare(@RequestParam Long shareId) {
        Boolean result = fileService.cancelFileShare(shareId);
        return ApiResponse.success(result);
    }
    
    /**
     * Query my outgoing shares list (shared files)
     * GET /api/files/shares/outgoing
     */
    @GetMapping("/shares/outgoing")
    public ApiResponse<Page<FileShareResponse>> listOutgoingShares(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<FileShareResponse> response = fileService.listOutgoingShares(page, size);
        return ApiResponse.success(response);
    }
    
    /**
     * Query incoming shares list
     * GET /api/files/shares/incoming
     */
    @GetMapping("/shares/incoming")
    public ApiResponse<Page<FileShareResponse>> listIncomingShares(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        Page<FileShareResponse> response = fileService.listIncomingShares(page, size);
        return ApiResponse.success(response);
    }
    
    /**
     * Query share details
     * GET /api/files/share-detail
     */
    @GetMapping("/share-detail")
    public ApiResponse<FileShareResponse> getShareDetail(@RequestParam Long shareId) {
        FileShareResponse response = fileService.getShareDetail(shareId);
        return ApiResponse.success(response);
    }
    
    // ========== Access Logs ==========
    
    /**
     * Query file access logs
     * POST /api/files/access-logs
     */
    @PostMapping("/access-logs")
    public ApiResponse<Page<FileAccessLogResponse>> getFileAccessLogs(@Valid @RequestBody GetFileAccessLogsRequest request) {
        Page<FileAccessLogResponse> response = fileService.getFileAccessLogs(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Query user file statistics
     * GET /api/files/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<FileStatisticsResponse> getFileStatistics() {
        FileStatisticsResponse response = fileService.getFileStatistics();
        return ApiResponse.success(response);
    }
    
    // ========== Recycle Bin Management ==========
    
    /**
     * Query recycle bin file list (supports pagination and sorting)
     * GET /api/files/trash
     */
    @GetMapping("/trash")
    public ApiResponse<Page<FileInfoResponse>> queryTrash(@Valid RecycleBinQueryRequest request) {
        Page<FileInfoResponse> response = fileService.queryTrash(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Batch restore files
     * POST /api/files/restore
     */
    @PostMapping("/restore")
    public ApiResponse<BatchOperationResponse> batchRestoreFiles(@Valid @RequestBody BatchOperationRequest request) {
        BatchOperationResponse response = fileService.batchRestoreFiles(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Batch permanently delete files
     * DELETE /api/files/permanent
     */
    @DeleteMapping("/permanent")
    public ApiResponse<BatchOperationResponse> batchPermanentDeleteFiles(@Valid @RequestBody BatchOperationRequest request) {
        BatchOperationResponse response = fileService.batchPermanentDeleteFiles(request);
        return ApiResponse.success(response);
    }
    
    /**
     * Empty recycle bin
     * DELETE /api/files/trash/empty
     */
    @DeleteMapping("/trash/empty")
    public ApiResponse<BatchOperationResponse> emptyRecycleBin() {
        BatchOperationResponse response = fileService.emptyRecycleBin();
        return ApiResponse.success(response);
    }
    
    /**
     * Get recycle bin statistics
     * GET /api/files/trash/stats
     */
    @GetMapping("/trash/stats")
    public ApiResponse<TrashStatsResponse> getTrashStats() {
        TrashStatsResponse response = fileService.getTrashStats();
        return ApiResponse.success(response);
    }
}
