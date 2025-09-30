package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.SaLoginModel;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.config.RefreshTokenProperties;
import org.tech.ai.deepimage.constant.JwtClaimConstant;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.dto.request.CreateRefreshTokenRequest;
import org.tech.ai.deepimage.dto.request.LoginRequest;
import org.tech.ai.deepimage.dto.request.RegisterRequest;
import org.tech.ai.deepimage.dto.request.ResetPasswordRequest;
import org.tech.ai.deepimage.dto.response.TokenPairResponse;
import org.tech.ai.deepimage.entity.RefreshToken;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.service.AuthService;
import org.tech.ai.deepimage.service.RefreshTokenService;
import org.tech.ai.deepimage.service.SessionService;
import org.tech.ai.deepimage.service.UserService;
import org.tech.ai.deepimage.util.CryptoUtil;
import org.tech.ai.deepimage.util.HttpRequestUtil;

import java.time.LocalDateTime;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserService userService;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private SessionService sessionService;

    @Autowired
    private RefreshTokenProperties refreshProps;

    @Override
    public TokenPairResponse loginByEmail(LoginRequest request) {
        // 查询用户
        User user = userService.getOne(new LambdaQueryWrapper<User>()
                .eq(User::getEmail, request.getEmail()));

        BusinessException.throwIf(user == null,
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.INVALID_CREDENTIALS_MESSAGE);
        BusinessException.throwIf(Boolean.FALSE.equals(user.getVerified()),
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.EMAIL_NOT_VERIFIED_MESSAGE);

        // 校验密码
        boolean passwordOk = CryptoUtil.match(request.getPassword(), user.getPasswordHash());
        BusinessException.throwIf(!passwordOk,
                ResponseConstant.UNAUTHORIZED,
                ResponseConstant.INVALID_CREDENTIALS_MESSAGE);

        // 登录并生成 JWT（携带必要用户信息）
        SaLoginModel model = new SaLoginModel();
        model.setExtra(JwtClaimConstant.USERNAME, user.getUsername());
        model.setExtra(JwtClaimConstant.EMAIL, user.getEmail());
        StpUtil.login(user.getId(), model);
        String accessToken = StpUtil.getTokenValue();

        // 采集请求头中的 IP 与 UA
        String ip = HttpRequestUtil.extractClientIp();
        String userAgent = HttpRequestUtil.extractUserAgent();

        // 持久化会话
        Session session = new Session();
        session.setUserId(user.getId());
        session.setAccessTokenHash(CryptoUtil.sha256Hex(accessToken));
        session.setDeviceInfo(null);
        session.setIpAddress(ip);
        session.setUserAgent(userAgent);
        session.setLastRefreshAt(LocalDateTime.now());
        sessionService.save(session);


        // 生成并持久化 refresh token
        CreateRefreshTokenRequest createReq = new CreateRefreshTokenRequest();
        createReq.setUserId(user.getId());
        createReq.setSessionId(session.getId());
        String refreshTokenPlain = refreshTokenService.createAndStoreRefreshToken(createReq);

        return new TokenPairResponse(accessToken, refreshTokenPlain);
    }

    @Override
    public TokenPairResponse refreshToken(String refreshTokenPlain) {
        // 1. 验证 refresh token 是否有效
        RefreshToken refreshToken = refreshTokenService.verifyAndGet(refreshTokenPlain);
        BusinessException.throwIf(refreshToken == null, ResponseConstant.UNAUTHORIZED, ResponseConstant.INVALID_REFRESH_TOKEN_MESSAGE);

        // 2. 撤销旧的 refresh token
        refreshTokenService.revoke(refreshTokenPlain);

        // 3. 获取用户信息
        User user = userService.getById(refreshToken.getUserId());
        BusinessException.throwIf(user == null, ResponseConstant.UNAUTHORIZED, ResponseConstant.USER_NOT_FOUND_MESSAGE);

        // 4. 生成新的 access token
        StpUtil.login(user.getId(), new SaLoginModel()
                .setExtra(JwtClaimConstant.USERNAME, user.getUsername())
                .setExtra(JwtClaimConstant.EMAIL, user.getEmail()));
        String accessToken = StpUtil.getTokenValue();

        // 5. 更新会话信息
        Session session = sessionService.getById(refreshToken.getSessionId());
        if (session != null) {
            session.setAccessTokenHash(CryptoUtil.sha256Hex(accessToken));
            session.setLastRefreshAt(LocalDateTime.now());
            sessionService.updateById(session);
        }

        // 6. 生成新的 refresh token
        CreateRefreshTokenRequest createReq = new CreateRefreshTokenRequest();
        createReq.setUserId(user.getId());
        createReq.setSessionId(refreshToken.getSessionId());
        String newRefreshTokenPlain = refreshTokenService.createAndStoreRefreshToken(createReq);

        // 7. 返回新的 token pair
        return new TokenPairResponse(accessToken, newRefreshTokenPlain);
    }

    @Override
    public Boolean register(RegisterRequest request) {
        // 校验 username/email 全局唯一（不区分逻辑删除）
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
        //TODO: 暂时设置为true，因为我们还没做邮箱登录的功能
        user.setVerified(true);
        return userService.save(user);
    }

    @Override
    public Boolean logout() {
        return false;
    }

    @Override
    public Boolean resetPassword(ResetPasswordRequest request) {
        return false;
    }


}
