<template>
  <div class="min-h-screen bg-gradient-to-r from-white to-gray-50">
    <!-- 主容器 -->
    <div class="max-w-7xl mx-auto px-6 py-8">
      <div class="flex flex-row h-screen gap-6">
        <!-- 左侧主区域 (90%) -->
        <div class="flex-1" style="flex: 0 0 90%;">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 h-full">
            <!-- 标题区域 -->
            <div class="text-center mb-12">
              <h1 class="text-4xl font-bold bg-gradient-to-r from-blue-600 to-purple-600 bg-clip-text text-transparent mb-3">
                TinyFlow
              </h1>
              <p class="text-gray-600 text-lg">缩短链接，加速分享</p>
            </div>

            <!-- 输入区域 -->
            <div class="max-w-2xl mx-auto">
              <div class="mb-6">
                <input
                  v-model="inputUrl"
                  type="url"
                  placeholder="请输入长链接（必须以 http:// 或 https:// 开头）"
                  class="w-full px-6 py-4 text-lg border-2 border-gray-200 rounded-xl focus:border-blue-500 focus:ring-4 focus:ring-blue-100 outline-none transition-all duration-200"
                  :class="{ 'border-red-500 focus:border-red-500 focus:ring-red-100': error }"
                  @keyup.enter="generateShortUrl"
                />
              </div>

              <!-- 生成按钮 -->
              <button
                @click="generateShortUrl"
                :disabled="loading || !inputUrl.trim()"
                class="w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white font-semibold py-4 px-8 rounded-xl transition-all duration-200 transform hover:scale-[1.02] disabled:scale-100 disabled:cursor-not-allowed flex items-center justify-center text-lg"
              >
                <svg v-if="loading" class="animate-spin -ml-1 mr-3 h-5 w-5 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                </svg>
                {{ loading ? '生成中...' : '生成链接' }}
              </button>

              <!-- 错误提示 -->
              <div v-if="error" class="mt-4 p-4 bg-red-50 border border-red-200 rounded-lg">
                <p class="text-red-600 text-sm">{{ error }}</p>
              </div>

              <!-- 成功结果 -->
              <div v-if="shortUrl" class="mt-8 p-6 bg-gray-50 rounded-xl border border-gray-200">
                <div class="flex items-center justify-between mb-4">
                  <label class="text-sm font-medium text-gray-700">生成的短链接：</label>
                  <button
                    @click="showQrModal = true"
                    class="p-2 text-gray-500 hover:text-blue-600 transition-colors"
                    title="查看二维码"
                  >
                    <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v1m6 11h2m-6 0h-2v4m0-11v3m0 0h.01M12 12h4.01M16 16h4.01M5 12V7a1 1 0 011-1h4m0 0V5a1 1 0 011-1h4m0 0v1m0 0h4.01M12 8h.01M8 12h.01M8 16h.01M12 20h.01M16 20h.01M20 12h.01M4 12h.01M4 8h.01M4 16h.01"></path>
                    </svg>
                  </button>
                </div>
                <div class="flex items-center gap-3">
                  <a
                    :href="shortUrl"
                    target="_blank"
                    class="flex-1 font-mono text-blue-600 hover:text-blue-800 underline break-all"
                  >
                    {{ shortUrl }}
                  </a>
                  <button
                    @click="copyToClipboard(shortUrl)"
                    class="px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors flex items-center gap-2"
                    :class="{ 'bg-green-600 hover:bg-green-700': copied }"
                  >
                    <svg v-if="!copied" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 16H6a2 2 0 01-2-2V6a2 2 0 012-2h8a2 2 0 012 2v2m-6 12h8a2 2 0 002-2v-8a2 2 0 00-2-2h-8a2 2 0 00-2 2v8a2 2 0 002 2z"></path>
                    </svg>
                    <span v-if="copied">✅</span>
                    {{ copied ? '已复制' : '复制' }}
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧历史栏 (10%) -->
        <div class="w-20 flex-shrink-0" style="min-width: 60px; max-width: 100px;">
          <div class="bg-white rounded-2xl shadow-sm border border-gray-100 h-full overflow-hidden">
            <div class="h-full overflow-y-auto p-2">
              <div v-if="history.length === 0" class="flex items-center justify-center h-full">
                <div class="text-center">
                  <p class="text-gray-400 text-xs">暂无记录</p>
                </div>
              </div>
              <div v-else class="space-y-2">
                <div
                  v-for="(item, index) in history"
                  :key="index"
                  @click="copyHistoryItem(item.shortUrl)"
                  class="w-full h-10 bg-gray-50 hover:bg-blue-50 rounded-lg cursor-pointer transition-colors flex items-center justify-center group relative"
                  :title="item.originalUrl"
                >
                  <div class="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center">
                    <span class="text-xs font-medium text-blue-600">{{ index + 1 }}</span>
                  </div>
                  <!-- Tooltip -->
                  <div class="absolute left-full ml-2 bg-gray-800 text-white text-xs rounded px-2 py-1 opacity-0 group-hover:opacity-100 transition-opacity z-10 whitespace-nowrap pointer-events-none">
                    {{ item.shortUrl }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 二维码模态框 -->
    <div
      v-if="showQrModal"
      class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      @click="showQrModal = false"
    >
      <div class="bg-white rounded-2xl p-8 max-w-sm mx-4" @click.stop>
        <div class="text-center">
          <h3 class="text-xl font-semibold mb-4">二维码</h3>
          <img
            :src="qrCodeUrl"
            alt="QR Code"
            class="mx-auto mb-4 border border-gray-200 rounded-lg"
          />
          <p class="text-sm text-gray-600 mb-4 break-all">{{ shortUrl }}</p>
          <button
            @click="showQrModal = false"
            class="px-6 py-2 bg-gray-600 hover:bg-gray-700 text-white rounded-lg transition-colors"
          >
            关闭
          </button>
        </div>
      </div>
    </div>

    <!-- Snackbar 通知 -->
    <div
      v-if="showSnackbar"
      class="fixed top-4 left-1/2 transform -translate-x-1/2 bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg z-50 transition-all duration-300"
    >
      ✅ 已复制到剪贴板
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'

// 响应式数据
const inputUrl = ref('')
const shortUrl = ref('')
const loading = ref(false)
const error = ref('')
const copied = ref(false)
const showQrModal = ref(false)
const showSnackbar = ref(false)
const history = ref([])

// 计算属性
const qrCodeUrl = computed(() => {
  if (!shortUrl.value) return ''
  return `https://chart.googleapis.com/chart?cht=qr&chs=200x200&chl=${encodeURIComponent(shortUrl.value)}`
})

// 生成短链接
const generateShortUrl = async () => {
  if (!inputUrl.value.trim()) return

  // 验证URL格式
  if (!inputUrl.value.match(/^https?:\/\/.+/)) {
    error.value = '请输入有效的URL（必须以 http:// 或 https:// 开头）'
    return
  }

  loading.value = true
  error.value = ''

  try {
    const response = await axios.post('/api/v1/shorten', {
      url: inputUrl.value.trim()
    })

    shortUrl.value = response.data.shortUrl
    
    // 添加到历史记录
    addToHistory(inputUrl.value, shortUrl.value)
    
    // 重置输入
    inputUrl.value = ''
  } catch (err) {
    console.error('生成短链接失败:', err)
    if (err.response?.status === 400) {
      error.value = '链接格式无效'
    } else if (err.response?.status === 409) {
      error.value = '短码已被占用，请重试'
    } else {
      error.value = '生成失败，请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

// 复制到剪贴板
const copyToClipboard = async (text) => {
  try {
    await navigator.clipboard.writeText(text)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  } catch (err) {
    console.error('复制失败:', err)
    // 降级方案
    const textArea = document.createElement('textarea')
    textArea.value = text
    document.body.appendChild(textArea)
    textArea.select()
    document.execCommand('copy')
    document.body.removeChild(textArea)
    copied.value = true
    setTimeout(() => {
      copied.value = false
    }, 2000)
  }
}

// 复制历史项目
const copyHistoryItem = async (url) => {
  await copyToClipboard(url)
  showSnackbar.value = true
  setTimeout(() => {
    showSnackbar.value = false
  }, 2000)
}

// 添加到历史记录
const addToHistory = (originalUrl, shortUrl) => {
  const newItem = {
    originalUrl,
    shortUrl,
    timestamp: Date.now()
  }
  
  // 添加到数组开头
  history.value.unshift(newItem)
  
  // 保持最多10条记录
  if (history.value.length > 10) {
    history.value = history.value.slice(0, 10)
  }
  
  // 保存到localStorage
  localStorage.setItem('tinyflow_history', JSON.stringify(history.value))
}

// 从localStorage加载历史记录
const loadHistory = () => {
  try {
    const saved = localStorage.getItem('tinyflow_history')
    if (saved) {
      history.value = JSON.parse(saved)
    }
  } catch (err) {
    console.error('加载历史记录失败:', err)
    history.value = []
  }
}

// 组件挂载时加载历史记录
onMounted(() => {
  loadHistory()
})
</script>

<style scoped>
/* 自定义滚动条样式 */
.overflow-y-auto::-webkit-scrollbar {
  width: 4px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #d1d5db;
  border-radius: 2px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #9ca3af;
}

/* 输入框聚焦时的阴影效果 */
input:focus {
  box-shadow: 0 0 0 3px rgba(59, 130, 246, 0.1);
}

/* 确保在移动端的响应式布局 */
@media (max-width: 768px) {
  .flex-row {
    flex-direction: column;
  }
  
  .w-20 {
    width: 100%;
    height: 120px;
    min-width: unset;
    max-width: unset;
  }
  
  .overflow-y-auto {
    overflow-x: auto;
    overflow-y: hidden;
  }
  
  .space-y-2 {
    display: flex;
    gap: 0.5rem;
  }
  
  .space-y-2 > * {
    margin-top: 0;
  }
}

/* 平滑过渡动画 */
.transition-all {
  transition: all 0.2s ease-in-out;
}

/* 按钮悬停效果 */
button:hover {
  transform: translateY(-1px);
}

button:active {
  transform: translateY(0);
}

/* 模态框背景模糊效果 */
.fixed.inset-0 {
  backdrop-filter: blur(4px);
}
</style>