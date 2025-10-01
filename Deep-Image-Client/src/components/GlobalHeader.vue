<!-- 
  全局头部导航组件
  包含网站Logo、导航菜单和用户登录按钮
-->
<template>
  <div id="globalHeader">
    <!-- 使用 Ant Design 的行布局，不换行 -->
    <a-row :wrap="false">
      <!-- Logo区域：固定宽度110px -->
      <a-col flex="110px">
        <router-link to="/">
          <div class="title-bar">
            <img class="logo" src="../assets/logo.svg" alt="logo" />
            <div class="title">DeepImage</div>
          </div>
        </router-link>
      </a-col>

      <!-- 导航菜单区域：自适应宽度 -->
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>

      <!-- 用户操作区域：固定宽度100px -->
      <a-col flex="160px">
        <div class="user-login-status">
          <template v-if="loginUserStore.loginUser.id">
            <a-dropdown trigger="['click']">
              <a-space class="user-entry">
                <a-avatar size="small">{{ initials }}</a-avatar>
                <span class="username">{{ loginUserStore.loginUser.userName }}</span>
              </a-space>
              <template #overlay>
                <a-menu @click="onUserMenuClick">
                  <a-menu-item key="profile">个人中心</a-menu-item>
                  <a-menu-divider />
                  <a-menu-item key="logout">退出登录</a-menu-item>
                </a-menu>
              </template>
            </a-dropdown>
          </template>
          <template v-else>
            <a-button type="primary" @click="goLogin">登录</a-button>
          </template>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref, computed } from 'vue'
import { HomeOutlined, GithubOutlined } from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { useLoginUserStore } from '../stores/UseLoginUserStore'
import { useAuthStore } from '../stores/useAuthStore'

const loginUserStore = useLoginUserStore()
const authStore = useAuthStore()
const current = ref<string[]>([])
const items = ref<MenuProps['items']>([
  {
    key: '/',
    icon: () => h(HomeOutlined),
    label: 'Home',
    title: 'Home',
  },
  {
    key: '/about',
    label: 'about',
    title: 'about',
  },
  {
    key: '/other',
    icon: () => h(GithubOutlined),
    label: h('a', { href: 'https://github.com/dawn83679/DeepImage', target: '_blank' }, 'GitHub'),
    title: 'GitHub',
  },
])

const router = useRouter()
const doMenuClick = ({ key }: { key: string }) => {
  router.push(key)
}
router.afterEach((to) => {
    current.value = [to.path]
})

const goLogin = () => router.push({ name: 'auth' })
const initials = computed(() => {
  const name = loginUserStore.loginUser.userName || ''
  return name ? name.slice(0, 1).toUpperCase() : '?'
})

async function onUserMenuClick({ key }: { key: string }) {
  if (key === 'logout') {
    await authStore.logout()
    router.replace({ name: 'auth' })
  } else if (key === 'profile') {
    router.push('/about')
  }
}
</script>

<style scoped>
#globalHeader .title-bar {
  display: flex;
  align-items: center;
}

#globalHeader .title {
  color: black;
  font-size: 14px;
  margin-left: 5px;
}

#globalHeader .logo {
  width: 28px;
  height: 28px;
  padding-top: 5px;
}
.user-login-status {
  display: flex;
  justify-content: flex-end;
}
.user-entry {
  cursor: pointer;
}
.username {
  font-size: 13px;
}
</style>
