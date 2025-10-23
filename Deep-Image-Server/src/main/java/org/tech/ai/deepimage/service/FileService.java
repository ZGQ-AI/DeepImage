package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.*;

import java.io.InputStream;
import java.util.List;

/**
 * 文件管理Service接口
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface FileService {
    
    // ========== 文件上传 ==========
    
    /**
     * 单文件上传
     * 
     * @param request 上传请求
     * @return 上传响应
     */
    FileUploadResponse uploadFile(UploadFileRequest request);
    
    /**
     * 检查文件是否已存在
     * 
     * @param request 文件存在性检查请求
     * @return 检查结果
     */
    FileExistsResponse checkFileExists(FileExistsCheckRequest request);
    
    // ========== 文件查询 ==========
    
    /**
     * 分页查询文件列表（统一接口）
     * 支持按业务类型、标签ID筛选，支持自定义排序
     * 
     * @param request 查询请求
     * @return 文件列表分页结果
     */
    Page<FileInfoResponse> listFiles(ListFilesRequest request);
    
    /**
     * 查询文件详情
     * 
     * @param fileId 文件ID
     * @return 文件详情
     */
    FileDetailResponse getFileDetail(Long fileId);
    
    // ========== 文件下载 ==========
    
    /**
     * 下载文件
     * 
     * @param fileId 文件ID
     * @return 文件输入流
     */
    InputStream downloadFile(Long fileId);
    
    /**
     * 获取文件预览URL
     * 
     * @param fileId 文件ID
     * @param expirySeconds 过期时间（秒）
     * @return 预览URL响应
     */
    FilePreviewResponse getPreviewUrl(Long fileId, Integer expirySeconds);
    
    // ========== 文件管理 ==========
    
    /**
     * 重命名文件
     * 
     * @param request 重命名请求
     * @return 文件信息
     */
    FileInfoResponse renameFile(RenameFileRequest request);
    
    /**
     * 删除文件（软删除）
     * 
     * @param fileId 文件ID
     * @return 是否成功
     */
    Boolean deleteFile(Long fileId);
    
    /**
     * 批量删除文件
     * 
     * @param request 批量操作请求
     * @return 批量操作响应
     */
    BatchOperationResponse batchDeleteFiles(BatchOperationRequest request);
    
    /**
     * 彻底删除文件（从MinIO删除）
     * 
     * @param fileId 文件ID
     * @return 是否成功
     */
    Boolean permanentDeleteFile(Long fileId);
    
    // ========== 文件标签 ==========
    
    /**
     * 为文件添加标签
     * 
     * @param request 添加标签请求
     * @return 文件的所有标签
     */
    List<TagResponse> addFileTags(AddFileTagsRequest request);
    
    /**
     * 移除文件标签
     * 
     * @param fileId 文件ID
     * @param tagId 标签ID
     * @return 是否成功
     */
    Boolean removeFileTag(Long fileId, Long tagId);
    
    /**
     * 查询文件的所有标签
     * 
     * @param fileId 文件ID
     * @return 标签列表
     */
    List<TagResponse> getFileTags(Long fileId);
    
    // ========== 文件分享 ==========
    
    /**
     * 创建文件分享
     * 
     * @param request 创建分享请求
     * @return 分享信息
     */
    FileShareResponse createFileShare(CreateFileShareRequest request);
    
    /**
     * 取消分享
     * 
     * @param shareId 分享ID
     * @return 是否成功
     */
    Boolean cancelFileShare(Long shareId);
    
    /**
     * 查询我的分享列表（分享出去的）
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分享列表分页结果
     */
    Page<FileShareResponse> listOutgoingShares(Integer page, Integer size);
    
    /**
     * 查询收到的分享列表
     * 
     * @param page 页码
     * @param size 每页大小
     * @return 分享列表分页结果
     */
    Page<FileShareResponse> listIncomingShares(Integer page, Integer size);
    
    /**
     * 查询分享详情
     * 
     * @param shareId 分享ID
     * @return 分享信息
     */
    FileShareResponse getShareDetail(Long shareId);
    
    // ========== 访问日志 ==========
    
    /**
     * 查询文件访问日志
     * 
     * @param request 查询请求
     * @return 访问日志分页结果
     */
    Page<FileAccessLogResponse> getFileAccessLogs(GetFileAccessLogsRequest request);
    
    /**
     * 查询用户文件统计
     * 
     * @return 统计信息
     */
    FileStatisticsResponse getFileStatistics();
    
    // ========== 回收站管理 ==========
    
    /**
     * 查询回收站文件列表（支持分页和排序）
     * 
     * @param request 分页查询请求
     * @return 回收站文件分页列表
     */
    Page<FileInfoResponse> queryTrash(RecycleBinQueryRequest request);
    
    /**
     * 批量恢复文件
     * 
     * @param request 批量操作请求
     * @return 操作结果
     */
    BatchOperationResponse batchRestoreFiles(BatchOperationRequest request);
    
    /**
     * 批量彻底删除文件
     * 
     * @param request 批量操作请求
     * @return 操作结果
     */
    BatchOperationResponse batchPermanentDeleteFiles(BatchOperationRequest request);
    
    /**
     * 清空回收站
     * 
     * @return 操作结果
     */
    BatchOperationResponse emptyRecycleBin();
    
    /**
     * 获取回收站统计信息
     * 
     * @return 统计信息
     */
    TrashStatsResponse getTrashStats();
}
