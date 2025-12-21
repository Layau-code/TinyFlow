<template>
  <div class="min-h-screen flex items-center justify-center px-4" style="background-color:var(--tf-bg-page)">
    <div class="max-w-md w-full">
      <!-- Logo -->
      <div class="text-center mb-8">
        <h1 class="font-semibold text-[36px] mb-2" style="color:var(--tf-brand-primary)">TinyFlow</h1>
        <p class="text-[16px]" style="color:var(--tf-text-body)">{{ isLogin ? '登录到你的账户' : '创建新账户' }}</p>
      </div>

      <!-- 登录/注册表单卡片（飞书风格）-->
      <div class="fs-card p-8">
        <!-- 切换按钮 -->
        <div class="mb-6">
          <div class="flex gap-2 p-1 rounded-lg" style="background:var(--tf-bg-page)">
            <button
              @click="isLogin = true"
              :class="[
                'flex-1 py-2 px-4 rounded-md font-medium transition-all text-[14px]',
                isLogin ? 'bg-white shadow-sm' : ''
              ]"
              :style="isLogin ? { color: 'var(--tf-brand-primary)' } : { color: 'var(--tf-text-muted)' }"
            >
              登录
            </button>
            <button
              @click="isLogin = false"
              :class="[
                'flex-1 py-2 px-4 rounded-md font-medium transition-all text-[14px]',
                !isLogin ? 'bg-white shadow-sm' : ''
              ]"
              :style="!isLogin ? { color: 'var(--tf-brand-primary)' } : { color: 'var(--tf-text-muted)' }"
            >
              注册
            </button>
          </div>
        </div>

        <form @submit.prevent="handleSubmit" class="space-y-4">
          <!-- 用户名 -->
          <div>
            <label class="block text-sm font-medium mb-2" style="color:var(--tf-text-body)">用户名</label>
            <input v-model="formData.username" type="text" required class="fs-input w-full" placeholder="请输入用户名" />
          </div>

          <!-- 邮箱（仅注册）-->
          <div v-if="!isLogin">
            <label class="block text-sm font-medium mb-2" style="color:var(--tf-text-body)">邮箱</label>
            <input v-model="formData.email" type="email" class="fs-input w-full" placeholder="请输入邮箱（可选）" />
          </div>

          <!-- 密码 -->
          <div>
            <label class="block text-sm font-medium mb-2" style="color:var(--tf-text-body)">密码</label>
            <input v-model="formData.password" type="password" required class="fs-input w-full" :placeholder="isLogin ? '请输入密码' : '请设置密码（至少6位）'" :minlength="isLogin ? 1 : 6" />
          </div>

          <!-- 错误提示 -->
          <div v-if="error" class="p-3 rounded-lg text-sm" style="background:rgba(245,74,69,0.1);border:1px solid rgba(245,74,69,0.3);color:var(--tf-error)">
            {{ error }}
          </div>

          <!-- 提交按钮 -->
          <button type="submit" :disabled="loading" class="fs-btn-primary w-full h-11">
            {{ loading ? '处理中...' : (isLogin ? '登录' : '注册') }}
          </button>
        </form>

        <!-- 匿名访问提示 -->
        <div class="mt-6 text-center">
          <button @click="skipLogin" class="text-sm hover:underline transition" style="color:var(--tf-text-muted)">
            暂时跳过，匿名访问 →
          </button>
        </div>
      </div>

      <!-- 底部提示 -->
      <div class="mt-6 text-center text-sm" style="color:var(--tf-text-muted)">
        {{ isLogin ? '还没有账户？' : '已有账户？' }}
        <button @click="isLogin = !isLogin" class="font-medium ml-1 hover:underline" style="color:var(--tf-brand-primary)">
          {{ isLogin ? '立即注册' : '立即登录' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuth } from '../composables/useAuth'
import axios from 'axios'

console.log('LoginPage.vue 组件正在加载...')

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
    console.log('响应头:', response.headers)
    const data = response.data

    // 处理返回结果 - 支持两种格式：
    // 1. Token 在响应体: { success: true, data: { token: "xxx" } }
    // 2. Token 在响应头: Authorization: "Bearer xxx"
    let token = null
    let username = formData.value.username

    // 方式1: 从响应头获取 Token
    const authHeader = response.headers['authorization'] || response.headers['Authorization']
    if (authHeader && authHeader.startsWith('Bearer ')) {
      token = authHeader.substring(7) // 移除 "Bearer " 前缀
      console.log('从响应头提取 Token:', token)
    }
    
    // 方式2: 从响应体获取 Token（备用）
    if (!token && data.success && data.data && data.data.token) {
      token = data.data.token
      username = data.data.username || username
      console.log('从响应体提取 Token:', token)
    }

    if (!token) {
      console.error('登录失败，未找到 Token。响应数据:', data, '响应头:', response.headers)
      throw new Error(data.message || '登录失败：未返回有效 Token')
    }

    console.log('提取 Token:', token)
    console.log('提取用户名:', username)

    // 存储 Token 并更新响应式状态
    setToken(token, username)
    console.log('Token 已存储，响应式状态已更新，准备跳转首页')

    // 跳转到首页
    await router.push('/')
    console.log('已跳转到首页')
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
  setToken('', '')
  router.push('/')
}
</script>

<style scoped>
/* 飞书风格登录页面 */
</style>
