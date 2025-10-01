<!-- 
  基础布局组件
  提供整个应用的页面布局结构，包含头部导航、内容区域和底部
-->
<template>
  <div id="basicLayout">
    <!-- 使用 Ant Design 的布局组件，设置最小高度为100vh -->
    <a-layout style="min-height: 100vh">
      <!-- 头部区域：包含全局导航栏 -->
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>

      <!-- 内容区域：根据路由动态渲染页面内容 -->
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>

      <!-- 底部区域：显示项目链接 -->
      <a-layout-footer class="footer">
        <a href="https://github.com/dawn83679/DeepImage" target="_blank">
          <GithubOutlined /> GitHub
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
// 导入全局头部组件
import GlobalHeader from '@/components/GlobalHeader.vue'
// 导入 GitHub 图标组件
import { GithubOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
/* 头部样式 */
#basicLayout .header {
  background-color: white;
  margin-bottom: 16px;
  color: unset;
  padding-inline: 20px;
}

/* 底部样式 - 固定在页面底部 */
#basicLayout .footer {
  background-color: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}

/* 内容区域样式 - 渐变背景 */
#basicLayout .content {
  padding: 20px;
  background: linear-gradient(to right, #efefef, #fff);
  margin-bottom: 28px; /* 为固定底部留出空间 */
}
</style>
