package org.tech.ai.deepimage.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tech.ai.deepimage.annotation.LogParams;
import org.tech.ai.deepimage.constant.JwtClaimConstant;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.model.dto.request.RegisterGoogleUserRequest;
import org.tech.ai.deepimage.model.dto.request.LoginRequest;
import org.tech.ai.deepimage.model.dto.request.RefreshTokenRequest;
import org.tech.ai.deepimage.model.dto.request.RegisterRequest;
import org.tech.ai.deepimage.model.dto.request.ResetPasswordRequest;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;
import org.tech.ai.deepimage.model.dto.response.TokenPairResponse;
import org.tech.ai.deepimage.service.AuthService;
import org.tech.ai.deepimage.service.GoogleOauthService;
import org.tech.ai.deepimage.util.GoogleOauthHelper;
import org.tech.ai.deepimage.util.JwtUtil;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/auth")
@LogParams
public class AuthController {

    @Autowired
    private AuthService authService;
    
    @Autowired
    private GoogleOauthService googleOauthService;

    @PostMapping("/login")
    public ApiResponse<TokenPairResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenPairResponse tokenPair = authService.loginByEmail(request);
        return ApiResponse.success(tokenPair);
    }

    @PostMapping("/register")
    public ApiResponse<Boolean> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.success(authService.register(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenPairResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        TokenPairResponse tokenPair = authService.refreshToken(request.getRefreshToken());
        return ApiResponse.success(tokenPair);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Boolean> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        Boolean ok = authService.resetPassword(request);
        return ApiResponse.success(ok);
    }

    @PostMapping("/logout")
    public ApiResponse<Boolean> logout() {
        Boolean ok = authService.logout();
        return ApiResponse.success(ok);
    }

    /**
     * Initiate Google OAuth login
     * Redirect user to Google authorization page
     */
    @GetMapping("/google/login")
    public void googleLogin(@RequestParam String fromUrl,
                            HttpServletResponse response) throws IOException {
        try {
            String authorizationUrl = googleOauthService.generateAuthorizationUrl(fromUrl);
            log.info("Redirecting to Google OAuth authorization page");
            response.sendRedirect(authorizationUrl);
        } catch (Exception e) {
            log.error("Failed to generate Google OAuth authorization URL", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    ResponseConstant.GOOGLE_OAUTH_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Google OAuth callback handler
     * Process authorization code, get user information, redirect to fromUrl with token
     */
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String error,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        // Try to extract fromUrl from state, fallback to default value or Referer on failure
        String fromUrl = ResponseConstant.DEFAULT_FRONTEND_URL;
        
        try {
            // 1. Use Helper to validate callback parameters
            GoogleOauthHelper.OauthCallbackValidation validation =
                    GoogleOauthHelper.validateCallbackParams(code, state, error);

            // 2. Parse state parameter early, extract fromUrl (for subsequent error redirect)
            if (StringUtils.hasText(state)) {
                try {
                    fromUrl = GoogleOauthHelper.parseStateString(state);
                    log.info("Successfully parsed state parameter, fromUrl: {}", fromUrl);
                } catch (Exception e) {
                    log.warn("Failed to parse state parameter, trying to use Referer", e);
                    String referer = request.getHeader("Referer");
                    if (StringUtils.hasText(referer)) {
                        fromUrl = referer;
                    }
                }
            }

            if (!validation.isValid()) {
                log.error("OAuth callback parameter validation failed: {}", validation.getErrorMessage());
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        validation.getErrorCode()
                );
                response.sendRedirect(errorUrl);
                return;
            }

            // 3. Use authorization code to get access token
            Map<String, Object> tokenResponse = googleOauthService.exchangeCodeForToken(
                    validation.getCode());
            
            if (!GoogleOauthHelper.validateTokenResponse(tokenResponse)) {
                log.error("Failed to get Google access token");
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        ResponseConstant.GOOGLE_OAUTH_TOKEN_EXCHANGE_FAILED
                );
                response.sendRedirect(errorUrl);
                return;
            }

            // 4. Directly get id_token, no additional API call needed
            String idToken = (String) tokenResponse.get(GoogleOauthHelper.ID_TOKEN);
            if (!StringUtils.hasText(idToken)) {
                log.error("Missing id_token in Google OAuth response");
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        ResponseConstant.GOOGLE_OAUTH_MISSING_ID_TOKEN
                );
                response.sendRedirect(errorUrl);
                return;
            }

            log.info("Successfully obtained Google id_token");

            // 5. Extract user information from Google id_token
            String email = JwtUtil.getClaimAsString(idToken, JwtClaimConstant.EMAIL);
            String name = JwtUtil.getClaimAsString(idToken, JwtClaimConstant.NAME);
            String picture = JwtUtil.getClaimAsString(idToken, JwtClaimConstant.PICTURE);
            
            if (!StringUtils.hasText(email)) {
                log.error("Unable to extract email from Google id_token");
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        ResponseConstant.GOOGLE_OAUTH_MISSING_EMAIL
                );
                response.sendRedirect(errorUrl);
                return;
            }

            log.info("Extracted user information from Google id_token, email: {}, name: {}, picture: {}", 
                    email, name, picture);

            // 6. Build GoogleUserInfo object
            RegisterGoogleUserRequest registerGoogleUserRequest = new RegisterGoogleUserRequest(email, name, picture);

            // 7. Check if user exists, auto-register if not exists
            User user = authService.registerGoogleUser(registerGoogleUserRequest);
            log.info("Google user ready, userId: {}", user.getId());

            // 8. Perform login, create Session and RefreshToken
            TokenPairResponse tokenPair = authService.loginByGoogle(user);

            // 9. Use Helper to build success redirect URL with newly generated token
            String successUrl = GoogleOauthHelper.buildSuccessRedirectUrl(
                    fromUrl,
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken()
            );

            log.info("Google OAuth login successful, redirecting to: {}", successUrl);
            response.sendRedirect(successUrl);

        } catch (Exception e) {
            log.error("Exception occurred while processing Google OAuth callback", e);
            // Use previously extracted fromUrl, or default value if not available
            String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                    fromUrl,
                    ResponseConstant.GOOGLE_OAUTH_CALLBACK_PROCESSING_FAILED
            );
            response.sendRedirect(errorUrl);
        }
    }
}


