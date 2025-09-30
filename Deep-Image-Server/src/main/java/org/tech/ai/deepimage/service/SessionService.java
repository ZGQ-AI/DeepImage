package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.model.dto.request.FindSessionByTokenRequest;
import org.tech.ai.deepimage.entity.Session;

/**
 * 用户会话表 服务类
 * 
 * @author zgq
 * @since 2025-09-29
 */
public interface SessionService extends IService<Session> {

    /**
     * 根据accessToken和用户ID查找会话
     */
    Session findByAccessTokenAndUserId(FindSessionByTokenRequest request);
}
