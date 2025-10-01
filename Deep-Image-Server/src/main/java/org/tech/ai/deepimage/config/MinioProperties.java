package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * MinIO配置属性
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinioProperties {

    /**
     * MinIO服务端点地址
     * 例如: http://localhost:9000
     */
    private String endpoint;

    /**
     * 访问密钥 (Access Key)
     */
    private String accessKey;

    /**
     * 密钥 (Secret Key)
     */
    private String secretKey;

    /**
     * 默认存储桶名称
     */
    private String bucket;
}

