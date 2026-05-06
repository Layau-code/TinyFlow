import './style.css'
import { createApp } from 'vue'
import App from '../App.vue'
import router from './router'
import { i18n } from './i18n'
import { setupAxiosInterceptors } from './composables/useAuth'

// 配置 Axios 拦截器
setupAxiosInterceptors()

createApp(App).use(router).use(i18n).mount('#app')
