import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

const apiProxyTarget = process.env.VITE_API_PROXY_TARGET || 'http://127.0.0.1:8080'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: apiProxyTarget, // 后端服务
        changeOrigin: true,
      },
      '/shorten': {
        target: apiProxyTarget, // 后端服务
        changeOrigin: true,
      },
    }
  }
})
