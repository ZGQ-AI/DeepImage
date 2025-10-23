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
 * 文件管理Controller
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
    
    // ========== 文件上传 ==========
    
    /**
     * 单文件上传
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
     * 检查文件是否已存在
     * POST /api/files/check-exists
     */
    @PostMapping("/check-exists")
    public ApiResponse<FileExistsResponse> checkFileExists(@Valid @RequestBody FileExistsCheckRequest request) {
        FileExistsResponse response = fileService.checkFileExists(request);
        return ApiResponse.success(response);
    }
    
    // ========== 文件查询 ==========
    
    /**
     * 分页查询文件列表（统一接口）
     * POST /api/files/list
     * 支持按业务类型、标签ID筛选，支持自定义排序
     */
    @PostMapping("/list")
    public ApiResponse<Page<FileInfoResponse>> listFiles(@Valid @RequestBody ListFilesRequest request) {
        Page<FileInfoResponse> response = fileService.listFiles(request);
        return ApiResponse.success(response);
    }

    /**
     * 查询文件详情
     * GET /api/files/detail
     */
    @GetMapping("/detail")
    public ApiResponse<FileDetailResponse> getFileDetail(@RequestParam Long fileId) {
        FileDetailResponse response = fileService.getFileDetail(fileId);
        return ApiResponse.success(response);
    }

    // ========== 文件下载 ==========
    
    /**
     * 下载文件
     * GET /api/files/download
     */
    @GetMapping("/download")
    public ResponseEntity<InputStreamResource> downloadFile(@RequestParam Long fileId) {
        // 获取文件详情
        FileDetailResponse fileDetail = fileService.getFileDetail(fileId);
        
        // 下载文件流
        InputStream inputStream = fileService.downloadFile(fileId);
        
        // 设置响应头
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
     * 获取文件预览URL
     * GET /api/files/preview-url
     */
    @GetMapping("/preview-url")
    public ApiResponse<FilePreviewResponse> getPreviewUrl(
            @RequestParam Long fileId,
            @RequestParam(required = false) Integer expirySeconds) {
        FilePreviewResponse response = fileService.getPreviewUrl(fileId, expirySeconds);
        return ApiResponse.success(response);
    }
    
    // ========== 文件管理 ==========
    
    /**
     * 重命名文件
     * POST /api/files/rename
     */
    @PostMapping("/rename")
    public ApiResponse<FileInfoResponse> renameFile(@Valid @RequestBody RenameFileRequest request) {
        FileInfoResponse response = fileService.renameFile(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 批量删除文件
     * DELETE /api/files
     */
    @DeleteMapping
    public ApiResponse<BatchOperationResponse> batchDeleteFiles(@Valid @RequestBody BatchOperationRequest request) {
        BatchOperationResponse response = fileService.batchDeleteFiles(request);
        return ApiResponse.success(response);
    }
    
    // ========== 文件标签 ==========
    
    /**
     * 为文件添加标签
     * POST /api/files/add-tags
     */
    @PostMapping("/add-tags")
    public ApiResponse<List<TagResponse>> addFileTags(@Valid @RequestBody AddFileTagsRequest request) {
        List<TagResponse> response = fileService.addFileTags(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 移除文件标签
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
     * 查询文件的所有标签
     * GET /api/files/tags
     */
    @GetMapping("/tags")
    public ApiResponse<List<TagResponse>> getFileTags(@RequestParam Long fileId) {
        List<TagResponse> response = fileService.getFileTags(fileId);
        return ApiResponse.success(response);
    }
    
    // ========== 文件分享 ==========
    
    /**
     * 创建文件分享
     * POST /api/files/create-share
     */
    @PostMapping("/create-share")
    public ApiResponse<FileShareResponse> createFileShare(@Valid @RequestBody CreateFileShareRequest request) {
        FileShareResponse response = fileService.createFileShare(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 取消分享
     * DELETE /api/files/cancel-share
     */
    @DeleteMapping("/cancel-share")
    public ApiResponse<Boolean> cancelFileShare(@RequestParam Long shareId) {
        Boolean result = fileService.cancelFileShare(shareId);
        return ApiResponse.success(result);
    }
    
    /**
     * 查询我的分享列表（分享出去的）
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
     * 查询收到的分享列表
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
     * 查询分享详情
     * GET /api/files/share-detail
     */
    @GetMapping("/share-detail")
    public ApiResponse<FileShareResponse> getShareDetail(@RequestParam Long shareId) {
        FileShareResponse response = fileService.getShareDetail(shareId);
        return ApiResponse.success(response);
    }
    
    // ========== 访问日志 ==========
    
    /**
     * 查询文件访问日志
     * POST /api/files/access-logs
     */
    @PostMapping("/access-logs")
    public ApiResponse<Page<FileAccessLogResponse>> getFileAccessLogs(@Valid @RequestBody GetFileAccessLogsRequest request) {
        Page<FileAccessLogResponse> response = fileService.getFileAccessLogs(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 查询用户文件统计
     * GET /api/files/statistics
     */
    @GetMapping("/statistics")
    public ApiResponse<FileStatisticsResponse> getFileStatistics() {
        FileStatisticsResponse response = fileService.getFileStatistics();
        return ApiResponse.success(response);
    }
    
    // ========== 回收站管理 ==========
    
    /**
     * 查询回收站文件列表（支持分页和排序）
     * GET /api/files/trash
     */
    @GetMapping("/trash")
    public ApiResponse<Page<FileInfoResponse>> queryTrash(@Valid RecycleBinQueryRequest request) {
        Page<FileInfoResponse> response = fileService.queryTrash(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 批量恢复文件
     * POST /api/files/restore
     */
    @PostMapping("/restore")
    public ApiResponse<BatchOperationResponse> batchRestoreFiles(@Valid @RequestBody BatchOperationRequest request) {
        BatchOperationResponse response = fileService.batchRestoreFiles(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 批量彻底删除文件
     * DELETE /api/files/permanent
     */
    @DeleteMapping("/permanent")
    public ApiResponse<BatchOperationResponse> batchPermanentDeleteFiles(@Valid @RequestBody BatchOperationRequest request) {
        BatchOperationResponse response = fileService.batchPermanentDeleteFiles(request);
        return ApiResponse.success(response);
    }
    
    /**
     * 清空回收站
     * DELETE /api/files/trash/empty
     */
    @DeleteMapping("/trash/empty")
    public ApiResponse<BatchOperationResponse> emptyRecycleBin() {
        BatchOperationResponse response = fileService.emptyRecycleBin();
        return ApiResponse.success(response);
    }
    
    /**
     * 获取回收站统计信息
     * GET /api/files/trash/stats
     */
    @GetMapping("/trash/stats")
    public ApiResponse<TrashStatsResponse> getTrashStats() {
        TrashStatsResponse response = fileService.getTrashStats();
        return ApiResponse.success(response);
    }
}
