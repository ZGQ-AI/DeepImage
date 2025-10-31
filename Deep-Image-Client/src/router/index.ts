import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/useAuthStore'
import { useUserStore } from '../stores/useUserStore'
import { checkAuth } from '../utils/authUtils'
import { tokenRefreshManager } from '../utils/tokenRefreshManager'
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
    {
      path: '/search',
      name: 'image-search',
      component: () => import('../pages/ImageSearch.vue'),
      meta: {
        requiresAuth: true,
        title: '图片搜索',
      },
    },
    {
      path: '/recycle-bin',
      name: 'recycle-bin',
      component: () => import('../pages/RecycleBin.vue'),
      meta: {
        requiresAuth: true,
        title: '回收站',
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const user = useUserStore()
  const isPublic = to.meta?.public === true

  // Keep consistent page title for all pages
  document.title = 'Deep Image'

  // Public routes don't require authentication
  if (isPublic) return true

  if (to.meta?.requiresAuth) {
    // Use centralized auth utilities to check authentication state
    const authState = checkAuth()

    // Case 1: User is authenticated and token is valid
    if (authState.isAuthenticated && !authState.needsRefresh) {
      console.log('[Router Guard] Access token is valid')

      // Load user profile if not already loaded
      if (!user.profile) {
        try {
          await user.fetchProfile()
        } catch (err) {
          console.warn('[Router Guard] Failed to fetch user profile:', err)
        }
      }
      return true
    }

    // Case 2: Access token expired but refresh token exists
    if (authState.needsRefresh) {
      console.log('[Router Guard] Access token expired, attempting refresh...')

      try {
        // Use TokenRefreshManager for centralized refresh handling
        await tokenRefreshManager.refresh()
        console.log('[Router Guard] Token refreshed successfully')

        // Load user profile after successful refresh
        try {
          await user.fetchProfile()
        } catch (err) {
          console.warn('[Router Guard] Failed to fetch user profile after refresh:', err)
        }
        return true
      } catch (err) {
        console.error('[Router Guard] Token refresh failed:', err)
        // Refresh failed, continue to login flow
      }
    }

    // Case 3: No valid tokens, show login modal
    console.log('[Router Guard] No valid token, showing login modal')
    auth.showLoginModal(to.fullPath)

    // Cancel navigation, stay on current page
    return false
  }

  return true
})

export default router
