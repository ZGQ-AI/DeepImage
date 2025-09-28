<!-- 
  主应用组件
  作为整个应用的根组件，负责渲染基础布局
-->
<template>
  <div id="app">
    <!-- 使用基础布局组件，包含头部、内容区域和底部 -->
    <BasicLayout />
  </div>
</template>

<script setup lang="ts">
// 导入基础布局组件
import BasicLayout from './layout/BasicLayout.vue'
import { useLoginUserStore } from './stores/UseLoginUserStore';

const loginUserStore = useLoginUserStore();

loginUserStore.fetchLoginUser();

</script>

<style scoped>
/* 应用根组件样式 */
</style>
