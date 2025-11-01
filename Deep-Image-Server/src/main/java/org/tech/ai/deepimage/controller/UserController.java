package org.tech.ai.deepimage.controller;

import cn.dev33.satoken.stp.StpUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.tech.ai.deepimage.annotation.LogParams;
import org.tech.ai.deepimage.model.dto.request.UpdateUserProfileRequest;
import org.tech.ai.deepimage.model.dto.response.ApiResponse;
import org.tech.ai.deepimage.model.dto.response.DeleteOtherSessionsResponse;
import org.tech.ai.deepimage.model.dto.response.SessionListResponse;
import org.tech.ai.deepimage.model.dto.response.UserProfileResponse;
import org.tech.ai.deepimage.service.UserService;

/**
 * User controller
 * 
 * @author zgq
 * @since 2025-10-01
 */
@RestController
@Validated
@RequestMapping("/api/user")
@LogParams
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Get current user information
     * @return User information
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserProfileResponse profile = userService.getCurrentUserProfile(userId);
        return ApiResponse.success(profile);
    }

    /**
     * Update user profile
     * @param request Update request 
     * @return Updated user information
     */
    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateUserProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserProfileResponse profile = userService.updateUserProfile(userId, request);
        return ApiResponse.success(profile);
    }

    /**
     * View user session list (only returns active sessions)
     * @return Session list
     */
    @GetMapping("/sessions")
    public ApiResponse<SessionListResponse> listUserSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        SessionListResponse sessions = userService.listUserSessions(userId);
        return ApiResponse.success(sessions);
    }

    /**
     * Delete specified session (kick out device)
     * @param sessionId Session ID
     * @return Whether successful
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Boolean> deleteSession(@PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = userService.deleteSession(userId, sessionId);
        return ApiResponse.success(result);
    }

    /**
     * Delete all other sessions (keep only current device)
     * @return Delete result
     */
    @DeleteMapping("/sessions/others")
    public ApiResponse<DeleteOtherSessionsResponse> deleteOtherSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        DeleteOtherSessionsResponse response = userService.deleteOtherSessions(userId);
        return ApiResponse.success(response);
    }
}