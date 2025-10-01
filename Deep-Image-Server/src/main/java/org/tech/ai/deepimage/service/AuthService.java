package org.tech.ai.deepimage.service;

import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.model.dto.request.RegisterGoogleUserRequest;
import org.tech.ai.deepimage.model.dto.request.LoginRequest;
import org.tech.ai.deepimage.model.dto.request.RegisterRequest;
import org.tech.ai.deepimage.model.dto.request.ResetPasswordRequest;
import org.tech.ai.deepimage.model.dto.response.TokenPairResponse;

public interface AuthService {
    TokenPairResponse loginByEmail(LoginRequest request);
    TokenPairResponse refreshToken(String refreshTokenPlain);
    Boolean register(RegisterRequest request);
    Boolean logout();
    Boolean resetPassword(ResetPasswordRequest request);
    
    /**
     * Google OAuth登录（内部方法）
     * @param user 用户实体
     * @return TokenPairResponse
     */
    TokenPairResponse loginByGoogle(User user);
    
    /**
     * Google用户自动注册
     * @param registerGoogleUserRequest Google用户信息（email, name, picture）
     * @return 用户实体
     */
    User registerGoogleUser(RegisterGoogleUserRequest registerGoogleUserRequest);
}


