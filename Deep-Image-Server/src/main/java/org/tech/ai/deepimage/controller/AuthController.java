package org.tech.ai.deepimage.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tech.ai.deepimage.model.dto.request.LoginRequest;
import org.tech.ai.deepimage.model.dto.request.RefreshTokenRequest;
import org.tech.ai.deepimage.model.dto.request.RegisterRequest;
import org.tech.ai.deepimage.model.dto.request.ResetPasswordRequest;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;
import org.tech.ai.deepimage.model.dto.response.TokenPairResponse;
import org.tech.ai.deepimage.service.AuthService;

@RestController
@Validated
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

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

  
}


