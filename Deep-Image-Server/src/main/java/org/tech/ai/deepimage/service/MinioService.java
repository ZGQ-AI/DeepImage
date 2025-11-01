package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.model.dto.response.FileObjectInfoResponse;

import java.io.InputStream;
import java.util.List;

/**
 * MinIO object storage service interface
 * Encapsulates common MinIO operation APIs
 * 
 * @author zgq
 * @since 2025-10-02
 */
public interface MinioService {

    // ========== File Upload ==========

    /**
     * Upload file (from input stream)
     * 
     * @param inputStream File input stream
     * @param objectName  Object name (storage path)
     * @param contentType Content type (MIME type)
     * @return File access URL
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType);

    /**
     * Upload file to specified bucket
     * 
     * @param bucketName  Bucket name
     * @param inputStream File input stream
     * @param objectName  Object name
     * @param contentType Content type
     * @return File access URL
     */
    String uploadFile(String bucketName, InputStream inputStream, String objectName, String contentType);

    // ========== File Download ==========

    /**
     * Download file
     * 
     * @param objectName Object name
     * @return File input stream
     */
    InputStream downloadFile(String objectName);

    /**
     * Download file from specified bucket
     * 
     * @param bucketName Bucket name
     * @param objectName Object name
     * @return File input stream
     */
    InputStream downloadFile(String bucketName, String objectName);

    // ========== File Delete ==========

    /**
     * Delete file
     * 
     * @param objectName Object name
     */
    void deleteFile(String objectName);

    /**
     * Delete file from specified bucket
     * 
     * @param bucketName Bucket name
     * @param objectName Object name
     */
    void deleteFile(String bucketName, String objectName);

    /**
     * Batch delete files
     * 
     * @param objectNames Object name list
     */
    void deleteFiles(List<String> objectNames);

    /**
     * Batch delete files from specified bucket
     * 
     * @param bucketName  Bucket name
     * @param objectNames Object name list
     */
    void deleteFiles(String bucketName, List<String> objectNames);

    // ========== Presigned URL ==========

    /**
     * Get presigned upload URL (for direct frontend upload)
     * 
     * @param objectName Object name
     * @param expires    Expiry time (seconds)
     * @return Presigned URL
     */
    String getPresignedUploadUrl(String objectName, int expires);

    /**
     * Get presigned download URL
     * 
     * @param objectName Object name
     * @param expires    Expiry time (seconds)
     * @return Presigned download URL
     */
    String getPresignedDownloadUrl(String objectName, int expires);

    /**
     * Get presigned URL (specified bucket)
     * 
     * @param bucketName Bucket name
     * @param objectName Object name
     * @param expires    Expiry time (seconds)
     * @param isUpload   Whether it is upload URL
     * @return Presigned URL
     */
    String getPresignedUrl(String bucketName, String objectName, int expires, boolean isUpload);

    // ========== File Query ==========

    /**
     * List files
     * 
     * @param prefix Prefix (can be used for directory filtering)
     * @return File object information list
     */
    List<FileObjectInfoResponse> listFiles(String prefix);

    /**
     * List files from specified bucket
     * 
     * @param bucketName Bucket name
     * @param prefix     Prefix
     * @param recursive  Whether to list recursively
     * @return File object information list
     */
    List<FileObjectInfoResponse> listFiles(String bucketName, String prefix, boolean recursive);

    /**
     * Get file information
     * 
     * @param objectName Object name
     * @return File object information
     */
    FileObjectInfoResponse getFileInfo(String objectName);

    /**
     * Get file information from specified bucket
     * 
     * @param bucketName Bucket name
     * @param objectName Object name
     * @return File object information
     */
    FileObjectInfoResponse getFileInfo(String bucketName, String objectName);

    /**
     * Check if file exists
     * 
     * @param objectName Object name
     * @return Whether exists
     */
    boolean fileExists(String objectName);

    /**
     * Check if file exists in specified bucket
     * 
     * @param bucketName Bucket name
     * @param objectName Object name
     * @return Whether exists
     */
    boolean fileExists(String bucketName, String objectName);

    // ========== Bucket Management ==========

    /**
     * Check if bucket exists
     * 
     * @param bucketName Bucket name
     * @return Whether exists
     */
    boolean bucketExists(String bucketName);

    /**
     * Create bucket
     * 
     * @param bucketName Bucket name
     */
    void createBucket(String bucketName);

    /**
     * Delete bucket
     * 
     * @param bucketName Bucket name
     */
    void deleteBucket(String bucketName);

    /**
     * List all buckets
     * 
     * @return Bucket name list
     */
    List<String> listBuckets();

    // ========== Copy and Move ==========

    /**
     * Copy object
     * 
     * @param sourceBucket Source bucket
     * @param sourceObject Source object name
     * @param targetBucket Target bucket
     * @param targetObject Target object name
     */
    void copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject);

    /**
     * Copy object (within same bucket)
     * 
     * @param sourceObject Source object name
     * @param targetObject Target object name
     */
    void copyObject(String sourceObject, String targetObject);
}

