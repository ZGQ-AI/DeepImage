import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/useAuthStore'
import { useUserStore } from '../stores/useUserStore'
import { getAccessToken, getRefreshToken } from '../utils/token'
import { isTokenExpired } from '../utils/jwt'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/auth/callback',
      name: 'auth-callback',
      component: () => import('../pages/AuthCallback.vue'),
      meta: {
        public: true,
        title: 'Google登录回调',
      },
    },
    {
      path: '/',
      name: 'home',
      component: HomeView,
      // public home page, no auth required
    },
    {
      path: '/about',
      name: 'about',
      // route level code-splitting
      // this generates a separate chunk (About.[hash].js) for this route
      // which is lazy-loaded when the route is visited.
      component: () => import('../views/AboutView.vue'),
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../pages/UserProfile.vue'),
      meta: {
        requiresAuth: true,
        title: '个人中心',
      },
    },
    {
      path: '/tags',
      name: 'tags',
      component: () => import('../pages/TagManagement.vue'),
      meta: {
        requiresAuth: true,
        title: '标签管理',
      },
    },
    {
      path: '/gallery',
      name: 'gallery',
      component: () => import('../pages/ImageGallery.vue'),
      meta: {
        requiresAuth: true,
        title: '我的图库',
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const user = useUserStore()
  const isPublic = to.meta?.public === true

  // Public routes don't require authentication
  if (isPublic) return true

  if (to.meta?.requiresAuth) {
    // 主动检查 access token 是否有效
    const accessToken = getAccessToken()
    const refreshToken = getRefreshToken()

    // 情况 1: 有 accessToken 且未过期
    if (accessToken && !isTokenExpired(accessToken, 60)) {
      console.log('[Router Guard] Access token is valid')
      
      // 如果用户信息未加载，尝试加载
      if (!user.profile) {
        try {
          await user.fetchProfile()
        } catch (err) {
          console.warn('[Router Guard] Failed to fetch user profile:', err)
        }
      }
      return true
    }

    // 情况 2: accessToken 过期或不存在，但有 refreshToken
    if (refreshToken) {
      console.log('[Router Guard] Access token expired, attempting refresh with refresh token...')
      
      try {
        await auth.refresh()
        console.log('[Router Guard] Token refreshed successfully')
        
        // 刷新成功后，加载用户信息
        try {
          await user.fetchProfile()
        } catch (err) {
          console.warn('[Router Guard] Failed to fetch user profile after refresh:', err)
        }
        return true
      } catch (err) {
        console.error('[Router Guard] Token refresh failed:', err)
        // 刷新失败，继续走下面的登录流程
      }
    }

    // 情况 3: 没有有效的 token，弹出登录框
    console.log('[Router Guard] No valid token, showing login modal')
    auth.showLoginModal(to.fullPath)
    
    // 取消当前导航，保持在当前页面
    return false
  }

  return true
})

export default router
