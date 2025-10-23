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
 * 用户控制器
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
     * 获取当前用户信息
     * @return 用户信息
     */
    @GetMapping("/profile")
    public ApiResponse<UserProfileResponse> getCurrentUserProfile() {
        Long userId = StpUtil.getLoginIdAsLong();
        UserProfileResponse profile = userService.getCurrentUserProfile(userId);
        return ApiResponse.success(profile);
    }

    /**
     * 更新个人信息
     * @param request 更新请求 
     * @return 更新后的用户信息
     */
    @PutMapping("/profile")
    public ApiResponse<UserProfileResponse> updateUserProfile(@Valid @RequestBody UpdateUserProfileRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        UserProfileResponse profile = userService.updateUserProfile(userId, request);
        return ApiResponse.success(profile);
    }

    /**
     * 查看个人会话列表（只返回活跃会话）
     * @return 会话列表
     */
    @GetMapping("/sessions")
    public ApiResponse<SessionListResponse> listUserSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        SessionListResponse sessions = userService.listUserSessions(userId);
        return ApiResponse.success(sessions);
    }

    /**
     * 删除指定会话（踢出设备）
     * @param sessionId 会话ID
     * @return 是否成功
     */
    @DeleteMapping("/sessions/{sessionId}")
    public ApiResponse<Boolean> deleteSession(@PathVariable Long sessionId) {
        Long userId = StpUtil.getLoginIdAsLong();
        Boolean result = userService.deleteSession(userId, sessionId);
        return ApiResponse.success(result);
    }

    /**
     * 删除其他所有会话（仅保留当前设备）
     * @return 删除结果
     */
    @DeleteMapping("/sessions/others")
    public ApiResponse<DeleteOtherSessionsResponse> deleteOtherSessions() {
        Long userId = StpUtil.getLoginIdAsLong();
        DeleteOtherSessionsResponse response = userService.deleteOtherSessions(userId);
        return ApiResponse.success(response);
    }
}