<template>
  <!-- 全屏登录页面 - 飞书风格 -->
  <div class="min-h-screen w-full flex font-sans">
    <!-- 左侧品牌区域 - 25% 宽度，渐变背景 -->
    <div class="hidden lg:flex flex-col w-1/4 min-w-[340px] relative overflow-hidden bg-gradient-to-br from-slate-50 via-blue-50 to-indigo-100">
      <!-- 装饰性背景元素 -->
      <div class="absolute inset-0 overflow-hidden">
        <!-- 主装饰圆 -->
        <div class="absolute -top-40 -left-40 w-[500px] h-[500px] rounded-full bg-gradient-to-br from-blue-200/40 to-indigo-300/30 blur-3xl"></div>
        <div class="absolute top-1/3 -right-20 w-[400px] h-[400px] rounded-full bg-gradient-to-br from-cyan-200/30 to-blue-300/20 blur-3xl"></div>
        <div class="absolute -bottom-32 left-1/4 w-[450px] h-[450px] rounded-full bg-gradient-to-br from-violet-200/30 to-indigo-200/20 blur-3xl"></div>
        
        <!-- 精致的装饰线条 -->
        <svg class="absolute inset-0 w-full h-full opacity-[0.06]" xmlns="http://www.w3.org/2000/svg">
          <defs>
            <pattern id="grid" width="60" height="60" patternUnits="userSpaceOnUse">
              <path d="M 60 0 L 0 0 0 60" fill="none" stroke="currentColor" stroke-width="0.5" class="text-indigo-500"/>
            </pattern>
          </defs>
          <rect width="100%" height="100%" fill="url(#grid)" />
        </svg>
        
        <!-- 流动的装饰曲线 -->
        <svg class="absolute bottom-0 left-0 w-full h-1/2 opacity-10" viewBox="0 0 1440 320" preserveAspectRatio="none">
          <path fill="url(#wave-gradient)" d="M0,160L48,176C96,192,192,224,288,213.3C384,203,480,149,576,138.7C672,128,768,160,864,181.3C960,203,1056,213,1152,197.3C1248,181,1344,139,1392,117.3L1440,96L1440,320L1392,320C1344,320,1248,320,1152,320C1056,320,960,320,864,320C768,320,672,320,576,320C480,320,384,320,288,320C192,320,96,320,48,320L0,320Z"></path>
          <defs>
            <linearGradient id="wave-gradient" x1="0%" y1="0%" x2="100%" y2="0%">
              <stop offset="0%" style="stop-color:#6366f1;stop-opacity:0.3" />
              <stop offset="50%" style="stop-color:#3b82f6;stop-opacity:0.2" />
              <stop offset="100%" style="stop-color:#06b6d4;stop-opacity:0.3" />
            </linearGradient>
          </defs>
        </svg>
      </div>
      
      <!-- 内容区域 -->
      <div class="relative z-10 flex flex-col h-full px-10 py-8">
        <!-- 顶部小 Logo -->
        <div class="flex items-center gap-2">
          <img src="/logo.png" alt="TinyFlow" class="h-6 w-auto" />
          <span class="text-[18px] font-semibold text-gray-700">TinyFlow</span>
        </div>
      
      <!-- 中间主内容区域 -->
      <div class="flex-1 flex flex-col justify-start items-start pt-56 px-2">
        <!-- 大 Logo + 名称 -->
        <div class="flex items-center gap-1 mb-4 w-full justify-end pr-2">
          <img src="/logo.png" alt="TinyFlow" class="h-8 w-auto flex-shrink-0" />
          <span class="text-[44px] font-bold text-gray-800">TinyFlow</span>
        </div>
        
        <!-- 主标语 -->
        <div class="mb-12 text-right w-full pr-2">
          <h2 class="text-[24px] font-semibold text-gray-800 leading-tight">现在注册</h2>
          <h2 class="text-[32px] font-bold gradient-text-animated leading-tight">让分享更轻松</h2>
        </div>
        
        <!-- 三段卖点 - 简洁线条图标 -->
        <div class="space-y-6">
          <!-- 卖炷1 -->
          <div class="flex items-start gap-4">
            <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-1" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M13 10V3L4 14h7v7l9-11h-7z"></path>
            </svg>
            <div>
              <div class="text-[14px] font-semibold text-gray-800 mb-1">毫秒级跳转</div>
              <div class="text-[13px] text-gray-500 leading-relaxed">基于分布式架构优化，每次跳转均控制在毫秒级，高效不失可靠</div>
            </div>
          </div>
        
          <!-- 卖炷2 -->
          <div class="flex items-start gap-4">
            <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-1" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
            </svg>
            <div>
              <div class="text-[14px] font-semibold text-gray-800 mb-1">实时数据分析</div>
              <div class="text-[13px] text-gray-500 leading-relaxed">多维度报表展示来源、设备、地域等数据分布，精准掌握用户特征与趋势</div>
            </div>
          </div>
        
          <!-- 卖炷3 -->
          <div class="flex items-start gap-4">
            <svg class="w-5 h-5 text-blue-500 flex-shrink-0 mt-1" fill="none" stroke="currentColor" stroke-width="2.5" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8z"></path>
            </svg>
            <div>
              <div class="text-[14px] font-semibold text-gray-800 mb-1">安全可靠</div>
              <div class="text-[13px] text-gray-500 leading-relaxed">企业级侵入检测与数据加密保护，灵活的权限管理体系，构建深层防御线</div>
            </div>
          </div>
        </div>
      </div>
      
      <!-- 底部版权 -->
      <div>
        <div class="text-[14px] text-gray-400">© 2025 TinyFlow. All rights reserved.</div>
      </div>
      </div>
    </div>

    <!-- 右侧登录表单区域 - 75% 宽度 -->
    <div class="flex-1 flex items-start justify-center pt-80 bg-white relative">
      <!-- 登录表单容器 -->
      <div class="w-[360px]">
        <!-- 移动端 Logo -->
        <div class="lg:hidden flex items-center gap-3 mb-10">
          <img src="/logo.png" alt="TinyFlow" class="h-10 w-auto" />
          <span class="text-2xl font-bold text-gray-800">TinyFlow</span>
        </div>

        <!-- 登录卡片 -->
        <div class="bg-white rounded-2xl p-8 md:p-150 shadow-xl border border-gray-100">
          <!-- 标题 -->
          <div class="mb-6">
            <h1 class="text-[18px] font-bold text-gray-900 mb-1">
              {{ isLogin ? '欢迎使用 TinyFlow' : '创建新账户' }}
            </h1>
            <p class="text-[13px] text-gray-500">
              {{ isLogin ? '登录以管理您的短链和统计数据' : '注册后即可享受全部功能' }}
            </p>
          </div>

          <!-- 登录/注册表单 -->
          <form @submit.prevent="handleSubmit" class="space-y-5">
            <!-- 用户名 -->
            <div>
              <label class="block text-[12px] font-medium text-gray-700 mb-1.5">用户名</label>
              <input
                v-model="formData.username"
                type="text"
                required
                class="w-full h-10 px-3 rounded-lg border border-gray-200 text-[13px] transition-all duration-200 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 bg-white hover:border-gray-300"
                :placeholder="isLogin ? '输入您的用户名' : '设置用户名'"
              />
            </div>

            <!-- 邮箱（仅注册时显示）-->
            <div v-if="!isLogin">
              <label class="block text-[12px] font-medium text-gray-700 mb-1.5">邮箱地址</label>
              <input
                v-model="formData.email"
                type="email"
                class="w-full h-10 px-3 rounded-lg border border-gray-200 text-[13px] transition-all duration-200 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 bg-white hover:border-gray-300"
                placeholder="输入您的邮箱地址（可选）"
              />
            </div>

            <!-- 密码 -->
            <div>
              <label class="block text-[12px] font-medium text-gray-700 mb-1.5">密码</label>
              <input
                v-model="formData.password"
                type="password"
                required
                class="w-full h-10 px-3 rounded-lg border border-gray-200 text-[13px] transition-all duration-200 focus:ring-2 focus:ring-blue-500/20 focus:border-blue-500 bg-white hover:border-gray-300"
                :placeholder="isLogin ? '输入您的密码' : '设置密码（至少6位）'"
                :minlength="isLogin ? 1 : 6"
              />
            </div>

            <!-- 错误提示 -->
            <div
              v-if="error"
              class="p-2.5 rounded-lg text-[12px] bg-red-50 border border-red-100 text-red-600"
            >
              {{ error }}
            </div>

            <!-- 提交按钮 -->
            <button
              type="submit"
              :disabled="loading"
              class="w-full h-10 rounded-lg font-semibold text-white text-[13px] transition-all duration-200 hover:opacity-90 disabled:opacity-70 disabled:cursor-not-allowed bg-blue-500 hover:bg-blue-600"
            >
              {{ loading ? '处理中...' : (isLogin ? '登录' : '创建账户') }}
            </button>
          </form>

          <!-- 跳过登录 -->
          <div class="mt-4 text-center">
            <button
              @click="skipLogin"
              class="text-[12px] text-gray-400 hover:text-blue-500 transition-colors"
            >
              暂不登录，先看看 →
            </button>
          </div>

          <!-- 底部切换提示 -->
          <div class="mt-4 pt-4 border-t border-gray-100 text-center text-[12px] text-gray-500">
            {{ isLogin ? '还没有账户？' : '已有账户？' }}
            <button
              @click="isLogin = !isLogin"
              class="font-medium text-blue-500 hover:text-blue-600 ml-1 transition-colors"
            >
              {{ isLogin ? '立即注册' : '立即登录' }}
            </button>
          </div>
        </div>
      </div>

      <!-- 右下角语言切换 -->
      <div class="absolute bottom-6 right-6 flex items-center gap-1 text-[13px] text-gray-400 cursor-pointer hover:text-gray-600">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 12a9 9 0 01-9 9m9-9a9 9 0 00-9-9m9 9H3m9 9a9 9 0 01-9-9m9 9c1.657 0 3-4.03 3-9s-1.343-9-3-9m0 18c-1.657 0-3-4.03-3-9s1.343-9 3-9m-9 9a9 9 0 019-9"></path>
        </svg>
        <span>简体中文</span>
      </div>

      <!-- 右侧客服浮窗 -->
      <div class="fixed right-4 top-1/2 -translate-y-1/2 flex flex-col gap-2">
        <div class="w-10 h-10 rounded-lg bg-white shadow-lg border border-gray-100 flex items-center justify-center cursor-pointer hover:shadow-xl transition-shadow" title="扫码登录">
          <svg class="w-5 h-5 text-gray-500" fill="currentColor" viewBox="0 0 24 24">
            <path d="M3 11h8V3H3v8zm2-6h4v4H5V5zm8-2v8h8V3h-8zm6 6h-4V5h4v4zM3 21h8v-8H3v8zm2-6h4v4H5v-4zm13 2h-2v2h2v2h2v-2h2v-2h-2v-2h-2v2zm0-8h2v2h-2v-2zm-4 4h2v4h-2v-4zm-4 6h2v4h-2v-4z"/>
          </svg>
        </div>
        <div class="w-10 h-10 rounded-lg bg-white shadow-lg border border-gray-100 flex items-center justify-center cursor-pointer hover:shadow-xl transition-shadow" title="联系客服">
          <svg class="w-5 h-5 text-gray-500" fill="currentColor" viewBox="0 0 24 24">
            <path d="M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zm0 14H5.17L4 17.17V4h16v12z"/>
            <path d="M12 10c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm-4 0c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1zm8 0c.55 0 1-.45 1-1s-.45-1-1-1-1 .45-1 1 .45 1 1 1z"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import axios from 'axios'

const router = useRouter()
const { setToken } = useAuth()
const isLogin = ref(true)
const loading = ref(false)
const error = ref('')

const formData = ref({
  username: '',
  email: '',
  password: ''
})

onMounted(() => {
  console.log('LoginPage 组件已挂载')
})

const handleSubmit = async () => {
  error.value = ''
  loading.value = true

  try {
    const endpoint = isLogin.value ? '/api/auth/login' : '/api/auth/register'
    const payload = isLogin.value
      ? { username: formData.value.username, password: formData.value.password }
      : { username: formData.value.username, email: formData.value.email || undefined, password: formData.value.password }

    const response = await axios.post(endpoint, payload)
    const data = response.data

    let token = null
    let username = formData.value.username

    const authHeader = response.headers['authorization'] || response.headers['Authorization']
    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.substring(7)
    }
    
    if (!token && data.success && data.data && data.data.token) {
      token = data.data.token
      username = data.data.username || username
    }

    if (!token) {
      throw new Error(data.message || '登录失败：未返回有效 Token')
    }

    setToken(token, username)
    await router.push('/')
  } catch (err) {
    console.error('登录/注册错误:', err)
    if (err.response) {
      const msg = err.response.data?.message || err.response.data?.msg || err.response.data
      error.value = typeof msg === 'string' ? msg : (isLogin.value ? '登录失败,请检查用户名和密码' : '注册失败,请稍后重试')
    } else if (err.request) {
      error.value = '网络错误,请检查后端服务是否启动'
    } else {
      error.value = isLogin.value ? '登录失败,请稍后重试' : '注册失败,请稍后重试'
    }
  } finally {
    loading.value = false
  }
}

const skipLogin = () => {
  setToken('', '')
  sessionStorage.setItem('hasVisited', 'true')
  router.push('/')
}
</script>

<style scoped>
/* 渐变文字样式 - 蓝色系渐变，随时间动态变化 */
.gradient-text-animated {
  background: linear-gradient(90deg, #3b82f6, #1e40af, #3b82f6);
  background-size: 200% 100%;
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
  animation: gradient-flow 3s ease infinite;
}

@keyframes gradient-flow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.gradient-text {
  background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 50%, #ec4899 100%);
  -webkit-background-clip: text;
  background-clip: text;
  color: transparent;
}

/* 无衬线字体 */
.font-sans {
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", sans-serif;
}
</style>
