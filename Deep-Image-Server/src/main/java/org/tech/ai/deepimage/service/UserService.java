package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.model.dto.request.UpdateUserProfileRequest;
import org.tech.ai.deepimage.model.dto.response.DeleteOtherSessionsResponse;
import org.tech.ai.deepimage.model.dto.response.SessionListResponse;
import org.tech.ai.deepimage.model.dto.response.UserProfileResponse;

/**
 * User information table service class
 * 
 * @author zgq
 * @since 2025-09-29
 */
public interface UserService extends IService<User> {
    boolean existsByUsernameAll(String username);
    boolean existsByEmailAll(String email);
    
    /**
     * Get current user information
     * @param userId User ID
     * @return User information
     */
    UserProfileResponse getCurrentUserProfile(Long userId);
    
    /**
     * Update user information
     * @param userId User ID
     * @param request Update request
     * @return Updated user information
     */
    UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);
    
    /**
     * Query user session list (only returns active sessions)
     * @param userId User ID
     * @return Session list
     */
    SessionListResponse listUserSessions(Long userId);
    
    /**
     * Delete specified session
     * @param userId User ID
     * @param sessionId Session ID
     * @return Whether successful
     */
    Boolean deleteSession(Long userId, Long sessionId);
    
    /**
     * Delete all other sessions
     * @param userId User ID
     * @return Delete result
     */
    DeleteOtherSessionsResponse deleteOtherSessions(Long userId);
}
