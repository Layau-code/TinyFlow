<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-indigo-50">
    <div class="text-center space-y-6 p-8">
      <!-- 加载动画 -->
      <div v-if="loading" class="space-y-4">
        <div class="inline-block">
          <svg class="animate-spin h-16 w-16 text-blue-600" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
          </svg>
        </div>
        <div class="text-xl font-medium text-gray-700">正在跳转...</div>
        <div class="text-sm text-gray-500">{{ shortCode }}</div>
      </div>

      <!-- 错误提示 -->
      <div v-if="error" class="space-y-4">
        <div class="text-6xl">😢</div>
        <div class="text-2xl font-semibold text-gray-800">链接不存在</div>
        <div class="text-gray-600">短码 <span class="font-mono bg-gray-100 px-2 py-1 rounded">{{ shortCode }}</span> 未找到</div>
        <button @click="goHome" class="mt-4 px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors">
          返回首页
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_BASE } from '../composables/shortBase'

const route = useRoute()
const router = useRouter()
const shortCode = ref(route.params.shortCode)
const loading = ref(true)
const error = ref(false)

onMounted(async () => {
  try {
    // 调用后端跳转接口
    const response = await axios.get(`${API_BASE}/api/redirect/${shortCode.value}`, {
      maxRedirects: 0, // 禁止 axios 自动跟随重定向
      validateStatus: (status) => status === 302 || status === 301 || status < 400
    })

    // 从响应头获取 Location
    const location = response.headers.location || response.headers.Location
    
    if (location) {
      // 跳转到目标网址
      window.location.href = location
    } else {
      // 如果没有 Location 头，可能后端直接返回了长链接
      if (response.data && typeof response.data === 'string' && response.data.startsWith('http')) {
        window.location.href = response.data
      } else {
        error.value = true
        loading.value = false
      }
    }
  } catch (e) {
    console.error('短链跳转失败:', e)
    error.value = true
    loading.value = false
  }
})

function goHome() {
  router.push('/')
}
</script>

<style scoped>
/* 可以添加自定义样式 */
</style>
