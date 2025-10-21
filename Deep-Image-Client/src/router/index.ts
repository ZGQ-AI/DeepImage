import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/useAuthStore'
import HomeView from '../views/HomeView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/auth',
      name: 'auth',
      component: () => import('../pages/Auth.vue'),
      meta: { public: true },
    },
    {
      path: '/auth/callback',
      name: 'auth-callback',
      component: () => import('../pages/AuthCallback.vue'),
      meta: { 
        public: true,
        title: 'Google登录回调'
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
        title: '个人中心'
      },
    },
    {
      path: '/tags',
      name: 'tags',
      component: () => import('../pages/TagManagement.vue'),
      meta: { 
        requiresAuth: true,
        title: '标签管理'
      },
    },
  ],
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  const isPublic = to.meta?.public === true
  
  if (isPublic) return true
  
  if (to.meta?.requiresAuth) {
    // 如果已认证，直接放行（用户信息会通过 watch 自动加载）
    if (auth.isAuthenticated) {
      return true
    }
    
    // 尝试通过 refresh token 恢复登录状态
    const ok = await auth.bootstrap()
    if (ok) {
      return true
    }
    
    // 认证失败，跳转到登录页
    return { name: 'auth', query: { redirect: to.fullPath } }
  }
  
  return true
})

export default router
