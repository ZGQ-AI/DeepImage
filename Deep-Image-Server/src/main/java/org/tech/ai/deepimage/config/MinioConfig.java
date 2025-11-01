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
 * MinIO Configuration Class
 * Responsible for creating MinioClient Bean and initializing default bucket
 * Uses event listener to avoid circular dependency issues
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
     * Create MinioClient Bean
     * 
     * @return MinioClient instance
     */
    @Bean
    public MinioClient minioClient() {
        log.info("Initializing MinIO client, endpoint: {}", minioProperties.getEndpoint());

        return MinioClient.builder()
                .endpoint(minioProperties.getEndpoint())
                .credentials(minioProperties.getAccessKey(), minioProperties.getSecretKey())
                .build();
    }

    /**
     * Listen to application ready event, initialize MinIO bucket
     * Uses ApplicationReadyEvent to ensure execution after application fully starts, avoiding circular dependency
     * Gets MinioClient from ApplicationContext to avoid circular dependency caused by constructor injection
     * 
     * @param event Application ready event
     */
    @EventListener(ApplicationReadyEvent.class)
    public void initMinioBucket(ApplicationReadyEvent event) {
        try {
            // Get MinioClient and MinioService from ApplicationContext to avoid circular dependency
            MinioClient minioClient = event.getApplicationContext().getBean(MinioClient.class);
            String bucketName = minioProperties.getBucket();

            // Check if bucket exists
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );

            if (!exists) {
                // Create bucket
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Created default bucket successfully: {}", bucketName);
            } else {
                log.info("Default bucket already exists: {}", bucketName);
            }
            
            // Set bucket to public read-only access (allows direct file URL access)
            setBucketPublicReadOnly(minioClient, bucketName);
            
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
        }
    }
    
    /**
     * Set bucket to public read-only access
     * Allows anonymous users to read files, but not upload, delete, etc.
     * 
     * @param minioClient MinIO client
     * @param bucketName Bucket name
     */
    private void setBucketPublicReadOnly(MinioClient minioClient, String bucketName) {
        try {
            // MinIO bucket policy JSON
            // Note: Principal must be "*" string, not {"AWS": ["*"]} object
            // This is a MinIO-specific format requirement
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
            log.info("Set bucket public read-only access successfully: bucket={}", bucketName);

        } catch (Exception e) {
            log.error("Failed to set bucket public access: bucket={}, error={}", bucketName, e.getMessage(), e);
            // Don't throw exception, only log, as this is not a fatal error
            log.warn("If public access is needed, please manually set bucket policy in MinIO console");
        }
    }
}

