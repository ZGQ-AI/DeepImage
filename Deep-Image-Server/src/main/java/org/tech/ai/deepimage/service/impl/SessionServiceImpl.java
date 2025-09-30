package org.tech.ai.deepimage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.model.dto.request.FindSessionByTokenRequest;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.mapper.SessionMapper;
import org.tech.ai.deepimage.service.SessionService;
import org.tech.ai.deepimage.util.CryptoUtil;

/**
 * 用户会话表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class SessionServiceImpl extends ServiceImpl<SessionMapper, Session> implements SessionService {

    @Override
    public Session findByAccessTokenAndUserId(FindSessionByTokenRequest request) {
        String accessTokenHash = CryptoUtil.sha256Hex(request.getAccessToken());
        return getOne(new LambdaQueryWrapper<Session>()
                .eq(Session::getAccessTokenHash, accessTokenHash)
                .eq(Session::getUserId, request.getUserId()));
    }
}
