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
 * MinIO对象存储服务实现
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

    // ========== 文件上传 ==========

    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType) {
        return uploadFile(minioProperties.getBucket(), inputStream, objectName, contentType);
    }

    @Override
    public String uploadFile(String bucketName, InputStream inputStream, String objectName, String contentType) {
        try {
            // 构建上传参数
            PutObjectArgs args = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(contentType != null ? contentType : "application/octet-stream")
                    .build();

            // 执行上传
            minioClient.putObject(args);

            log.info("文件上传成功: bucket={}, object={}", bucketName, objectName);
            return getFileUrl(bucketName, objectName);

        } catch (Exception e) {
            log.error("文件上传失败: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.serverError("文件上传失败: " + e.getMessage());
        }
    }

    // ========== 文件下载 ==========

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
            log.info("文件下载成功: bucket={}, object={}", bucketName, objectName);
            return stream;

        } catch (Exception e) {
            log.error("文件下载失败: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.notFound("文件不存在: " + objectName);
        }
    }

    // ========== 文件删除 ==========

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
            log.info("文件删除成功: bucket={}, object={}", bucketName, objectName);

        } catch (Exception e) {
            log.error("文件删除失败: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.serverError("文件删除失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteFiles(List<String> objectNames) {
        deleteFiles(minioProperties.getBucket(), objectNames);
    }

    @Override
    public void deleteFiles(String bucketName, List<String> objectNames) {
        try {
            // 构建删除对象列表
            List<DeleteObject> objects = objectNames.stream()
                    .map(DeleteObject::new)
                    .collect(Collectors.toList());

            RemoveObjectsArgs args = RemoveObjectsArgs.builder()
                    .bucket(bucketName)
                    .objects(objects)
                    .build();

            // 执行批量删除
            Iterable<Result<DeleteError>> results = minioClient.removeObjects(args);

            // 检查删除结果
            for (Result<DeleteError> result : results) {
                DeleteError error = result.get();
                log.error("文件删除失败: bucket={}, object={}, message={}",
                        bucketName, error.objectName(), error.message());
            }

            log.info("批量删除文件成功: bucket={}, count={}", bucketName, objectNames.size());

        } catch (Exception e) {
            log.error("批量删除文件失败: bucket={}", bucketName, e);
            throw BusinessException.serverError("批量删除文件失败: " + e.getMessage());
        }
    }

    // ========== 预签名URL ==========

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
            log.info("获取预签名URL成功: bucket={}, object={}, method={}", bucketName, objectName, method);
            return url;

        } catch (Exception e) {
            log.error("获取预签名URL失败: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.serverError("获取预签名URL失败: " + e.getMessage());
        }
    }

    // ========== 文件查询 ==========

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

            log.info("列出文件成功: bucket={}, prefix={}, count={}", bucketName, prefix, files.size());
            return files;

        } catch (Exception e) {
            log.error("列出文件失败: bucket={}, prefix={}", bucketName, prefix, e);
            throw BusinessException.serverError("列出文件失败: " + e.getMessage());
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

            log.info("获取文件信息成功: bucket={}, object={}", bucketName, objectName);
            return info;

        } catch (Exception e) {
            log.error("获取文件信息失败: bucket={}, object={}", bucketName, objectName, e);
            throw BusinessException.notFound("文件不存在: " + objectName);
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

    // ========== 存储桶管理 ==========

    @Override
    public boolean bucketExists(String bucketName) {
        try {
            BucketExistsArgs args = BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build();

            return minioClient.bucketExists(args);

        } catch (Exception e) {
            log.error("检查存储桶失败: bucket={}", bucketName, e);
            throw BusinessException.serverError("检查存储桶失败: " + e.getMessage());
        }
    }

    @Override
    public void createBucket(String bucketName) {
        try {
            if (bucketExists(bucketName)) {
                log.warn("存储桶已存在: bucket={}", bucketName);
                return;
            }

            MakeBucketArgs args = MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build();

            minioClient.makeBucket(args);
            log.info("创建存储桶成功: bucket={}", bucketName);

        } catch (Exception e) {
            log.error("创建存储桶失败: bucket={}", bucketName, e);
            throw BusinessException.serverError("创建存储桶失败: " + e.getMessage());
        }
    }

    @Override
    public void deleteBucket(String bucketName) {
        try {
            RemoveBucketArgs args = RemoveBucketArgs.builder()
                    .bucket(bucketName)
                    .build();

            minioClient.removeBucket(args);
            log.info("删除存储桶成功: bucket={}", bucketName);

        } catch (Exception e) {
            log.error("删除存储桶失败: bucket={}", bucketName, e);
            throw BusinessException.serverError("删除存储桶失败: " + e.getMessage());
        }
    }

    @Override
    public List<String> listBuckets() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            List<String> bucketNames = buckets.stream()
                    .map(Bucket::name)
                    .collect(Collectors.toList());

            log.info("列出存储桶成功: count={}", bucketNames.size());
            return bucketNames;

        } catch (Exception e) {
            log.error("列出存储桶失败", e);
            throw BusinessException.serverError("列出存储桶失败: " + e.getMessage());
        }
    }

    // ========== 复制和移动 ==========

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
            log.info("复制对象成功: {}:{} -> {}:{}", sourceBucket, sourceObject, targetBucket, targetObject);

        } catch (Exception e) {
            log.error("复制对象失败: {}:{} -> {}:{}", sourceBucket, sourceObject, targetBucket, targetObject, e);
            throw BusinessException.serverError("复制对象失败: " + e.getMessage());
        }
    }

    @Override
    public void copyObject(String sourceObject, String targetObject) {
        String bucket = minioProperties.getBucket();
        copyObject(bucket, sourceObject, bucket, targetObject);
    }

    // ========== 私有工具方法 ==========

    /**
     * 获取文件访问URL
     * 
     * @param bucketName 存储桶名称
     * @param objectName 对象名称
     * @return 文件访问URL
     */
    private String getFileUrl(String bucketName, String objectName) {
        return String.format("%s/%s/%s", minioProperties.getEndpoint(), bucketName, objectName);
    }
}

