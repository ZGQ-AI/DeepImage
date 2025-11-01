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
 * Google OAuth service
 * Specifically handles OAuth 2.0 flow, does not involve database storage
 * 
 * @author zgq
 * @since 2025-10-02
 */
@Service
@Slf4j
public class GoogleOauthService {

    @Autowired
    private GoogleOauthProperties properties;
    
    @Autowired
    private RestTemplate restTemplate;

    /**
     * Generate Google OAuth authorization URL
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
        
        log.info("Generated Google OAuth authorization URL, fromUrl: {}", fromUrl);
        return url;
    }

    /**
     * Generate nonce value (used to prevent replay attacks)
     */
    private String generateNonce() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * Exchange authorization code for access token
     */
    public Map<String, Object> exchangeCodeForToken(String code) {
        try {
            // Use Helper to build request parameters
            Map<String, String> params = GoogleOauthHelper.buildTokenRequestParams(
                GoogleOauthHelper.tokenRequest()
                    .code(code)
                    .clientId(properties.getClientId())
                    .clientSecret(properties.getClientSecret())
                    .redirectUri(properties.getRedirectUri())
                    .build()
            );
            
            // Convert to MultiValueMap
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
                log.info("Successfully obtained Google access token");
                return response.getBody();
            } else {
                log.error("Failed to get Google access token: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Exception occurred while getting Google access token", e);
            return null;
        }
    }
}

