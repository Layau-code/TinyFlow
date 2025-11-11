import { createRouter, createWebHistory } from 'vue-router'
import { defineAsyncComponent } from 'vue'

const DashboardPage = defineAsyncComponent(() => import('../pages/DashboardPage.vue'))
const StatsPage = defineAsyncComponent(() => import('../pages/StatsPage.vue'))

// 仅声明统计相关页面的路由；首页由 App.vue 直接渲染
const routes = [
  { path: '/dashboard', component: DashboardPage },
  { path: '/stats/:shortCode', component: StatsPage },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

export default router
export { router }