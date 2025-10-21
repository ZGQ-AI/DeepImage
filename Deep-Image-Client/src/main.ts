import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'

import App from './App.vue'
import router from './router'

const app = createApp(App)

// Initialize Pinia first (required by stores)
app.use(createPinia())

// Initialize router
app.use(router)

// Initialize Ant Design UI
app.use(Antd)

app.mount('#app')
