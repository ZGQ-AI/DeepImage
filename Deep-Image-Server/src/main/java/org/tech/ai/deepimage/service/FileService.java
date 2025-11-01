package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;

import java.io.InputStream;
import java.util.List;

/**
 * File Management Service Interface
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface FileService {
    
    // ========== File Upload ==========
    
    /**
     * Single file upload
     * 
     * @param request Upload request
     * @return Upload response
     */
    FileUploadResponse uploadFile(UploadFileRequest request);
    
    /**
     * Check if file exists
     * 
     * @param request File existence check request
     * @return Check result
     */
    FileExistsResponse checkFileExists(FileExistsCheckRequest request);
    
    // ========== File Query ==========
    
    /**
     * Paginated file list query (unified interface)
     * Supports filtering by business type and tag ID, supports custom sorting
     * 
     * @param request Query request
     * @return File list pagination result
     */
    Page<FileInfoResponse> listFiles(ListFilesRequest request);
    
    /**
     * Query public files (no authentication required)
     * Returns all files with visibility = PUBLIC
     * 
     * @param request Query request
     * @return Public file list pagination result
     */
    Page<FileInfoResponse> listPublicFiles(ListPublicFilesRequest request);
    
    /**
     * Query file details
     * 
     * @param fileId File ID
     * @return File details
     */
    FileDetailResponse getFileDetail(Long fileId);
    
    // ========== File Download ==========
    
    /**
     * Download file
     * 
     * @param fileId File ID
     * @return File input stream
     */
    InputStream downloadFile(Long fileId);
    
    /**
     * Get file preview URL
     * 
     * @param fileId File ID
     * @param expirySeconds Expiration time (seconds)
     * @return Preview URL response
     */
    FilePreviewResponse getPreviewUrl(Long fileId, Integer expirySeconds);
    
    // ========== File Management ==========
    
    /**
     * Rename file
     * 
     * @param request Rename request
     * @return File information
     */
    FileInfoResponse renameFile(RenameFileRequest request);
    
    /**
     * Delete file (soft delete)
     * 
     * @param fileId File ID
     * @return Whether successful
     */
    Boolean deleteFile(Long fileId);
    
    /**
     * Batch delete files
     * 
     * @param request Batch operation request
     * @return Batch operation response
     */
    BatchOperationResponse batchDeleteFiles(BatchOperationRequest request);
    
    /**
     * Permanently delete file (delete from MinIO)
     * 
     * @param fileId File ID
     * @return Whether successful
     */
    Boolean permanentDeleteFile(Long fileId);
    
    // ========== File Tags ==========
    
    /**
     * Add tags to file
     * 
     * @param request Add tags request
     * @return All tags of the file
     */
    List<TagResponse> addFileTags(AddFileTagsRequest request);
    
    /**
     * Remove file tag
     * 
     * @param fileId File ID
     * @param tagId Tag ID
     * @return Whether successful
     */
    Boolean removeFileTag(Long fileId, Long tagId);
    
    /**
     * Query all tags of a file
     * 
     * @param fileId File ID
     * @return Tag list
     */
    List<TagResponse> getFileTags(Long fileId);
    
    // ========== File Sharing ==========
    
    /**
     * Create file share
     * 
     * @param request Create share request
     * @return Share information
     */
    FileShareResponse createFileShare(CreateFileShareRequest request);
    
    /**
     * Cancel share
     * 
     * @param shareId Share ID
     * @return Whether successful
     */
    Boolean cancelFileShare(Long shareId);
    
    /**
     * Query my outgoing shares list (shared files)
     * 
     * @param page Page number
     * @param size Page size
     * @return Share list pagination result
     */
    Page<FileShareResponse> listOutgoingShares(Integer page, Integer size);
    
    /**
     * Query incoming shares list
     * 
     * @param page Page number
     * @param size Page size
     * @return Share list pagination result
     */
    Page<FileShareResponse> listIncomingShares(Integer page, Integer size);
    
    /**
     * Query share details
     * 
     * @param shareId Share ID
     * @return Share information
     */
    FileShareResponse getShareDetail(Long shareId);
    
    // ========== Access Logs ==========
    
    /**
     * Query file access logs
     * 
     * @param request Query request
     * @return Access logs pagination result
     */
    Page<FileAccessLogResponse> getFileAccessLogs(GetFileAccessLogsRequest request);
    
    /**
     * Query user file statistics
     * 
     * @return Statistics information
     */
    FileStatisticsResponse getFileStatistics();
    
    // ========== Recycle Bin Management ==========
    
    /**
     * Query recycle bin file list (supports pagination and sorting)
     * 
     * @param request Pagination query request
     * @return Recycle bin file pagination list
     */
    Page<FileInfoResponse> queryTrash(RecycleBinQueryRequest request);
    
    /**
     * Batch restore files
     * 
     * @param request Batch operation request
     * @return Operation result
     */
    BatchOperationResponse batchRestoreFiles(BatchOperationRequest request);
    
    /**
     * Batch permanently delete files
     * 
     * @param request Batch operation request
     * @return Operation result
     */
    BatchOperationResponse batchPermanentDeleteFiles(BatchOperationRequest request);
    
    /**
     * Empty recycle bin
     * 
     * @return Operation result
     */
    BatchOperationResponse emptyRecycleBin();
    
    /**
     * Get recycle bin statistics
     * 
     * @return Statistics information
     */
    TrashStatsResponse getTrashStats();
}
