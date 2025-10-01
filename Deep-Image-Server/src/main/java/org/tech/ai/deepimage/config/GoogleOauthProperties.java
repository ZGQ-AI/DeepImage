package org.tech.ai.deepimage.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Google OAuth配置属性
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "deepimage.auth.google")
public class GoogleOauthProperties {

    /**
     * Google OAuth2 client id
     */
    private String clientId;

    /**
     * Google OAuth2 client secret
     */
    private String clientSecret;

    /**
     * Callback redirect uri
     */
    private String redirectUri;

    /**
     * Authorization endpoint
     */
    private String authorizationUri;

    /**
     * Token endpoint
     */
    private String tokenUri;

    /**
     * OAuth scope list
     */
    private List<String> scope;

    /**
     * Access type (online/offline)
     */
    private String accessType;

    /**
     * Prompt type (none/consent/select_account)
     */
    private String prompt;
}

