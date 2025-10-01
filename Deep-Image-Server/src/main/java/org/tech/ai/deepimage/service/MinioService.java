package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.model.dto.response.FileObjectInfoResponse;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO对象存储服务接口
 * 封装MinIO常用操作API
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface MinioService {

    // ========== 文件上传 ==========

    /**
     * 上传文件(从输入流)
     * 
     * @param inputStream 文件输入流
     * @param objectName  对象名称(存储路径)
     * @param contentType 内容类型(MIME类型)
     * @return 文件访问URL
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType);

    /**
     * 上传文件到指定存储桶
     * 
     * @param bucketName  存储桶名称
     * @param inputStream 文件输入流
     * @param objectName  对象名称
     * @param contentType 内容类型
     * @return 文件访问URL
     */
    String uploadFile(String bucketName, InputStream inputStream, String objectName, String contentType);

    // ========== 文件下载 ==========

    /**
     * 下载文件
     * 
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String objectName);

    /**
     * 下载指定存储桶的文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件输入流
     */
    InputStream downloadFile(String bucketName, String objectName);

    // ========== 文件删除 ==========

    /**
     * 删除文件
     * 
     * @param objectName 对象名称
     */
    void deleteFile(String objectName);

    /**
     * 删除指定存储桶的文件
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     */
    void deleteFile(String bucketName, String objectName);

    /**
     * 批量删除文件
     * 
     * @param objectNames 对象名称列表
     */
    void deleteFiles(List<String> objectNames);

    /**
     * 批量删除指定存储桶的文件
     * 
     * @param bucketName  存储桶名称
     * @param objectNames 对象名称列表
     */
    void deleteFiles(String bucketName, List<String> objectNames);

    // ========== 预签名URL ==========

    /**
     * 获取上传预签名URL (用于前端直传)
     * 
     * @param objectName 对象名称
     * @param expires    过期时间(秒)
     * @return 预签名URL
     */
    String getPresignedUploadUrl(String objectName, int expires);

    /**
     * 获取下载预签名URL
     * 
     * @param objectName 对象名称
     * @param expires    过期时间(秒)
     * @return 预签名下载URL
     */
    String getPresignedDownloadUrl(String objectName, int expires);

    /**
     * 获取预签名URL (指定存储桶)
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @param expires    过期时间(秒)
     * @param isUpload   是否为上传URL
     * @return 预签名URL
     */
    String getPresignedUrl(String bucketName, String objectName, int expires, boolean isUpload);

    // ========== 文件查询 ==========

    /**
     * 列出文件
     * 
     * @param prefix 前缀(可用于目录过滤)
     * @return 文件对象信息列表
     */
    List<FileObjectInfoResponse> listFiles(String prefix);

    /**
     * 列出指定存储桶的文件
     * 
     * @param bucketName 存储桶名称
     * @param prefix     前缀
     * @param recursive  是否递归列出
     * @return 文件对象信息列表
     */
    List<FileObjectInfoResponse> listFiles(String bucketName, String prefix, boolean recursive);

    /**
     * 获取文件信息
     * 
     * @param objectName 对象名称
     * @return 文件对象信息
     */
    FileObjectInfoResponse getFileInfo(String objectName);

    /**
     * 获取指定存储桶的文件信息
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件对象信息
     */
    FileObjectInfoResponse getFileInfo(String bucketName, String objectName);

    /**
     * 判断文件是否存在
     * 
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String objectName);

    /**
     * 判断指定存储桶的文件是否存在
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 是否存在
     */
    boolean fileExists(String bucketName, String objectName);

    // ========== 存储桶管理 ==========

    /**
     * 检查存储桶是否存在
     * 
     * @param bucketName 存储桶名称
     * @return 是否存在
     */
    boolean bucketExists(String bucketName);

    /**
     * 创建存储桶
     * 
     * @param bucketName 存储桶名称
     */
    void createBucket(String bucketName);

    /**
     * 删除存储桶
     * 
     * @param bucketName 存储桶名称
     */
    void deleteBucket(String bucketName);

    /**
     * 列出所有存储桶
     * 
     * @return 存储桶名称列表
     */
    List<String> listBuckets();

    // ========== 复制和移动 ==========

    /**
     * 复制对象
     * 
     * @param sourceBucket 源存储桶
     * @param sourceObject 源对象名称
     * @param targetBucket 目标存储桶
     * @param targetObject 目标对象名称
     */
    void copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject);

    /**
     * 复制对象(同一存储桶内)
     * 
     * @param sourceObject 源对象名称
     * @param targetObject 目标对象名称
     */
    void copyObject(String sourceObject, String targetObject);
}

