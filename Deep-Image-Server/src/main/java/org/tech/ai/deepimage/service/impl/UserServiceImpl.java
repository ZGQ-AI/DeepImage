package org.tech.ai.deepimage.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tech.ai.deepimage.constant.ResponseConstant;
import org.tech.ai.deepimage.enums.RefreshTokenStatusEnum;
import org.tech.ai.deepimage.enums.SessionStatusEnum;
import org.tech.ai.deepimage.entity.RefreshToken;
import org.tech.ai.deepimage.entity.Session;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.exception.BusinessException;
import org.tech.ai.deepimage.mapper.UserMapper;
import org.tech.ai.deepimage.model.dto.request.FindSessionByTokenRequest;
import org.tech.ai.deepimage.model.dto.request.UpdateUserProfileRequest;
import org.tech.ai.deepimage.model.dto.response.DeleteOtherSessionsResponse;
import org.tech.ai.deepimage.model.dto.response.SessionItemResponse;
import org.tech.ai.deepimage.model.dto.response.SessionListResponse;
import org.tech.ai.deepimage.model.dto.response.UserProfileResponse;
import org.tech.ai.deepimage.service.RefreshTokenService;
import org.tech.ai.deepimage.service.SessionService;
import org.tech.ai.deepimage.service.UserService;
import org.tech.ai.deepimage.util.CryptoUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户信息表 服务实现类
 * 
 * @author zgq
 * @since 2025-09-29
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private RefreshTokenService refreshTokenService;
    
    @Override
    public boolean existsByUsernameAll(String username) {
        return baseMapper.countByUsernameAll(username) > 0;
    }

    @Override
    public boolean existsByEmailAll(String email) {
        return baseMapper.countByEmailAll(email) > 0;
    }

    @Override
    public UserProfileResponse getCurrentUserProfile(Long userId) {
        // 查询用户信息，如果不存在则抛出404异常
        User user = BusinessException.assertNotNull(
            this.getById(userId), 
            ResponseConstant.USER_NOT_FOUND_MESSAGE
        );
        
        // 转换为响应对象
        UserProfileResponse response = new UserProfileResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        // 查询用户
        User user = BusinessException.assertNotNull(
            this.getById(userId), 
            ResponseConstant.USER_NOT_FOUND_MESSAGE
        );
        
        // 如果要更新用户名，检查是否已被占用（排除自己）
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(User::getUsername, request.getUsername())
                   .ne(User::getId, userId);
            long count = this.count(wrapper);
            BusinessException.assertFalse(count > 0, 
                    ResponseConstant.PARAM_ERROR, 
                    ResponseConstant.USERNAME_ALREADY_EXISTS_MESSAGE);
            user.setUsername(request.getUsername());
        }
        
        // 更新手机号
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        // 更新头像URL
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        // 更新时间
        user.setUpdatedAt(LocalDateTime.now());
        
        // 保存更新
        this.updateById(user);
        
        // 返回更新后的用户信息
        return getCurrentUserProfile(userId);
    }

    @Override
    public SessionListResponse listUserSessions(Long userId) {
        // 获取当前access_token的hash，用于标记当前会话
        String currentTokenHash = null;
        try {
            String currentToken = StpUtil.getTokenValue();
            currentTokenHash = CryptoUtil.sha256Hex(currentToken);
        } catch (Exception e) {
            // 如果获取失败，不影响查询
        }
        
        // 构建查询条件 - 只查询活跃的会话
        LambdaQueryWrapper<Session> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Session::getUserId, userId)
                    .eq(Session::getActive, SessionStatusEnum.ACTIVE.getValue())  // 固定查询活跃会话
                    .orderByDesc(Session::getCreatedAt);  // 按创建时间降序排列
        
        // 查询所有活跃会话（不分页，通常会话数量不会太多）
        List<Session> sessions = sessionService.list(queryWrapper);
        
        // 直接从结果中获取总数，避免额外的count查询
        long total = sessions.size();
        
        // 转换为响应对象
        final String finalCurrentTokenHash = currentTokenHash;
        List<SessionItemResponse> items = sessions.stream().map(session -> {
            SessionItemResponse item = new SessionItemResponse();
            BeanUtils.copyProperties(session, item);
            // 标记是否为当前会话
            item.setIsCurrent(finalCurrentTokenHash != null && 
                             finalCurrentTokenHash.equals(session.getAccessTokenHash()));
            return item;
        }).collect(Collectors.toList());
        
        // 构建响应
        SessionListResponse response = new SessionListResponse();
        response.setSessions(items);
        response.setTotal(total);
        response.setPage(1);
        response.setPageSize((int) total);
        
        return response;
    }

    @Override
    public Boolean deleteSession(Long userId, Long sessionId) {
        // 查询会话，如果不存在则抛出404异常
        Session session = BusinessException.assertNotNull(
            sessionService.getById(sessionId), 
            ResponseConstant.SESSION_NOT_FOUND_MESSAGE
        );
        
        // 验证会话是否属于当前用户
        BusinessException.assertTrue(
            session.getUserId().equals(userId),
            ResponseConstant.FORBIDDEN,
            ResponseConstant.SESSION_NOT_BELONG_TO_USER_MESSAGE
        );
        
        // 禁止删除当前会话
        String currentToken = StpUtil.getTokenValue();
        String currentTokenHash = CryptoUtil.sha256Hex(currentToken);
        BusinessException.assertFalse(
            currentTokenHash.equals(session.getAccessTokenHash()),
            ResponseConstant.PARAM_ERROR, 
            ResponseConstant.CANNOT_DELETE_CURRENT_SESSION_MESSAGE
        );
        
        // 标记会话为非活跃
        session.setActive(SessionStatusEnum.INACTIVE.getValue());
        session.setUpdatedAt(LocalDateTime.now());
        sessionService.updateById(session);
        
        // 撤销该会话关联的所有refresh_token
        LambdaQueryWrapper<RefreshToken> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(RefreshToken::getSessionId, sessionId)
               .eq(RefreshToken::getRevoked, RefreshTokenStatusEnum.NOT_REVOKED.getValue());
        
        RefreshToken updateEntity = new RefreshToken();
        updateEntity.setRevoked(RefreshTokenStatusEnum.REVOKED.getValue());
        updateEntity.setUpdatedAt(LocalDateTime.now());
        refreshTokenService.update(updateEntity, wrapper);
        
        return true;
    }

    @Override
    public DeleteOtherSessionsResponse deleteOtherSessions(Long userId) {
        // 获取当前会话ID
        String currentToken = StpUtil.getTokenValue();
        
        FindSessionByTokenRequest req = new FindSessionByTokenRequest();
        req.setAccessToken(currentToken);
        req.setUserId(userId);
        Session currentSession = sessionService.findByAccessTokenAndUserId(req);
        
        Long currentSessionId = currentSession != null ? currentSession.getId() : null;
        
        // 批量更新其他会话为非活跃状态
        LambdaQueryWrapper<Session> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(Session::getUserId, userId)
                     .eq(Session::getActive, SessionStatusEnum.ACTIVE.getValue());
        if (currentSessionId != null) {
            sessionWrapper.ne(Session::getId, currentSessionId);
        }
        
        // 获取要删除的会话数量
        long count = sessionService.count(sessionWrapper);
        
        // 执行更新
        Session updateSession = new Session();
        updateSession.setActive(SessionStatusEnum.INACTIVE.getValue());
        updateSession.setUpdatedAt(LocalDateTime.now());
        sessionService.update(updateSession, sessionWrapper);
        
        // 批量撤销相关的refresh_token
        LambdaQueryWrapper<RefreshToken> tokenWrapper = new LambdaQueryWrapper<>();
        tokenWrapper.eq(RefreshToken::getUserId, userId)
                   .eq(RefreshToken::getRevoked, RefreshTokenStatusEnum.NOT_REVOKED.getValue());
        if (currentSessionId != null) {
            tokenWrapper.ne(RefreshToken::getSessionId, currentSessionId);
        }
        
        RefreshToken updateToken = new RefreshToken();
        updateToken.setRevoked(RefreshTokenStatusEnum.REVOKED.getValue());
        updateToken.setUpdatedAt(LocalDateTime.now());
        refreshTokenService.update(updateToken, tokenWrapper);
        
        return new DeleteOtherSessionsResponse((int) count);
    }
}
