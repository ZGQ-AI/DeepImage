import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { login as loginApi, refreshToken as refreshApi, logout as logoutApi } from '../api/auth'
import type { LoginRequest, TokenPairResponse } from '../types/auth'
import { getAccessToken, setAccessToken, getRefreshToken, setRefreshToken, clearTokens, setTokens } from '../utils/token'
import { decodeJwt } from '../utils/jwt'
import { useLoginUserStore } from './UseLoginUserStore'

export const useAuthStore = defineStore('auth', () => {
    const accessToken = ref<string | null>(getAccessToken())
    const refreshToken = ref<string | null>(getRefreshToken())
    const expiresIn = ref<number | null>(null)

    const isAuthenticated = computed(() => !!accessToken.value)

    function applyTokenPair(tokenPair: TokenPairResponse, mode: 'local' | 'session' = 'session') {
        accessToken.value = tokenPair.accessToken
        refreshToken.value = tokenPair.refreshToken
        expiresIn.value = tokenPair.expiresIn ?? null
        setTokens(tokenPair.accessToken, tokenPair.refreshToken, mode)
        // 解析 JWT，更新用户可见信息（id、userName），便于头部展示
        const payload = decodeJwt(tokenPair.accessToken)
        if (payload) {
            const loginUserStore = useLoginUserStore()
            const id = (payload.loginId ?? payload.sub) as any
            const userName = (payload.username ?? payload.USERNAME ?? payload.name) as any
            if (id || userName) {
                loginUserStore.setLoginUser({ id: id ?? null, userName: userName ?? 'user' })
            }
        }
    }

    async function login(payload: LoginRequest & { remember?: boolean }) {
        const { data } = await loginApi(payload)
        if (data.code !== 200) throw new Error(data.message)
        applyTokenPair(data.data, payload.remember ? 'local' : 'session')
        return true
    }

    async function refresh() {
        const rt = refreshToken.value || getRefreshToken()
        if (!rt) throw new Error('No refresh token')
        const { data } = await refreshApi({ refreshToken: rt })
        if (data.code !== 200) throw new Error(data.message)
        applyTokenPair(data.data)
        return true
    }
    async function logout() {
        try { await logoutApi() } catch (e) { /* ignore */ }
        accessToken.value = null
        refreshToken.value = null
        expiresIn.value = null
        clearTokens()
    }

    async function bootstrap() {
        if (accessToken.value) return true
        if (getRefreshToken()) {
            try { await refresh(); return true } catch { /* fallthrough */ }
        }
        return false
    }

    return { accessToken, refreshToken, expiresIn, isAuthenticated, login, refresh, logout, bootstrap }
})


