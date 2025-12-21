import { createRouter, createWebHistory } from 'vue-router'

const DashboardPage = () => import('../pages/DashboardPage.vue')
const StatsPage = () => import('../pages/StatsPage.vue')
const LoginPage = () => import('../pages/LoginPage.vue')

// 仅声明统计相关页面的路由；首页由 App.vue 直接渲染
const routes = [
  { path: '/login', component: LoginPage, meta: { public: true } },
  { path: '/dashboard', component: DashboardPage },
  { path: '/stats/:shortCode', component: StatsPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：检查登录状态（可选，因为支持匿名访问）
// 如果需要强制登录，取消下面的注释
/*
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const isPublic = to.meta.public
  
  if (!isPublic && !token) {
    next('/login')
  } else {
    next()
  }
})
*/

export default router
export { router }