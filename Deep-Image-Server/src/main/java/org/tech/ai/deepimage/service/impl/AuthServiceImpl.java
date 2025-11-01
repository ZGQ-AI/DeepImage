package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.constant.JwtClaimConstant;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.entity.RefreshToken;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.enums.RefreshTokenStatusEnum;
import org.tech.ai.deepimage.enums.SessionStatusEnum;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.model.dto.request.*;
import org.tech.ai.deepimage.model.dto.response.TokenPairResponse;
import org.tech.ai.deepimage.service.AuthService;
import org.tech.ai.deepimage.service.RefreshTokenService;
import org.tech.ai.deepimage.service.SessionService;
import org.tech.ai.deepimage.service.UserService;
import org.tech.ai.deepimage.util.ConditionalUtil;
import org.tech.ai.deepimage.util.CryptoUtil;
import org.tech.ai.deepimage.util.HttpRequestUtil;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private SessionService sessionService;


    @Override
    public TokenPairResponse loginByEmail(LoginRequest request) {
        // Query user
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));

        BusinessException.throwIf(user == null,
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.INVALID_CREDENTIALS_MESSAGE);
        BusinessException.throwIf(Boolean.FALSE.equals(user.getVerified()),
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.EMAIL_NOT_VERIFIED_MESSAGE);

        // Validate password
        boolean passwordOk = CryptoUtil.match(request.getPassword(), user.getPasswordHash());
        BusinessException.throwIf(!passwordOk,
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.INVALID_CREDENTIALS_MESSAGE);

        // Login and generate JWT (with necessary user information)
        SaLoginModel model = new SaLoginModel();
        model.setExtra(JwtClaimConstant.USERNAME, user.getUsername());
        model.setExtra(JwtClaimConstant.EMAIL, user.getEmail());
        model.setExtra(JwtClaimConstant.AVATAR_URL, user.getAvatarUrl());
        StpUtil.login(user.getId(), model);
        String accessToken = StpUtil.getTokenValue();

        // Extract IP and UA from request headers
        String ip = HttpRequestUtil.extractClientIp();
        String userAgent = HttpRequestUtil.extractUserAgent();

        // Persist session
        Session session = new Session();
        session.setUserId(user.getId());
        session.setAccessTokenHash(CryptoUtil.sha256Hex(accessToken));
        session.setDeviceInfo(null);
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);
        session.setLastRefreshAt(LocalDateTime.now());
        sessionService.save(session);


        // Generate and persist refresh token
        CreateRefreshTokenRequest createReq = new CreateRefreshTokenRequest();
        createReq.setUserId(user.getId());
        createReq.setSessionId(session.getId());
        String refreshTokenPlain = refreshTokenService.createAndStoreRefreshToken(createReq);

        return new TokenPairResponse(accessToken, refreshTokenPlain);
    }

    @Override
    public TokenPairResponse refreshToken(String refreshTokenPlain) {
        // 1. Verify refresh token validity
        RefreshToken refreshToken = refreshTokenService.verifyAndGet(refreshTokenPlain);
        BusinessException.throwIf(refreshToken == null, ResponseConstant.UNAUTHORIZED, ResponseConstant.INVALID_REFRESH_TOKEN_MESSAGE);

        // 2. Revoke old refresh token
        refreshTokenService.revoke(refreshTokenPlain);

        // 3. Get user information
        User user = userService.getById(refreshToken.getUserId());
        BusinessException.throwIf(user == null, ResponseConstant.UNAUTHORIZED, ResponseConstant.USER_NOT_FOUND_MESSAGE);

        // 4. Generate new access token
        StpUtil.login(user.getId(), new SaLoginModel()
                .setExtra(JwtClaimConstant.USERNAME, user.getUsername())
                .setExtra(JwtClaimConstant.EMAIL, user.getEmail())
                .setExtra(JwtClaimConstant.AVATAR_URL, user.getAvatarUrl()));
        String accessToken = StpUtil.getTokenValue();

        // 5. Update session information
        Session session = sessionService.getById(refreshToken.getSessionId());
        ConditionalUtil.ifNotNull(session,s->{
            session.setAccessTokenHash(CryptoUtil.sha256Hex(accessToken));
            session.setLastRefreshAt(LocalDateTime.now());
            sessionService.updateById(s);
        });

        // 6. Generate new refresh token
        CreateRefreshTokenRequest createReq = new CreateRefreshTokenRequest();
        createReq.setUserId(user.getId());
        createReq.setSessionId(refreshToken.getSessionId());
        String newRefreshTokenPlain = refreshTokenService.createAndStoreRefreshToken(createReq);

        // 7. Return new token pair
        return new TokenPairResponse(accessToken, newRefreshTokenPlain);
    }

    @Override
    public Boolean register(RegisterRequest request) {
        // Validate username/email globally unique (not distinguishing logical deletion)
        BusinessException.throwIf(userService.existsByUsernameAll(request.getUsername()),
                ResponseConstant.PARAM_ERROR,
                ResponseConstant.EMAIL_OR_USERNAME_EXISTS_MESSAGE);
        BusinessException.throwIf(userService.existsByEmailAll(request.getEmail()),
                ResponseConstant.PARAM_ERROR,
                ResponseConstant.EMAIL_OR_USERNAME_EXISTS_MESSAGE);
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPasswordHash(CryptoUtil.encode(request.getPassword()));
        //TODO: Temporarily set to true, as we haven't implemented email verification yet
        user.setVerified(true);
        return userService.save(user);
    }

    @Override
    public Boolean logout() {
        // 1. Get current logged-in user ID
        long loginUserId = StpUtil.getLoginIdAsLong();

        // 2. Get current session
        String accessToken = StpUtil.getTokenValue();

        FindSessionByTokenRequest findReq = new FindSessionByTokenRequest();
        findReq.setAccessToken(accessToken);
        findReq.setUserId(loginUserId);
        Session session = sessionService.findByAccessTokenAndUserId(findReq);

        // 3. Mark session as inactive and revoke related tokens
        ConditionalUtil.ifNotNull(session, s -> {
            s.setActive(SessionStatusEnum.INACTIVE.getValue());
            sessionService.updateById(s);

            // 4. Revoke all refresh tokens associated with this session
            RevokeRefreshTokenBySessionRequest revokeReq = new RevokeRefreshTokenBySessionRequest();
            revokeReq.setSessionId(s.getId());
            refreshTokenService.revokeAllBySessionId(revokeReq);
        });
        StpUtil.logout();
        return true;
    }

    @Override
    public Boolean resetPassword(ResetPasswordRequest request) {
        // 1) Query user by email
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));
        BusinessException.throwIf(user == null,
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.USER_NOT_FOUND_MESSAGE);

        // 2) Validate old password
        boolean oldOk = CryptoUtil.match(request.getOldPassword(), user.getPasswordHash());
        BusinessException.throwIf(!oldOk,
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.INVALID_CREDENTIALS_MESSAGE);

        // 3) Update to new password
        user.setPasswordHash(CryptoUtil.encode(request.getNewPassword()));
        userService.updateById(user);

        // 4) Force logout all devices: batch deactivate sessions and revoke refresh tokens by userId
        Long userId = user.getId();
        sessionService.lambdaUpdate()
                .set(Session::getActive, SessionStatusEnum.INACTIVE.getValue())
                .eq(Session::getUserId, userId)
                .eq(Session::getActive, SessionStatusEnum.ACTIVE.getValue())
                .update();
        refreshTokenService.lambdaUpdate()
                .set(RefreshToken::getRevoked, RefreshTokenStatusEnum.REVOKED.getValue())
                .eq(RefreshToken::getUserId, userId)
                .eq(RefreshToken::getRevoked, RefreshTokenStatusEnum.NOT_REVOKED.getValue())
                .update();

        try {
            StpUtil.logout();
        } catch (Exception ignored) {}

        return true;
    }

    @Override
    public TokenPairResponse loginByGoogle(User user) {
        // 1. Login using Sa-Token
        StpUtil.login(user.getId(), new SaLoginModel()
                .setExtra(JwtClaimConstant.EMAIL, user.getEmail())
                .setExtra(JwtClaimConstant.USERNAME, user.getUsername())
                .setExtra(JwtClaimConstant.AVATAR_URL, user.getAvatarUrl()));

        // 2. Get access_token
        String accessToken = StpUtil.getTokenValue();
        String accessTokenHash = CryptoUtil.sha256Hex(accessToken);

        // 3. Extract IP and UA from request headers
        String ip = HttpRequestUtil.extractClientIp();
        String userAgent = HttpRequestUtil.extractUserAgent();

        // 4. Create Session record
        Session session = new Session();
        session.setUserId(user.getId());
        session.setAccessTokenHash(accessTokenHash);
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);
        session.setActive(SessionStatusEnum.ACTIVE.getValue());
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        session.setLastRefreshAt(LocalDateTime.now());
        sessionService.save(session);

        // 5. Generate refresh_token
        CreateRefreshTokenRequest createReq = new CreateRefreshTokenRequest();
        createReq.setUserId(user.getId());
        createReq.setSessionId(session.getId());
        String refreshTokenPlain = refreshTokenService.createAndStoreRefreshToken(createReq);

        // 6. Return TokenPairResponse
        return new TokenPairResponse(accessToken, refreshTokenPlain);
    }

    @Override
    public User registerGoogleUser(RegisterGoogleUserRequest registerGoogleUserRequest) {
        // 1. Check if email already exists
        User existingUser = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, registerGoogleUserRequest.getEmail()));
        
        if (Objects.nonNull(existingUser)) {
            return existingUser;
        }

        // 2. Create new user
        User user = new User();
        user.setEmail(registerGoogleUserRequest.getEmail());
        
        ConditionalUtil.setIfNotBlankOrElse(
            registerGoogleUserRequest.getName(),
            () -> registerGoogleUserRequest.getEmail().split("@")[0],
            user::setUsername
        );
        
        ConditionalUtil.setIfNotBlank(registerGoogleUserRequest.getPicture(), user::setAvatarUrl);
        
        // Set random password (Google users don't need password login)
        user.setPasswordHash(CryptoUtil.sha256Hex(UUID.randomUUID().toString()));
        
        // Google accounts are verified by default
        user.setVerified(true);
        
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        // 3. Save user
        userService.save(user);
        
        return user;
    }


}
