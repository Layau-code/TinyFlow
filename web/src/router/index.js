import { createRouter, createWebHistory } from 'vue-router'

const DashboardPage = () => import('../pages/DashboardPage.vue')
const StatsPage = () => import('../pages/StatsPage.vue')
const LoginPage = () => import('../pages/LoginPage.vue')
const AboutPage = () => import('../pages/AboutPage.vue')
const HomePage = () => import('../pages/HomePage.vue')
const RedirectPage = () => import('../pages/RedirectPage.vue')

const routes = [
  { path: '/', component: HomePage, meta: { public: true, hideNav: false } },
  { path: '/login', component: LoginPage, meta: { public: true, hideNav: true } },
  { path: '/dashboard', component: DashboardPage },
  { path: '/stats/:shortCode', component: StatsPage },
  { path: '/about', component: AboutPage, meta: { public: true } },
  // 短链跳转路由（匹配 4-8 位字母数字组合）
  { 
    path: '/:shortCode(\\w{4,8})', 
    component: RedirectPage, 
    meta: { public: true, hideNav: true } 
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// 路由守卫：首次访问时检查是否需要跳转登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const hasVisited = sessionStorage.getItem('hasVisited')
  
  // 首次访问且未登录时，跳转到登录页
  if (to.path === '/' && !token && !hasVisited) {
    sessionStorage.setItem('hasVisited', 'true')
    next('/login')
  } else {
    next()
  }
})

export default router
export { router }