<!-- 
  Basic Layout Component
  Provides the page layout structure for the entire application, including header navigation, content area and footer
-->
<template>
  <div id="basicLayout">
    <!-- Use Ant Design layout component, set minimum height to 100vh -->
    <a-layout style="min-height: 100vh">
      <!-- Header area: includes global navigation bar -->
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>

      <!-- Content area: dynamically renders page content based on routes -->
      <a-layout-content class="content">
        <router-view />
      </a-layout-content>

      <!-- Footer area: displays project links -->
      <a-layout-footer class="footer">
        <a href="https://github.com/dawn83679/DeepImage" target="_blank">
          <GithubOutlined /> GitHub
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
// Import global header component
import GlobalHeader from '@/components/GlobalHeader.vue'
// Import GitHub icon component
import { GithubOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
/* Header styles */
#basicLayout .header {
  background-color: white;
  margin-bottom: 16px;
  color: unset;
  padding-inline: 20px;
}

/* Footer styles - fixed at bottom of page */
#basicLayout .footer {
  background-color: #efefef;
  padding: 16px;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  text-align: center;
}

/* Content area styles - gradient background */
#basicLayout .content {
  padding: 20px;
  background: linear-gradient(to right, #efefef, #fff);
  margin-bottom: 28px; /* Reserve space for fixed footer */
}
</style>
