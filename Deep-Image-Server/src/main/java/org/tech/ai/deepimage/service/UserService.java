package org.tech.ai.deepimage.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.tech.ai.deepimage.entity.User;
import org.tech.ai.deepimage.model.dto.request.UpdateUserProfileRequest;
import org.tech.ai.deepimage.model.dto.response.DeleteOtherSessionsResponse;
import org.tech.ai.deepimage.model.dto.response.SessionListResponse;
import org.tech.ai.deepimage.model.dto.response.UserProfileResponse;

/**
 * 用户信息表 服务类
 * 
 * @author zgq
 * @since 2025-09-29
 */
public interface UserService extends IService<User> {
    boolean existsByUsernameAll(String username);
    boolean existsByEmailAll(String email);
    
    /**
     * 获取当前用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    UserProfileResponse getCurrentUserProfile(Long userId);
    
    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param request 更新请求
     * @return 更新后的用户信息
     */
    UserProfileResponse updateUserProfile(Long userId, UpdateUserProfileRequest request);
    
    /**
     * 查询用户会话列表（只返回活跃会话）
     * @param userId 用户ID
     * @return 会话列表
     */
    SessionListResponse listUserSessions(Long userId);
    
    /**
     * 删除指定会话
     * @param userId 用户ID
     * @param sessionId 会话ID
     * @return 是否成功
     */
    Boolean deleteSession(Long userId, Long sessionId);
    
    /**
     * 删除其他所有会话
     * @param userId 用户ID
     * @return 删除结果
     */
    DeleteOtherSessionsResponse deleteOtherSessions(Long userId);
}
