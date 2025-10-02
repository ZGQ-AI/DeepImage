package org.tech.ai.deepimage.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
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
            // 从ApplicationContext中获取MinioClient，避免循环依赖
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
        } catch (Exception e) {
            log.error("初始化MinIO存储桶失败", e);
        }
    }
}

