package org.tech.ai.deepimage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO配置类
 * 负责创建MinioClient Bean并初始化默认存储桶
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
     * 应用启动时检查并创建默认存储桶
     */
    @PostConstruct
    public void initBucket() {
        try {
            MinioClient client = minioClient();
            String bucketName = minioProperties.getBucket();

            // 检查存储桶是否存在
            boolean exists = client.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // 创建存储桶
                client.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("创建默认存储桶成功: {}", bucketName);
            } else {
                log.info("默认存储桶已存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
        }
    }
}

