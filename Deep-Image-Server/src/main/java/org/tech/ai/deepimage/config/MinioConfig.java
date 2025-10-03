package org.tech.ai.deepimage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

/**
 * MinIO配置类
 * 负责创建MinioClient Bean并初始化默认存储桶
 * 使用事件监听器避免循环依赖问题
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioProperties minioProperties;

    /**
     * 创建MinioClient Bean
     * 
     * @return MinioClient实例
     */
    @Bean
    public MinioClient minioClient() {
        log.info("初始化MinIO客户端, endpoint: {}", minioProperties.getEndpoint());

        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * 监听应用完全启动就绪事件，初始化MinIO存储桶
     * 使用ApplicationReadyEvent确保在应用完全启动后执行，避免循环依赖
     * 从ApplicationContext中获取MinioClient，避免构造函数注入导致的循环依赖
     * 
     * @param event 应用就绪事件
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initMinioBucket(ApplicationReadyEvent event) {
        try {
            // 从ApplicationContext中获取MinioClient和MinioService，避免循环依赖
            MinioClient minioClient = event.getApplicationContext().getBean(MinioClient.class);
            String bucketName = minioProperties.getBucket();

            // 检查存储桶是否存在
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // 创建存储桶
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建默认存储桶成功: {}", bucketName);
            } else {
                log.info("默认存储桶已存在: {}", bucketName);
            }
            
            // 设置存储桶为公开只读访问（允许直接访问文件URL）
            setBucketPublicReadOnly(minioClient, bucketName);
            
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
        }
    }
    
    /**
     * 设置存储桶为公开只读访问
     * 允许匿名用户读取文件，但不能上传、删除等操作
     * 
     * @param minioClient MinIO客户端
     * @param bucketName 存储桶名称
     */
    private void setBucketPublicReadOnly(MinioClient minioClient, String bucketName) {
        try {
            // MinIO 存储桶策略 JSON
            // 注意：Principal 必须是 "*" 字符串，而不是 {"AWS": ["*"]} 对象
            // 这是 MinIO 特定的格式要求
            String policy = String.format("""
                {
                    "Version": "2012-10-17",
                    "Statement": [
                        {
                            "Effect": "Allow",
                            "Principal": "*",
                            "Action": ["s3:GetObject"],
                            "Resource": ["arn:aws:s3:::%s/*"]
                        }
                    ]
                }
                """, bucketName);

            SetBucketPolicyArgs args = SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build();

            minioClient.setBucketPolicy(args);
            log.info("设置存储桶公开只读访问成功: bucket={}", bucketName);

        } catch (Exception e) {
            log.error("设置存储桶公开访问失败: bucket={}, error={}", bucketName, e.getMessage(), e);
            // 不抛出异常，只记录日志，因为这不是致命错误
            log.warn("如需公开访问，请手动在 MinIO 控制台设置 bucket 策略");
        }
    }
}

