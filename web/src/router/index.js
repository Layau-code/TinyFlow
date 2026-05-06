import { createRouter, createWebHistory } from 'vue-router'

const DashboardPage = () => import('../pages/DashboardPage.vue')
const StatsPage = () => import('../pages/StatsPage.vue')
import StatsDetailPage from '../pages/StatsDetail.vue'
const LoginPage = () => import('../pages/LoginPage.vue')
const AboutPage = () => import('../pages/AboutPage.vue')
const HomePage = () => import('../pages/HomePage.vue')

const routes = [
  { path: '/', component: HomePage, meta: { public: true, hideNav: false } },
  { path: '/login', component: LoginPage, meta: { public: true, hideNav: true } },
  { path: '/dashboard', component: DashboardPage },
  // 支持无 shortCode 的 /stats 路由（便于 demo 预览和直接访问）
  { path: '/stats', component: StatsPage },
  { path: '/stats/:shortCode', component: StatsPage },
  { path: '/stats/:shortCode/detail', component: StatsDetailPage },
  { path: '/about', component: AboutPage, meta: { public: true } },
  // 短链跳转路由（匹配 4-8 位字母数字组合）
  // 注意：短链跳转应直接访问后端API，而不是前端路由
  // 此路由仅作为备用，正常情况下应该由 Nginx 直接转发到后端
  // { 
  //   path: '/:shortCode(\\w{4,8})', 
  //   component: RedirectPage, 
  //   meta: { public: true, hideNav: true } 
  // },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：首次访问时检查是否需要跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const hasVisited = sessionStorage.getItem('hasVisited')
  
  // 如果 URL 带 demo 参数，优先引导到 demo stats 页面，方便快捷预览
  const demoFlag = to.query && (to.query.demo === '1' || to.query.demo === 'true')
  if (demoFlag) {
    // 如果已经在 stats 页面，继续；否则跳转到 demo shortCode 页面
    if (to.path.startsWith('/stats')) {
      next()
      return
    } else {
      next({ path: '/stats/demo123', query: { demo: '1' } })
      return
    }
  }

  // 首次访问且未登录时，跳转到登录页（保留原逻辑）
  if (to.path === '/' && !token && !hasVisited) {
    sessionStorage.setItem('hasVisited', 'true')
    next('/login')
  } else {
    next()
  }
})

export default router
export { router }