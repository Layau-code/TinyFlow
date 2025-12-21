import { ref, computed } from 'vue'
import axios from 'axios'

// 认证状态管理
const token = ref(localStorage.getItem('token') || '')
const username = ref(localStorage.getItem('username') || '')

export function useAuth() {
  const isAuthenticated = computed(() => !!token.value)

  const setToken = (newToken, newUsername) => {
    token.value = newToken
    username.value = newUsername
    if (newToken) {
      localStorage.setItem('token', newToken)
      localStorage.setItem('username', newUsername || '')
    } else {
      localStorage.removeItem('token')
      localStorage.removeItem('username')
    }
  }

  const logout = () => {
    setToken('', '')
  }

  return {
    token: computed(() => token.value),
    username: computed(() => username.value),
    isAuthenticated,
    setToken,
    logout
  }
}

// 配置 Axios 拦截器
export function setupAxiosInterceptors() {
  // 请求拦截器：自动添加 Authorization header
  axios.interceptors.request.use(
    (config) => {
      const currentToken = localStorage.getItem('token')
      if (currentToken) {
        config.headers.Authorization = `Bearer ${currentToken}`
      }
      return config
    },
    (error) => {
      return Promise.reject(error)
    }
  )

  // 响应拦截器：处理 401 未授权
  axios.interceptors.response.use(
    (response) => response,
    (error) => {
      if (error.response?.status === 401) {
        // Token 过期或无效，清除并跳转登录
        localStorage.removeItem('token')
        localStorage.removeItem('username')
        token.value = ''
        username.value = ''
        
        // 如果不在登录页，则跳转
        if (window.location.pathname !== '/login') {
          window.location.href = '/login'
        }
      }
      return Promise.reject(error)
    }
  )
}
