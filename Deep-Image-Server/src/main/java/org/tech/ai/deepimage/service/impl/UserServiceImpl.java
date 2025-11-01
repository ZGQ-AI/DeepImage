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
 * User information table service implementation class
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
        // Query user information, throw 404 exception if not exists
        User user = BusinessException.assertNotNull(
            this.getById(userId), 
            ResponseConstant.USER_NOT_FOUND_MESSAGE
        );
        
        // Convert to response object
        UserProfileResponse response = new UserProfileResponse();
        BeanUtils.copyProperties(user, response);
        return response;
    }

    @Override
    public UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        // Query user
        User user = BusinessException.assertNotNull(
            this.getById(userId), 
            ResponseConstant.USER_NOT_FOUND_MESSAGE
        );
        
        // If updating username, check if already taken (exclude self)
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
        
        // Update phone number
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        
        // Update avatar URL
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        
        // Update time
        user.setUpdatedAt(LocalDateTime.now());
        
        // Save update
        this.updateById(user);
        
        // Return updated user information
        return getCurrentUserProfile(userId);
    }

    @Override
    public SessionListResponse listUserSessions(Long userId) {
        // Get current access_token hash, used to mark current session
        String currentTokenHash = null;
        try {
            String currentToken = StpUtil.getTokenValue();
            currentTokenHash = CryptoUtil.sha256Hex(currentToken);
        } catch (Exception e) {
            // If get fails, does not affect query
        }
        
        // Build query conditions - only query active sessions
        LambdaQueryWrapper<Session> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Session::getUserId, userId)
                    .eq(Session::getActive, SessionStatusEnum.ACTIVE.getValue())  // Fixed query active sessions
                    .orderByDesc(Session::getCreatedAt);  // Order by creation time descending
        
        // Query all active sessions (no pagination, usually session count won't be too many)
        List<Session> sessions = sessionService.list(queryWrapper);
        
        // Get total directly from results, avoid additional count query
        long total = sessions.size();
        
        // Convert to response objects
        final String finalCurrentTokenHash = currentTokenHash;
        List<SessionItemResponse> items = sessions.stream().map(session -> {
            SessionItemResponse item = new SessionItemResponse();
            BeanUtils.copyProperties(session, item);
            // Mark whether it is current session
            item.setIsCurrent(finalCurrentTokenHash != null && 
                             finalCurrentTokenHash.equals(session.getAccessTokenHash()));
            return item;
        }).collect(Collectors.toList());
        
        // Build response
        SessionListResponse response = new SessionListResponse();
        response.setSessions(items);
        response.setTotal(total);
        response.setPage(1);
        response.setPageSize((int) total);
        
        return response;
    }

    @Override
    public Boolean deleteSession(Long userId, Long sessionId) {
        // Query session, throw 404 exception if not exists
        Session session = BusinessException.assertNotNull(
            sessionService.getById(sessionId), 
            ResponseConstant.SESSION_NOT_FOUND_MESSAGE
        );
        
        // Validate session belongs to current user
        BusinessException.assertTrue(
            session.getUserId().equals(userId),
            ResponseConstant.FORBIDDEN,
            ResponseConstant.SESSION_NOT_BELONG_TO_USER_MESSAGE
        );
        
        // Prohibit deleting current session
        String currentToken = StpUtil.getTokenValue();
        String currentTokenHash = CryptoUtil.sha256Hex(currentToken);
        BusinessException.assertFalse(
            currentTokenHash.equals(session.getAccessTokenHash()),
            ResponseConstant.PARAM_ERROR, 
            ResponseConstant.CANNOT_DELETE_CURRENT_SESSION_MESSAGE
        );
        
        // Mark session as inactive
        session.setActive(SessionStatusEnum.INACTIVE.getValue());
        session.setUpdatedAt(LocalDateTime.now());
        sessionService.updateById(session);
        
        // Revoke all refresh_tokens associated with this session
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
        // Get current session ID
        String currentToken = StpUtil.getTokenValue();
        
        FindSessionByTokenRequest req = new FindSessionByTokenRequest();
        req.setAccessToken(currentToken);
        req.setUserId(userId);
        Session currentSession = sessionService.findByAccessTokenAndUserId(req);
        
        Long currentSessionId = currentSession != null ? currentSession.getId() : null;
        
        // Batch update other sessions to inactive status
        LambdaQueryWrapper<Session> sessionWrapper = new LambdaQueryWrapper<>();
        sessionWrapper.eq(Session::getUserId, userId)
                     .eq(Session::getActive, SessionStatusEnum.ACTIVE.getValue());
        if (currentSessionId != null) {
            sessionWrapper.ne(Session::getId, currentSessionId);
        }
        
        // Get count of sessions to delete
        long count = sessionService.count(sessionWrapper);
        
        // Execute update
        Session updateSession = new Session();
        updateSession.setActive(SessionStatusEnum.INACTIVE.getValue());
        updateSession.setUpdatedAt(LocalDateTime.now());
        sessionService.update(updateSession, sessionWrapper);
        
        // Batch revoke related refresh_tokens
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
