package org.tech.ai.deepimage.util;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.tech.ai.deepimage.constant.ResponseConstant;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Google OAuth工具类
 * 处理OAuth流程中的URL构建、参数处理等操作
 * 
 * @author zgq
 * @since 2025-10-01
 */
@Slf4j
public class GoogleOauthHelper {

    private static final String PARAM_CLIENT_ID = "client_id";
    private static final String PARAM_RESPONSE_TYPE = "response_type";
    private static final String PARAM_SCOPE = "scope";
    private static final String PARAM_REDIRECT_URI = "redirect_uri";
    private static final String PARAM_ACCESS_TYPE = "access_type";
    private static final String PARAM_PROMPT = "prompt";
    private static final String PARAM_STATE = "state";
    private static final String PARAM_NONCE = "nonce";
    private static final String PARAM_CODE = "code";
    private static final String PARAM_CLIENT_SECRET = "client_secret";
    private static final String PARAM_GRANT_TYPE = "grant_type";
    
    private static final String RESPONSE_TYPE_CODE = "code";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    
    private static final String PARAM_ACCESS_TOKEN = "access_token";
    private static final String PARAM_REFRESH_TOKEN = "refresh_token";
    private static final String PARAM_LOGIN_ERROR = "loginError";
    private static final String PARAM_LOGIN_SUCCESS = "loginSuccess";
    public static final String ID_TOKEN = "id_token";

    /**
     * 构建Google OAuth授权URL
     */
    public static String buildAuthorizationUrl(AuthorizationUrlParams params) {
        try {
            StringBuilder url = new StringBuilder(params.getBaseUrl());
            url.append("?").append(PARAM_CLIENT_ID).append("=")
                    .append(encodeParameter(params.getClientId()));
            url.append("&").append(PARAM_RESPONSE_TYPE).append("=")
                    .append(RESPONSE_TYPE_CODE);
            url.append("&").append(PARAM_SCOPE).append("=")
                    .append(encodeParameter(params.getScope()));
            url.append("&").append(PARAM_REDIRECT_URI).append("=")
                    .append(encodeParameter(params.getRedirectUri()));
            url.append("&").append(PARAM_ACCESS_TYPE).append("=").append(params.getAccessType());
            url.append("&").append(PARAM_PROMPT).append("=").append(params.getPrompt());

            if (StringUtils.hasText(params.getState())) {
                url.append("&").append(PARAM_STATE).append("=")
                        .append(encodeParameter(params.getState()));
            }

            // 添加nonce参数（安全考虑）
            if (StringUtils.hasText(params.getNonce())) {
                url.append("&").append(PARAM_NONCE).append("=")
                        .append(encodeParameter(params.getNonce()));
            }

            log.debug("构建授权URL: {}", url);
            return url.toString();
        } catch (Exception e) {
            log.error("构建授权URL失败", e);
            throw new RuntimeException("构建授权URL失败", e);
        }
    }

    /**
     * 构建OAuth回调重定向URL（成功）
     */
    public static String buildSuccessRedirectUrl(String fromUrl, String accessToken, String refreshToken) {
        try {
            if (!StringUtils.hasText(fromUrl)) {
                fromUrl = "http://localhost:5173"; // 默认前端地址
            }

            StringBuilder url = new StringBuilder(fromUrl);
            String separator = fromUrl.contains("?") ? "&" : "?";

            url.append(separator).append(PARAM_ACCESS_TOKEN).append("=")
                    .append(encodeParameter(accessToken));
            url.append("&").append(PARAM_REFRESH_TOKEN).append("=")
                    .append(encodeParameter(refreshToken));

            log.debug("构建成功重定向URL: {}", url);
            return url.toString();
        } catch (Exception e) {
            log.error("构建成功重定向URL失败", e);
            throw new RuntimeException("构建成功重定向URL失败", e);
        }
    }

    /**
     * 构建OAuth回调重定向URL（失败）
     */
    public static String buildErrorRedirectUrl(String fromUrl, String error) {
        try {
            if (!StringUtils.hasText(fromUrl)) {
                fromUrl = "http://localhost:5173"; // 默认前端地址
            }

            StringBuilder url = new StringBuilder(fromUrl);
            String separator = fromUrl.contains("?") ? "&" : "?";

            url.append(separator).append(PARAM_LOGIN_ERROR)
                    .append("=").append(encodeParameter(error));
            url.append("&").append(PARAM_LOGIN_SUCCESS).append("=false");

            log.debug("构建错误重定向URL: {}", url);
            return url.toString();
        } catch (Exception e) {
            log.error("构建错误重定向URL失败", e);
            throw new RuntimeException("构建错误重定向URL失败", e);
        }
    }

    /**
     * 构建OAuth token请求参数
     */
    public static Map<String, String> buildTokenRequestParams(TokenRequestParams params) {
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put(PARAM_CODE, params.getCode());
        requestParams.put(PARAM_CLIENT_ID, params.getClientId());
        requestParams.put(PARAM_CLIENT_SECRET, params.getClientSecret());
        requestParams.put(PARAM_REDIRECT_URI, params.getRedirectUri());
        requestParams.put(PARAM_GRANT_TYPE, GRANT_TYPE_AUTHORIZATION_CODE);

        log.debug("构建token请求参数: {}", requestParams);
        return requestParams;
    }

    /**
     * 验证OAuth回调参数
     */
    public static OauthCallbackValidation validateCallbackParams(String code, String state, String error) {
        OauthCallbackValidation validation = new OauthCallbackValidation();

        if (StringUtils.hasText(error)) {
            validation.setValid(false);
            validation.setErrorCode(ResponseConstant.GOOGLE_OAUTH_AUTHORIZATION_FAILED);
            validation.setErrorMessage("Google OAuth authorization failed: " + error);
            return validation;
        }

        if (!StringUtils.hasText(code)) {
            validation.setValid(false);
            validation.setErrorCode(ResponseConstant.GOOGLE_OAUTH_MISSING_CODE);
            validation.setErrorMessage("Missing Google OAuth authorization code");
            return validation;
        }

        validation.setValid(true);
        validation.setCode(code);
        validation.setState(state);
        return validation;
    }

    /**
     * 验证token响应
     */
    public static boolean validateTokenResponse(Map<String, Object> tokenResponse) {
        if (tokenResponse == null) {
            log.error("Token响应为空");
            return false;
        }

        // 检查id_token
        if (tokenResponse.containsKey(ID_TOKEN)) {
            String idToken = getStringValue(tokenResponse, ID_TOKEN);
            if (StringUtils.hasText(idToken)) {
                log.debug("Token响应验证通过，使用id_token");
                return true;
            }
        }

        log.error("Token响应中缺少有效的id_token字段");
        return false;
    }

    /**
     * 从Map中安全获取字符串值
     */
    private static String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * URL编码参数
     */
    private static String encodeParameter(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("URL编码失败，使用原始值: {}", value);
            return value;
        }
    }

    /**
     * 构建state字符串（Base64URL编码fromUrl）
     */
    public static String buildStateString(String fromUrl) {
        if (!StringUtils.hasText(fromUrl)) {
            throw new IllegalArgumentException("fromUrl不能为空");
        }
        return base64UrlEncode(fromUrl);
    }

    /**
     * 解析state字符串
     */
    public static String parseStateString(String state) {
        if (!StringUtils.hasText(state)) {
            throw new IllegalArgumentException("state参数不能为空");
        }

        try {
            return base64UrlDecode(state);
        } catch (Exception e) {
            log.error("解析state参数失败: {}", state, e);
            throw new RuntimeException("解析state参数失败", e);
        }
    }

    /**
     * Base64URL编码
     */
    private static String base64UrlEncode(String input) {
        try {
            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Base64URL编码失败", e);
            throw new RuntimeException("Base64URL编码失败", e);
        }
    }

    /**
     * Base64URL解码
     */
    private static String base64UrlDecode(String input) {
        try {
            return new String(Base64.getUrlDecoder().decode(input), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Base64URL解码失败", e);
            throw new RuntimeException("Base64URL解码失败", e);
        }
    }

    /**
     * 授权URL构建参数
     */
    @Data
    public static class AuthorizationUrlParams {
        private String baseUrl;
        private String clientId;
        private String scope;
        private String redirectUri;
        private String accessType;
        private String prompt;
        private String state;
        private String nonce;
    }

    /**
     * Token请求参数
     */
    @Data
    public static class TokenRequestParams {
        private String code;
        private String clientId;
        private String clientSecret;
        private String redirectUri;
    }

    /**
     * OAuth回调参数验证结果
     */
    @Data
    public static class OauthCallbackValidation {
        private boolean valid;
        private String code;
        private String state;
        private String errorCode;
        private String errorMessage;
    }

    /**
     * 创建AuthorizationUrlParams构建器
     */
    public static AuthorizationUrlParamsBuilder authorizationUrl() {
        return new AuthorizationUrlParamsBuilder();
    }

    /**
     * 创建TokenRequestParams构建器
     */
    public static TokenRequestParamsBuilder tokenRequest() {
        return new TokenRequestParamsBuilder();
    }

    public static class AuthorizationUrlParamsBuilder {
        private final AuthorizationUrlParams params = new AuthorizationUrlParams();

        public AuthorizationUrlParamsBuilder baseUrl(String baseUrl) {
            params.setBaseUrl(baseUrl);
            return this;
        }

        public AuthorizationUrlParamsBuilder clientId(String clientId) {
            params.setClientId(clientId);
            return this;
        }

        public AuthorizationUrlParamsBuilder scope(String scope) {
            params.setScope(scope);
            return this;
        }

        public AuthorizationUrlParamsBuilder redirectUri(String redirectUri) {
            params.setRedirectUri(redirectUri);
            return this;
        }

        public AuthorizationUrlParamsBuilder accessType(String accessType) {
            params.setAccessType(accessType);
            return this;
        }

        public AuthorizationUrlParamsBuilder prompt(String prompt) {
            params.setPrompt(prompt);
            return this;
        }

        public AuthorizationUrlParamsBuilder state(String state) {
            params.setState(state);
            return this;
        }

        public AuthorizationUrlParamsBuilder nonce(String nonce) {
            params.setNonce(nonce);
            return this;
        }

        public AuthorizationUrlParams build() {
            return params;
        }
    }

    public static class TokenRequestParamsBuilder {
        private final TokenRequestParams params = new TokenRequestParams();

        public TokenRequestParamsBuilder code(String code) {
            params.setCode(code);
            return this;
        }

        public TokenRequestParamsBuilder clientId(String clientId) {
            params.setClientId(clientId);
            return this;
        }

        public TokenRequestParamsBuilder clientSecret(String clientSecret) {
            params.setClientSecret(clientSecret);
            return this;
        }

        public TokenRequestParamsBuilder redirectUri(String redirectUri) {
            params.setRedirectUri(redirectUri);
            return this;
        }

        public TokenRequestParams build() {
            return params;
        }
    }
}

