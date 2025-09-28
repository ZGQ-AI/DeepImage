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
                        <img class="logo" src="../assets/logo.svg" alt="logo"></img>
                        <div class="title">DeepImage</div>
                    </div>
                </router-link>
            </a-col>

            <!-- 导航菜单区域：自适应宽度 -->
            <a-col flex="auto">
                <a-menu v-model:selectedKeys="current" mode="horizontal" :items="items" @click="doMenuClick" />
            </a-col>

            <!-- 用户操作区域：固定宽度100px -->
            <a-col flex="100px">
                <div class="user-login-status">
                    <div v-if="loginUserStore.loginUser.id">
                        {{ loginUserStore.loginUser.userName }}
                    </div>
                    <div v-else>
                        <a-button type="primary" href="/user/login">登录</a-button>
                    </div>
                </div>
            </a-col>
        </a-row>
    </div>
</template>
<script lang="ts" setup>
import { h, ref } from 'vue';
import { HomeOutlined, GithubOutlined } from '@ant-design/icons-vue';
import type { MenuProps } from 'ant-design-vue';
import { useRouter } from 'vue-router';
import { useLoginUserStore } from '../stores/UseLoginUserStore';

const loginUserStore = useLoginUserStore();
const current = ref<string[]>([]);
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
    }
]);

const router = useRouter();
const doMenuClick = ({ key }: { key: string }) => {
    router.push(key);
};
router.afterEach((to, form, next) => {
    current.value = [to.path]
})
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
</style>