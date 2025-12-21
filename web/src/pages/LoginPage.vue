<template>
  <div class="min-h-screen flex items-center justify-center bg-gradient-to-br from-blue-50 to-purple-50 px-4">
    <div class="max-w-md w-full">
      <!-- Logo -->
      <div class="text-center mb-8">
        <h1 class="font-bold text-4xl tf-brand mb-2">
          <span class="tf-letter">T</span><span class="tf-letter">i</span><span class="tf-letter">n</span><span class="tf-letter">y</span><span class="tf-letter">F</span><span class="tf-letter">l</span><span class="tf-letter">o</span><span class="tf-letter">w</span>
        </h1>
        <p class="text-gray-600">{{ isLogin ? '登录到你的账户' : '创建新账户' }}</p>
      </div>

      <!-- 登录/注册表单卡片 -->
      <div class="bg-white rounded-2xl shadow-lg p-8 border border-gray-100">
        <div class="mb-6">
          <div class="flex gap-2 p-1 bg-gray-100 rounded-lg">
            <button
              @click="isLogin = true"
              :class="[
                'flex-1 py-2 px-4 rounded-md font-medium transition-all',
                isLogin ? 'bg-white shadow text-blue-600' : 'text-gray-600 hover:text-gray-900'
              ]"
            >
              登录
            </button>
            <button
              @click="isLogin = false"
              :class="[
                'flex-1 py-2 px-4 rounded-md font-medium transition-all',
                !isLogin ? 'bg-white shadow text-blue-600' : 'text-gray-600 hover:text-gray-900'
              ]"
            >
              注册
            </button>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- 用户名 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">用户名</label>
            <input
              v-model="formData.username"
              type="text"
              required
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
              placeholder="请输入用户名"
            />
          </div>

          <!-- 邮箱（仅注册） -->
          <div v-if="!isLogin">
            <label class="block text-sm font-medium text-gray-700 mb-1">邮箱</label>
            <input
              v-model="formData.email"
              type="email"
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
              placeholder="请输入邮箱（可选）"
            />
          </div>

          <!-- 密码 -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">密码</label>
            <input
              v-model="formData.password"
              type="password"
              required
              class="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent outline-none transition"
              :placeholder="isLogin ? '请输入密码' : '请设置密码（至少6位）'"
              :minlength="isLogin ? 1 : 6"
            />
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="p-3 bg-red-50 border border-red-200 rounded-lg text-red-600 text-sm">
            {{ error }}
          </div>

          <!-- 提交按钮 -->
          <button
            type="submit"
            :disabled="loading"
            @click="console.log('按钮被点击了！')"
            class="w-full py-3 px-4 bg-gradient-to-r from-blue-600 to-purple-600 text-white font-medium rounded-lg hover:from-blue-700 hover:to-purple-700 focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2 transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {{ loading ? '处理中...' : (isLogin ? '登录' : '注册') }}
          </button>
        </form>

        <!-- 匿名访问提示 -->
        <div class="mt-6 text-center">
          <button
            @click="skipLogin"
            class="text-sm text-gray-500 hover:text-gray-700 transition"
          >
            暂时跳过，匿名访问 →
          </button>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="mt-6 text-center text-sm text-gray-500">
        {{ isLogin ? '还没有账户？' : '已有账户？' }}
        <button
          @click="isLogin = !isLogin"
          class="text-blue-600 hover:text-blue-700 font-medium ml-1"
        >
          {{ isLogin ? '立即注册' : '立即登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'

console.log('LoginPage.vue 组件正在加载...')

const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)
const error = ref('')

const formData = ref({
  username: '',
  email: '',
  password: ''
})

onMounted(() => {
  console.log('LoginPage 组件已挂载！')
  console.log('测试：点击"立即注册"按钮应该能切换标签')
})

const handleSubmit = async () => {
  console.log('=== 开始登录/注册 ===', { isLogin: isLogin.value, formData: formData.value })
  error.value = ''
  loading.value = true

  try {
    const endpoint = isLogin.value ? '/api/auth/login' : '/api/auth/register'
    const payload = isLogin.value
      ? { username: formData.value.username, password: formData.value.password }
      : { username: formData.value.username, email: formData.value.email || undefined, password: formData.value.password }

    console.log('发送请求:', { endpoint, payload })
    const response = await axios.post(endpoint, payload)
    console.log('收到响应:', response)
    const data = response.data

    // 处理返回结果
    let token = null
    if (data.code === 0 && data.data) {
      // Result<AuthResponse> 包装格式
      token = data.data.token
      console.log('提取 Token (Result格式):', token)
    } else if (data.token) {
      // 直接返回 AuthResponse
      token = data.token
      console.log('提取 Token (直接格式):', token)
    }

    if (!token) {
      console.error('未找到有效 Token:', data)
      throw new Error('登录失败：未返回有效 Token')
    }

    // 存储 Token
    localStorage.setItem('token', token)
    localStorage.setItem('username', data.data?.username || data.username || formData.value.username)
    console.log('Token 已存储，准备跳转首页')

    // 跳转到首页
    router.push('/')
  } catch (err) {
    console.error('登录/注册错误:', err)
    if (err.response) {
      // 后端返回的错误
      console.error('后端错误响应:', err.response.data)
      const msg = err.response.data?.message || err.response.data?.msg || err.response.data
      error.value = typeof msg === 'string' ? msg : (isLogin.value ? '登录失败,请检查用户名和密码' : '注册失败,请稍后重试')
    } else if (err.request) {
      console.error('请求未收到响应:', err.request)
      error.value = '网络错误,请检查后端服务是否启动'
    } else {
      console.error('请求配置错误:', err.message)
      error.value = isLogin.value ? '登录失败,请稍后重试' : '注册失败,请稍后重试'
    }
  } finally {
    loading.value = false
    console.log('=== 登录/注册流程结束 ===')
  }
}

const skipLogin = () => {
  // 清除 Token，以匿名身份访问
  localStorage.removeItem('token')
  localStorage.removeItem('username')
  router.push('/')
}
</script>

<style scoped>
.tf-brand {
  background-image: linear-gradient(135deg, #6B72FF 0%, #8A6BFF 60%, #A66BFF 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  letter-spacing: 0.5px;
}

.tf-letter {
  display: inline-block;
  animation: bounce 0.6s ease-in-out;
}

.tf-letter:nth-child(1) { animation-delay: 0.1s; }
.tf-letter:nth-child(2) { animation-delay: 0.2s; }
.tf-letter:nth-child(3) { animation-delay: 0.3s; }
.tf-letter:nth-child(4) { animation-delay: 0.4s; }
.tf-letter:nth-child(5) { animation-delay: 0.5s; }
.tf-letter:nth-child(6) { animation-delay: 0.6s; }
.tf-letter:nth-child(7) { animation-delay: 0.7s; }
.tf-letter:nth-child(8) { animation-delay: 0.8s; }

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}
</style>
