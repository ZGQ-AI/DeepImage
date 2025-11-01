package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.model.dto.request.FindSessionByTokenRequest;
import org.tech.ai.deepimage.entity.Session;

/**
 * User session table service class
 * 
 * @author zgq
 * @since 2025-09-29
 */
public interface SessionService extends IService<Session> {

    /**
     * Find session by accessToken and user ID
     */
    Session findByAccessTokenAndUserId(FindSessionByTokenRequest request);
}
