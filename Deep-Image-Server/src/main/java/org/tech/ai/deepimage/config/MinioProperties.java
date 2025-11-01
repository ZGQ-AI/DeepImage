package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO Configuration Properties
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO service endpoint address
     * Example: http://localhost:9000
     */
    private String endpoint;

    /**
     * Access Key
     */
    private String accessKey;

    /**
     * Secret Key
     */
    private String secretKey;

    /**
     * Default bucket name
     */
    private String bucket;
}

