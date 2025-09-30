package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.model.dto.request.CreateRefreshTokenRequest;
import org.tech.ai.deepimage.model.dto.request.RevokeRefreshTokenBySessionRequest;
import org.tech.ai.deepimage.entity.RefreshToken;

/**
 * 刷新令牌表 服务类
 * 
 * @author zgq
 * @since 2025-09-29
 */
public interface RefreshTokenService extends IService<RefreshToken> {
    /**
     * 生成并持久化新的 RefreshToken，返回明文 refreshToken
     */
    String createAndStoreRefreshToken(CreateRefreshTokenRequest request);

    /**
     * 校验 refreshToken 是否有效（未过期、未撤销、未删除），返回对应实体；无效则返回 null
     */
    RefreshToken verifyAndGet(String refreshTokenPlain);


    /**
     * 撤销指定的 RefreshToken
     */
    void revoke(String refreshTokenPlain);

    /**
     * 撤销指定会话的所有 RefreshToken
     */
    void revokeAllBySessionId(RevokeRefreshTokenBySessionRequest request);
}
