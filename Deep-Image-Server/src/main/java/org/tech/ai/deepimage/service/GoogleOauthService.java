package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.config.GoogleOauthProperties;
import org.tech.ai.deepimage.util.GoogleOauthHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

/**
 * Google OAuth服务
 * 专门处理OAuth2.0流程，不涉及数据库存储
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Service
@Slf4j
public class GoogleOauthService {

    @Autowired
    private GoogleOauthProperties properties;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * 生成Google OAuth授权URL
     */
    public String generateAuthorizationUrl(String fromUrl) {
        String scope = String.join(" ", properties.getScope());
        String state = GoogleOauthHelper.buildStateString(fromUrl);
        
        String url = GoogleOauthHelper.buildAuthorizationUrl(
            GoogleOauthHelper.authorizationUrl()
                .baseUrl(properties.getAuthorizationUri())
                .clientId(properties.getClientId())
                .scope(scope)
                .redirectUri(properties.getRedirectUri())
                .accessType(properties.getAccessType())
                .prompt(properties.getPrompt())
                .state(state)
                .nonce(generateNonce())
                .build()
        );
        
        log.info("生成Google OAuth授权URL，fromUrl: {}", fromUrl);
        return url;
    }

    /**
     * 生成nonce值（用于防止重放攻击）
     */
    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 使用授权码获取访问令牌
     */
    public Map<String, Object> exchangeCodeForToken(String code) {
        try {
            // 使用Helper构建请求参数
            Map<String, String> params = GoogleOauthHelper.buildTokenRequestParams(
                GoogleOauthHelper.tokenRequest()
                    .code(code)
                    .clientId(properties.getClientId())
                    .clientSecret(properties.getClientSecret())
                    .redirectUri(properties.getRedirectUri())
                    .build()
            );
            
            // 转换为MultiValueMap
            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
            params.forEach(requestBody::add);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                properties.getTokenUri(),
                HttpMethod.POST,
                request,
                Map.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                log.info("成功获取Google访问令牌");
                return response.getBody();
            } else {
                log.error("获取Google访问令牌失败: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("获取Google访问令牌时发生异常", e);
            return null;
        }
    }
}

