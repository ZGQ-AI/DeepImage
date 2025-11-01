package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.model.dto.request.CreateRefreshTokenRequest;
import org.tech.ai.deepimage.model.dto.request.RevokeRefreshTokenBySessionRequest;
import org.tech.ai.deepimage.entity.RefreshToken;

/**
 * Refresh token table service class
 * 
 * @author zgq
 * @since 2025-09-29
 */
public interface RefreshTokenService extends IService<RefreshToken> {
    /**
     * Generate and persist new RefreshToken, return plaintext refreshToken
     */
    String createAndStoreRefreshToken(CreateRefreshTokenRequest request);

    /**
     * Verify if refreshToken is valid (not expired, not revoked, not deleted), return corresponding entity; return null if invalid
     */
    RefreshToken verifyAndGet(String refreshTokenPlain);


    /**
     * Revoke specified RefreshToken
     */
    void revoke(String refreshTokenPlain);

    /**
     * Revoke all RefreshTokens of specified session
     */
    void revokeAllBySessionId(RevokeRefreshTokenBySessionRequest request);
}
