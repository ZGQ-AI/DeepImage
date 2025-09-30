package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.config.RefreshTokenProperties;
import org.tech.ai.deepimage.constant.TokenConstants;
import org.tech.ai.deepimage.dto.request.CreateRefreshTokenRequest;
import org.tech.ai.deepimage.entity.RefreshToken;
import org.tech.ai.deepimage.mapper.RefreshTokenMapper;
import org.tech.ai.deepimage.service.RefreshTokenService;
import org.tech.ai.deepimage.util.CryptoUtil;

import java.time.LocalDateTime;

/**
 * 刷新令牌表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class RefreshTokenServiceImpl extends ServiceImpl<RefreshTokenMapper, RefreshToken> implements RefreshTokenService {
    @Autowired
    private RefreshTokenProperties refreshProps;

    private String hash(String plain) { return CryptoUtil.sha256Hex(plain); }
    private String randomToken() { return CryptoUtil.randomToken(); }

    @Override
    public String createAndStoreRefreshToken(CreateRefreshTokenRequest request) {
        String plain = randomToken();
        String hash = hash(plain);

        RefreshToken entity = new RefreshToken();
        entity.setUserId(request.getUserId());
        entity.setSessionId(request.getSessionId());
        entity.setTokenHash(hash);
        entity.setExpiresAt(LocalDateTime.now().plusSeconds(refreshProps.getRefreshTtlSeconds()));
        save(entity);
        return plain;
    }

    @Override
    public RefreshToken verifyAndGet(String refreshTokenPlain) {
        String hash = hash(refreshTokenPlain);
        RefreshToken token = getOne(new LambdaQueryWrapper<RefreshToken>()
                .eq(RefreshToken::getTokenHash, hash)
                .eq(RefreshToken::getRevoked, TokenConstants.NOT_REVOKED));
        if (token == null) return null;
        if (token.getExpiresAt() != null && token.getExpiresAt().isBefore(LocalDateTime.now())) {
            return null;
        }
        return token;
    }


    @Override
    public void revokeAllByUserId(Long userId) {
        lambdaUpdate()
                .eq(RefreshToken::getUserId, userId)
                .set(RefreshToken::getRevoked, 1)
                .update();
    }

    @Override
    public void revoke(String refreshTokenPlain) {
        String hash = hash(refreshTokenPlain);
        lambdaUpdate()
                .eq(RefreshToken::getTokenHash, hash)
                .set(RefreshToken::getRevoked, TokenConstants.REVOKED)
                .update();
    }
}
