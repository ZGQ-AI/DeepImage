// moved to src/types/api.ts

export interface LoginRequest {
  email: string
  password: string
}

export interface RegisterRequest {
  email: string
  username: string
  password: string
}

export interface RefreshTokenRequest {
  refreshToken: string
}

export interface TokenPairResponse {
  accessToken: string
  refreshToken: string
  expiresIn?: number
}

export interface ResetPasswordRequest {
  email: string
  oldPassword: string
  newPassword: string
}
