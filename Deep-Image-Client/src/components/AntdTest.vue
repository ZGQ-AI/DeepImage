<template>
  <div class="antd-test">
    <h2>🎉 Ant Design Vue 测试组件</h2>
    
    <!-- 按钮测试 -->
    <a-space>
      <a-button type="primary">主要按钮</a-button>
      <a-button>默认按钮</a-button>
      <a-button type="dashed">虚线按钮</a-button>
      <a-button type="link">链接按钮</a-button>
    </a-space>

    <!-- 输入框测试 -->
    <div style="margin-top: 20px;">
      <a-input 
        v-model:value="inputValue" 
        placeholder="请输入内容..."
        style="width: 300px;"
      />
      <p>输入的内容: {{ inputValue }}</p>
    </div>

    <!-- 消息提示测试 -->
    <div style="margin-top: 20px;">
      <a-space>
        <a-button @click="showSuccess">成功消息</a-button>
        <a-button @click="showError">错误消息</a-button>
        <a-button @click="showWarning">警告消息</a-button>
        <a-button @click="showInfo">信息消息</a-button>
      </a-space>
    </div>

    <!-- 卡片测试 -->
    <div style="margin-top: 20px;">
      <a-card title="测试卡片" style="width: 400px;">
        <p>这是一个使用 Ant Design Vue 的卡片组件</p>
        <a-button type="primary" @click="showModal">打开对话框</a-button>
      </a-card>
    </div>

    <!-- 对话框 -->
    <a-modal 
      v-model:open="modalVisible" 
      title="测试对话框"
      @ok="handleOk"
      @cancel="handleCancel"
    >
      <p>这是一个测试对话框，Ant Design Vue 集成成功！</p>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'

// 响应式数据
const inputValue = ref('')
const modalVisible = ref(false)

// 消息提示方法
const showSuccess = () => {
  message.success('这是一个成功消息！')
}

const showError = () => {
  message.error('这是一个错误消息！')
}

const showWarning = () => {
  message.warning('这是一个警告消息！')
}

const showInfo = () => {
  message.info('这是一个信息消息！')
}

// 对话框方法
const showModal = () => {
  modalVisible.value = true
}

const handleOk = () => {
  modalVisible.value = false
  message.success('对话框确认成功！')
}

const handleCancel = () => {
  modalVisible.value = false
}
</script>

<style scoped>
.antd-test {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

h2 {
  color: #1890ff;
  text-align: center;
  margin-bottom: 30px;
}
</style>
