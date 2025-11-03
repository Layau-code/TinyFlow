import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      '/api': {
        target: 'http://127.0.0.1:8080', // 后端服务
        changeOrigin: true,
      },
      '/api/v1': {
        target: 'http://127.0.0.1:8080',
        changeOrigin: true,
      },
      '/shorten': {
        target: 'http://127.0.0.1:8080', // 后端服务
        changeOrigin: true,
      },
    }
  }
})