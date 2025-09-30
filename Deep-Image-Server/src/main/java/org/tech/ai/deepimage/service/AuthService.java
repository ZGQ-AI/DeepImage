package org.tech.ai.deepimage.service;

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
}


