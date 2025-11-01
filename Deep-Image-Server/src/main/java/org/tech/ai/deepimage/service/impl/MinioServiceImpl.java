package org.tech.ai.deepimage.service.impl;

import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.config.MinioProperties;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.model.dto.response.FileObjectInfoResponse;
import org.tech.ai.deepimage.service.MinioService;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MinIO object storage service implementation
 * 
 * @author zgq
* @since 2025-10-02
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    // ========== File Upload ==========

    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType) {
        return uploadFile(minioProperties.getBucket(), inputStream, objectName, contentType);
    }

    @Override
    public String uploadFile(String bucketName, InputStream inputStream, String objectName, String contentType) {
        try {
            // Build upload parameters
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            // Execute upload
            minioClient.putObject(args);

            log.info("File upload successful: bucket={}, object={}", bucketName, objectName);
            return getFileUrl(bucketName, objectName);

        } catch (Exception e) {
            log.error("File upload failed: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.serverError("File upload failed: " + e.getMessage());
        }
    }

    // ========== File Download ==========

    @Override
    public InputStream downloadFile(String objectName) {
        return downloadFile(minioProperties.getBucket(), objectName);
    }

    @Override
    public InputStream downloadFile(String bucketName, String objectName) {
        try {
            GetObjectArgs args = GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            InputStream stream = minioClient.getObject(args);
            log.info("File download successful: bucket={}, object={}", bucketName, objectName);
            return stream;

        } catch (Exception e) {
            log.error("File download failed: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.notFound("File not found: " + objectName);
        }
    }

    // ========== File Delete ==========

    @Override
    public void deleteFile(String objectName) {
        deleteFile(minioProperties.getBucket(), objectName);
    }

    @Override
    public void deleteFile(String bucketName, String objectName) {
        try {
            RemoveObjectArgs args = RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.removeObject(args);
            log.info("File delete successful: bucket={}, object={}", bucketName, objectName);

        } catch (Exception e) {
            log.error("File delete failed: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.serverError("File delete failed: " + e.getMessage());
        }
    }

    @Override
    public void deleteFiles(List<String> objectNames) {
        deleteFiles(minioProperties.getBucket(), objectNames);
    }

    @Override
    public void deleteFiles(String bucketName, List<String> objectNames) {
        try {
            // Build delete object list
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());

            RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(objects)
                    .build();

            // Execute batch delete
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);

            // Check delete results
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("File delete failed: bucket={}, object={}, message={}",
                        bucketName, error.objectName(), error.message());
            }

            log.info("Batch delete files successful: bucket={}, count={}", bucketName, objectNames.size());

        } catch (Exception e) {
            log.error("Batch delete files failed: bucket={}", bucketName, e);
            throw BusinessException.serverError("Batch delete files failed: " + e.getMessage());
        }
    }

    // ========== Presigned URL ==========

    @Override
    public String getPresignedUploadUrl(String objectName, int expires) {
        return getPresignedUrl(minioProperties.getBucket(), objectName, expires, true);
    }

    @Override
    public String getPresignedDownloadUrl(String objectName, int expires) {
        return getPresignedUrl(minioProperties.getBucket(), objectName, expires, false);
    }

    @Override
    public String getPresignedUrl(String bucketName, String objectName, int expires, boolean isUpload) {
        try {
            Method method = isUpload ? Method.PUT : Method.GET;

            GetPresignedObjectUrlArgs args = GetPresignedObjectUrlArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .method(method)
                    .expiry(expires)
                    .build();

            String url = minioClient.getPresignedObjectUrl(args);
            log.info("Get presigned URL successful: bucket={}, object={}, method={}", bucketName, objectName, method);
            return url;

        } catch (Exception e) {
            log.error("Get presigned URL failed: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.serverError("Failed to get presigned URL: " + e.getMessage());
        }
    }

    // ========== File Query ==========

    @Override
    public List<FileObjectInfoResponse> listFiles(String prefix) {
        return listFiles(minioProperties.getBucket(), prefix, false);
    }

    @Override
    public List<FileObjectInfoResponse> listFiles(String bucketName, String prefix, boolean recursive) {
        try {
            ListObjectsArgs args = ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .prefix(prefix != null ? prefix : "")
                    .recursive(recursive)
                    .build();

            Iterable<Result<Item>> results = minioClient.listObjects(args);

            List<FileObjectInfoResponse> files = new ArrayList<>();
            for (Result<Item> result : results) {
                Item item = result.get();
                FileObjectInfoResponse info = FileObjectInfoResponse.from(item);
                info.setUrl(getFileUrl(bucketName, item.objectName()));
                files.add(info);
            }

            log.info("List files successful: bucket={}, prefix={}, count={}", bucketName, prefix, files.size());
            return files;

        } catch (Exception e) {
            log.error("List files failed: bucket={}, prefix={}", bucketName, prefix, e);
            throw BusinessException.serverError("Failed to list files: " + e.getMessage());
        }
    }

    @Override
    public FileObjectInfoResponse getFileInfo(String objectName) {
        return getFileInfo(minioProperties.getBucket(), objectName);
    }

    @Override
    public FileObjectInfoResponse getFileInfo(String bucketName, String objectName) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            StatObjectResponse stat = minioClient.statObject(args);
            FileObjectInfoResponse info = FileObjectInfoResponse.from(stat);
            info.setUrl(getFileUrl(bucketName, objectName));

            log.info("Get file info successful: bucket={}, object={}", bucketName, objectName);
            return info;

        } catch (Exception e) {
            log.error("Get file info failed: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.notFound("File not found: " + objectName);
        }
    }

    @Override
    public boolean fileExists(String objectName) {
        return fileExists(minioProperties.getBucket(), objectName);
    }

    @Override
    public boolean fileExists(String bucketName, String objectName) {
        try {
            StatObjectArgs args = StatObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build();

            minioClient.statObject(args);
            return true;

        } catch (Exception e) {
            return false;
        }
    }

    // ========== Bucket Management ==========

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            BucketExistsArgs args = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();

            return minioClient.bucketExists(args);

        } catch (Exception e) {
            log.error("Check bucket failed: bucket={}", bucketName, e);
            throw BusinessException.serverError("Failed to check bucket: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            if (bucketExists(bucketName)) {
                log.warn("Bucket already exists: bucket={}", bucketName);
                return;
            }

            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();

            minioClient.makeBucket(args);
            log.info("Create bucket successful: bucket={}", bucketName);

        } catch (Exception e) {
            log.error("Create bucket failed: bucket={}", bucketName, e);
            throw BusinessException.serverError("Failed to create bucket: " + e.getMessage());
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            RemoveBucketArgs args = RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build();

            minioClient.removeBucket(args);
            log.info("Delete bucket successful: bucket={}", bucketName);

        } catch (Exception e) {
            log.error("Delete bucket failed: bucket={}", bucketName, e);
            throw BusinessException.serverError("Failed to delete bucket: " + e.getMessage());
        }
    }

    @Override
    public List<String> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<String> bucketNames = buckets.stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());

            log.info("List buckets successful: count={}", bucketNames.size());
            return bucketNames;

        } catch (Exception e) {
            log.error("List buckets failed", e);
            throw BusinessException.serverError("Failed to list buckets: " + e.getMessage());
        }
    }

    // ========== Copy and Move ==========

    @Override
    public void copyObject(String sourceBucket, String sourceObject, String targetBucket, String targetObject) {
        try {
            CopySource source = CopySource.builder()
                    .bucket(sourceBucket)
                    .object(sourceObject)
                    .build();

            CopyObjectArgs args = CopyObjectArgs.builder()
                    .source(source)
                    .bucket(targetBucket)
                    .object(targetObject)
                    .build();

            minioClient.copyObject(args);
            log.info("Copy object successful: {}:{} -> {}:{}", sourceBucket, sourceObject, targetBucket, targetObject);

        } catch (Exception e) {
            log.error("Copy object failed: {}:{} -> {}:{}", sourceBucket, sourceObject, targetBucket, targetObject, e);
            throw BusinessException.serverError("Failed to copy object: " + e.getMessage());
        }
    }

    @Override
    public void copyObject(String sourceObject, String targetObject) {
        String bucket = minioProperties.getBucket();
        copyObject(bucket, sourceObject, bucket, targetObject);
    }

    // ========== Private Helper Methods ==========

    /**
     * Get file access URL
     * 
     * @param bucketName Bucket name
     * @param objectName Object name
     * @return File access URL
     */
    private String getFileUrl(String bucketName, String objectName) {
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, objectName);
    }
}

