package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "deepimage.auth")
public class RefreshTokenProperties {
    private long refreshTtlSeconds;
}


