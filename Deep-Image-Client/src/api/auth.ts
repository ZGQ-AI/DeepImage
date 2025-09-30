import request from '../request';
import type { ApiResponse } from '../types/api';
import type { LoginRequest, RefreshTokenRequest, RegisterRequest, ResetPasswordRequest, TokenPairResponse } from '../types/auth';

export function login(data: LoginRequest) {
    return request.post<ApiResponse<TokenPairResponse>>('/api/auth/login', data);
}

export function register(data: RegisterRequest) {
    return request.post<ApiResponse<boolean>>('/api/auth/register', data);
}

export function refreshToken(data: RefreshTokenRequest) {
    return request.post<ApiResponse<TokenPairResponse>>('/api/auth/refresh', data);
}

export function logout() {
    return request.post<ApiResponse<boolean>>('/api/auth/logout');
}

export function resetPassword(data: ResetPasswordRequest) {
    return request.post<ApiResponse<boolean>>('/api/auth/reset-password', data);
}


