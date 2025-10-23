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
     * 发起Google OAuth登录
     * 重定向用户到Google授权页面
     */
    @GetMapping("/google/login")
    public void googleLogin(@RequestParam String fromUrl,
                            HttpServletResponse response) throws IOException {
        try {
            String authorizationUrl = googleOauthService.generateAuthorizationUrl(fromUrl);
            log.info("重定向到Google OAuth授权页面");
            response.sendRedirect(authorizationUrl);
        } catch (Exception e) {
            log.error("生成Google OAuth授权URL失败", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    ResponseConstant.GOOGLE_OAUTH_SERVICE_UNAVAILABLE);
        }
    }

    /**
     * Google OAuth回调处理
     * 处理授权码，获取用户信息，重定向到fromUrl并带上token
     */
    @GetMapping("/google/callback")
    public void googleCallback(@RequestParam(required = false) String code,
                                @RequestParam(required = false) String state,
                                @RequestParam(required = false) String error,
                                HttpServletRequest request,
                                HttpServletResponse response) throws IOException {
        // 尝试从state中提取fromUrl，失败则使用默认值或Referer
        String fromUrl = ResponseConstant.DEFAULT_FRONTEND_URL;
        
        try {
            // 1. 使用Helper验证回调参数
            GoogleOauthHelper.OauthCallbackValidation validation =
                    GoogleOauthHelper.validateCallbackParams(code, state, error);

            // 2. 尽早解析state参数，提取fromUrl（用于后续错误重定向）
            if (StringUtils.hasText(state)) {
                try {
                    fromUrl = GoogleOauthHelper.parseStateString(state);
                    log.info("解析state参数成功，fromUrl: {}", fromUrl);
                } catch (Exception e) {
                    log.warn("解析state参数失败，尝试使用Referer", e);
                    String referer = request.getHeader("Referer");
                    if (StringUtils.hasText(referer)) {
                        fromUrl = referer;
                    }
                }
            }

            if (!validation.isValid()) {
                log.error("OAuth回调参数验证失败: {}", validation.getErrorMessage());
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        validation.getErrorCode()
                );
                response.sendRedirect(errorUrl);
                return;
            }

            // 3. 使用授权码获取访问令牌
            Map<String, Object> tokenResponse = googleOauthService.exchangeCodeForToken(
                    validation.getCode());
            
            if (!GoogleOauthHelper.validateTokenResponse(tokenResponse)) {
                log.error("获取Google访问令牌失败");
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        ResponseConstant.GOOGLE_OAUTH_TOKEN_EXCHANGE_FAILED
                );
                response.sendRedirect(errorUrl);
                return;
            }

            // 4. 直接获取id_token，无需额外API调用
            String idToken = (String) tokenResponse.get(GoogleOauthHelper.ID_TOKEN);
            if (!StringUtils.hasText(idToken)) {
                log.error("Google OAuth响应中缺少id_token");
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        ResponseConstant.GOOGLE_OAUTH_MISSING_ID_TOKEN
                );
                response.sendRedirect(errorUrl);
                return;
            }

            log.info("成功获取Google id_token");

            // 5. 从Google id_token中提取用户信息
            String email = JwtUtil.getClaimAsString(idToken, JwtClaimConstant.EMAIL);
            String name = JwtUtil.getClaimAsString(idToken, JwtClaimConstant.NAME);
            String picture = JwtUtil.getClaimAsString(idToken, JwtClaimConstant.PICTURE);
            
            if (!StringUtils.hasText(email)) {
                log.error("无法从Google id_token中提取email");
                String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                        fromUrl,
                        ResponseConstant.GOOGLE_OAUTH_MISSING_EMAIL
                );
                response.sendRedirect(errorUrl);
                return;
            }

            log.info("从Google id_token中提取用户信息，email: {}, name: {}, picture: {}", 
                    email, name, picture);

            // 6. 构建GoogleUserInfo对象
            RegisterGoogleUserRequest registerGoogleUserRequest = new RegisterGoogleUserRequest(email, name, picture);

            // 7. 检查用户是否存在，不存在则自动注册
            User user = authService.registerGoogleUser(registerGoogleUserRequest);
            log.info("Google用户准备就绪，userId: {}", user.getId());

            // 8. 执行登录，创建Session和RefreshToken
            TokenPairResponse tokenPair = authService.loginByGoogle(user);

            // 9. 使用Helper构建成功重定向URL，带上新生成的token
            String successUrl = GoogleOauthHelper.buildSuccessRedirectUrl(
                    fromUrl,
                    tokenPair.getAccessToken(),
                    tokenPair.getRefreshToken()
            );

            log.info("Google OAuth登录成功，重定向到: {}", successUrl);
            response.sendRedirect(successUrl);

        } catch (Exception e) {
            log.error("处理Google OAuth回调时发生异常", e);
            // 使用前面提取的fromUrl，如果还没有则使用默认值
            String errorUrl = GoogleOauthHelper.buildErrorRedirectUrl(
                    fromUrl,
                    ResponseConstant.GOOGLE_OAUTH_CALLBACK_PROCESSING_FAILED
            );
            response.sendRedirect(errorUrl);
        }
    }
}


