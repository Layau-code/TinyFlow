<template>
  <div class="min-h-screen dashboard-page pt-24">
    <div class="max-w-7xl mx-auto p-6 space-y-6">
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between flex-wrap gap-4">
        <h1 class="text-xl font-semibold text-gray-800">数据看板</h1>
        <div class="flex items-center gap-3">
          <input v-model="searchQuery" type="text" placeholder="搜索短链..." class="search-input" />
          <button @click="refreshAll" class="btn btn-secondary">刷新</button>
          <button @click="exportCSV" class="btn btn-secondary">导出CSV</button>
        </div>
      </div>

      <!-- 全局统计概览 -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4">
        <div class="metric-card">
          <div class="metric-label">总短链数</div>
          <div class="metric-value">{{ globalStats?.totalUrls ?? '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">总点击量(PV)</div>
          <div class="metric-value">{{ globalStats?.totalClicks ?? '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">独立访客(UV)</div>
          <div class="metric-value">{{ globalStats?.totalUniqueIps ?? '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">今日点击</div>
          <div class="metric-value">{{ globalStats?.todayClicks ?? '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">活跃短链</div>
          <div class="metric-value">{{ globalStats?.activeUrls ?? '-' }}</div>
        </div>
      </div>

      <!-- 图表区域 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 访问趋势 -->
        <div class="card">
          <div class="card-header flex items-center justify-between">
            <span>访问趋势</span>
            <div class="flex gap-2">
              <button class="btn-tab" :class="{ active: trendDays === 7 }" @click="setTrendDays(7)">7天</button>
              <button class="btn-tab" :class="{ active: trendDays === 14 }" @click="setTrendDays(14)">14天</button>
              <button class="btn-tab" :class="{ active: trendDays === 30 }" @click="setTrendDays(30)">30天</button>
            </div>
          </div>
          <div class="card-body">
            <div class="chart-container">
              <Suspense>
                <TrendChart :values="dailyTrendValues" :labels="dailyTrendLabels" :showValues="true" />
                <template #fallback><div class="chart-placeholder"></div></template>
              </Suspense>
            </div>
          </div>
        </div>

        <!-- 设备分布 - 饼图 -->
        <div class="card">
          <div class="card-header">设备分布</div>
          <div class="card-body">
            <Suspense>
              <PieDonut :data="devicePieData" :colors="['#2563eb', '#60a5fa', '#93c5fd', '#bfdbfe']" />
              <template #fallback><div class="chart-placeholder"></div></template>
            </Suspense>
          </div>
        </div>

        <!-- 城市TOP10 -->
        <div class="card">
          <div class="card-header">城市 TOP 10</div>
          <div class="card-body">
            <div class="rank-list">
              <div v-for="(item, idx) in cityTop10" :key="item.key" class="rank-item">
                <span class="rank-num">{{ idx + 1 }}</span>
                <span class="rank-name">{{ item.key || '未知' }}</span>
                <span class="rank-count">{{ item.count }}</span>
              </div>
              <div v-if="!cityTop10.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- 来源TOP10 -->
        <div class="card">
          <div class="card-header">来源域名 TOP 10</div>
          <div class="card-body">
            <div class="rank-list">
              <div v-for="(item, idx) in sourceTop10" :key="item.key" class="rank-item">
                <span class="rank-num">{{ idx + 1 }}</span>
                <span class="rank-name truncate">{{ item.key || '直接访问' }}</span>
                <span class="rank-count">{{ item.count }}</span>
              </div>
              <div v-if="!sourceTop10.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热门短链排行 -->
      <div class="card">
        <div class="card-header">热门短链 TOP 10</div>
        <div class="card-body p-0">
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>排名</th>
                  <th>短码</th>
                  <th>原始链接</th>
                  <th>总点击</th>
                  <th>今日点击</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="(url, idx) in topUrls" :key="url.shortCode">
                  <td><span class="rank-badge">{{ idx + 1 }}</span></td>
                  <td><code class="code-badge">{{ url.shortCode }}</code></td>
                  <td class="truncate max-w-[300px]">{{ url.longUrl }}</td>
                  <td class="font-semibold text-blue-600">{{ url.totalClicks }}</td>
                  <td>{{ url.todayClicks }}</td>
                  <td>
                    <div class="flex gap-2">
                      <router-link :to="'/stats/' + url.shortCode" class="action-link">详情</router-link>
                      <button @click="copyUrl(url.shortCode)" class="action-link">复制</button>
                    </div>
                  </td>
                </tr>
                <tr v-if="!topUrls.length">
                  <td colspan="6" class="empty-state">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 所有短链列表 -->
      <div class="card">
        <div class="card-header flex items-center justify-between">
          <span>所有短链</span>
          <span class="text-sm text-gray-500">共 {{ totalElements }} 条</span>
        </div>
        <div class="card-body p-0">
          <div class="table-container">
            <table class="data-table">
              <thead>
                <tr>
                  <th>短码</th>
                  <th>原始链接</th>
                  <th>总点击</th>
                  <th>今日点击</th>
                  <th>占比</th>
                  <th>创建时间</th>
                  <th>操作</th>
                </tr>
              </thead>
              <tbody>
                <tr v-for="url in filteredList" :key="url.shortCode">
                  <td><code class="code-badge">{{ url.shortCode }}</code></td>
                  <td class="truncate max-w-[300px]">{{ url.longUrl }}</td>
                  <td class="font-semibold text-blue-600">{{ url.totalVisits ?? '-' }}</td>
                  <td>{{ url.todayVisits === 0 ? '-' : url.todayVisits }}</td>
                  <td>
                    <div class="share-bar">
                      <div class="share-fill" :style="{ width: getSharePercent(url) + '%' }"></div>
                      <span class="share-text">{{ getSharePercent(url).toFixed(1) }}%</span>
                    </div>
                  </td>
                  <td>{{ formatDate(url.createdAt) }}</td>
                  <td>
                    <div class="flex gap-2">
                      <router-link :to="'/stats/' + url.shortCode" class="action-link">详情</router-link>
                      <button @click="copyUrl(url.shortCode)" class="action-link">复制</button>
                      <button @click="deleteUrl(url.shortCode)" class="action-link text-red-500">删除</button>
                    </div>
                  </td>
                </tr>
                <tr v-if="loading">
                  <td colspan="7" class="text-center py-4 text-gray-500">加载中...</td>
                </tr>
                <tr v-if="!loading && !filteredList.length">
                  <td colspan="7" class="empty-state">暂无数据</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
        <!-- 分页 -->
        <div class="card-footer">
          <div class="flex items-center justify-between">
            <span class="text-sm text-gray-500">第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
            <div class="flex gap-2">
              <button class="btn btn-secondary btn-sm" @click="prevPage" :disabled="page <= 1">上一页</button>
              <button class="btn btn-secondary btn-sm" @click="nextPage" :disabled="page >= totalPages">下一页</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, defineAsyncComponent } from 'vue'
import { useI18n } from 'vue-i18n'
import axios from 'axios'
import { API_BASE, SHORT_BASE } from '../composables/shortBase'
import { copyToClipboard } from '../composables/useCopy'

const TrendChart = defineAsyncComponent(() => import('../components/TrendChart.vue'))
const PieDonut = defineAsyncComponent(() => import('../components/charts/PieDonut.vue'))

const { t } = useI18n()

// 状态
const loading = ref(false)
const globalStats = ref(null)
const urlList = ref([])
const topUrls = ref([])
const searchQuery = ref('')
const page = ref(1)
const pageSize = 10
const totalElements = ref(0)
const totalPages = ref(1)
const trendDays = ref(7)

// 分布数据
const deviceDistribution = ref([])
const cityTop10 = ref([])
const sourceTop10 = ref([])
const dailyTrendLabels = ref([])
const dailyTrendValues = ref([])

// 计算属性
const deviceTotal = computed(() => deviceDistribution.value.reduce((a, b) => a + b.count, 0))

// 饼图数据转换
const devicePieData = computed(() => {
  return deviceDistribution.value.map(item => ({
    label: item.key || '未知',
    value: item.count
  }))
})

const filteredList = computed(() => {
  const q = searchQuery.value.trim().toLowerCase()
  const list = urlList.value || []
  if (!q) return list
  return list.filter(u => 
    (u.shortCode || '').toLowerCase().includes(q) || 
    (u.longUrl || '').toLowerCase().includes(q)
  )
})

const totalClicks = computed(() => {
  return (urlList.value || []).reduce((sum, u) => sum + Number(u.totalVisits || 0), 0) || 1
})

// 工具函数
function formatDate(ts) {
  if (!ts) return '-'
  try { return new Date(ts).toLocaleString('zh-CN') } catch { return String(ts) }
}

function getPercent(value, total) {
  return total > 0 ? Math.round(value / total * 100) : 0
}

function getSharePercent(url) {
  return totalClicks.value > 0 ? (Number(url.totalVisits || 0) / totalClicks.value * 100) : 0
}

async function copyUrl(shortCode) {
  const url = `${SHORT_BASE}/${shortCode}`
  try {
    await copyToClipboard(url)
    // 可以添加提示，但需要状态管理
  } catch (e) {
    console.error('复制失败:', e)
  }
}

// API调用
async function fetchGlobalStats() {
  try {
    const now = new Date()
    const start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
    const end = now.toISOString().split('T')[0]
    const res = await axios.get(`${API_BASE}/api/stats/global?start=${start}&end=${end}`)
    globalStats.value = res.data
    
    // 解析分布数据
    deviceDistribution.value = (res.data.deviceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    cityTop10.value = (res.data.cityTop10 || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    sourceTop10.value = (res.data.sourceTop10 || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    topUrls.value = res.data.topUrls || []
    
    // 趋势数据
    const trend = res.data.dailyTrend || []
    dailyTrendLabels.value = trend.map(i => i.key || i.label)
    dailyTrendValues.value = trend.map(i => Number(i.count || i.value || 0))
  } catch (e) { 
    console.error('fetchGlobalStats error:', e) 
  }
}

async function fetchUrlList() {
  loading.value = true
  try {
    const res = await axios.get(`${API_BASE}/api/urls?page=${page.value - 1}&size=${pageSize}`)
    const data = res.data
    urlList.value = Array.isArray(data) ? data : (data.content || data.items || [])
    totalElements.value = data.totalElements || urlList.value.length
    totalPages.value = data.totalPages || Math.ceil(totalElements.value / pageSize) || 1
  } catch (e) {
    console.error('fetchUrlList error:', e)
  } finally {
    loading.value = false
  }
}

async function deleteUrl(shortCode) {
  if (!confirm('确定要删除这个短链吗？')) return
  try {
    await axios.delete(`${API_BASE}/api/${encodeURIComponent(shortCode)}`)
    await refreshAll()
  } catch (e) {
    alert('删除失败')
  }
}

function setTrendDays(days) {
  trendDays.value = days
  fetchGlobalStats()
}

async function refreshAll() {
  await Promise.all([fetchGlobalStats(), fetchUrlList()])
}

function prevPage() {
  if (page.value > 1) {
    page.value--
    fetchUrlList()
  }
}

function nextPage() {
  if (page.value < totalPages.value) {
    page.value++
    fetchUrlList()
  }
}

function exportCSV() {
  const header = ['短码', '原始链接', '总点击', '今日点击', '创建时间']
  const rows = (urlList.value || []).map(u => [
    u.shortCode,
    u.longUrl,
    u.totalVisits ?? '-',
    u.todayVisits ?? '-',
    formatDate(u.createdAt)
  ])
  const csv = [header.join(','), ...rows.map(r => r.map(c => `"${String(c).replace(/"/g, '""')}"`).join(','))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = 'urls.csv'
  a.click()
  URL.revokeObjectURL(url)
}

onMounted(refreshAll)
</script>

<style scoped>
.dashboard-page {
  background: #f8fafc;
  min-height: 100vh;
}

/* 搜索框 */
.search-input {
  padding: 8px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  width: 200px;
  background: white;
}

.search-input:focus {
  outline: none;
  border-color: #2563eb;
}

/* 按钮 */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.btn-sm {
  padding: 6px 12px;
  font-size: 13px;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
}

.btn-secondary {
  background: white;
  color: #334155;
  border: 1px solid #e2e8f0;
}

.btn-secondary:hover {
  background: #f1f5f9;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-tab {
  padding: 6px 12px;
  border-radius: 4px;
  font-size: 13px;
  font-weight: 500;
  background: #f1f5f9;
  color: #64748b;
  border: none;
  cursor: pointer;
}

.btn-tab.active {
  background: #2563eb;
  color: white;
}

/* 卡片 */
.card {
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  padding: 16px 20px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 1px solid #f1f5f9;
}

.card-body {
  padding: 20px;
}

.card-footer {
  padding: 12px 20px;
  border-top: 1px solid #f1f5f9;
  background: #fafbfc;
}

/* 指标卡片 */
.metric-card {
  background: white;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  padding: 16px;
  text-align: center;
}

.metric-label {
  font-size: 12px;
  color: #64748b;
  margin-bottom: 8px;
  text-transform: uppercase;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

/* 图表 */
.chart-container {
  height: 280px;
  background: #fafbfc;
  border: 1px dashed #e2e8f0;
  border-radius: 6px;
}

.chart-placeholder {
  height: 280px;
}

/* 分布列表 */
.distribution-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.dist-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.dist-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dist-name {
  flex: 1;
  font-size: 14px;
  color: #334155;
}

.dist-count {
  font-size: 14px;
  font-weight: 600;
  color: #1e293b;
}

.dist-percent {
  font-size: 13px;
  color: #64748b;
  min-width: 45px;
  text-align: right;
}

.dist-bar {
  height: 6px;
  background: #f1f5f9;
  border-radius: 3px;
  overflow: hidden;
}

.dist-fill {
  height: 100%;
  background: #2563eb;
  border-radius: 3px;
}

/* 排行 */
.rank-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
  border-bottom: 1px solid #f1f5f9;
}

.rank-item:last-child {
  border-bottom: none;
}

.rank-num {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #2563eb;
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 50%;
}

.rank-item:nth-child(n+4) .rank-num {
  background: #94a3b8;
}

.rank-name {
  flex: 1;
  font-size: 14px;
  color: #334155;
}

.rank-count {
  font-size: 14px;
  font-weight: 600;
  color: #2563eb;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: #2563eb;
  color: white;
  font-size: 12px;
  font-weight: 600;
  border-radius: 50%;
}

tr:nth-child(n+4) .rank-badge {
  background: #94a3b8;
}

/* 表格 */
.table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
}

.data-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
}

.data-table td {
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}

.data-table tr:hover td {
  background: #fafbfc;
}

.code-badge {
  font-family: monospace;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  color: #2563eb;
  font-size: 13px;
}

.action-link {
  color: #2563eb;
  font-size: 13px;
  text-decoration: none;
  cursor: pointer;
  background: none;
  border: none;
  padding: 0;
}

.action-link:hover {
  text-decoration: underline;
}

/* 占比条 */
.share-bar {
  position: relative;
  height: 20px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
  min-width: 100px;
}

.share-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: #2563eb;
  border-radius: 4px;
}

.share-text {
  position: relative;
  z-index: 1;
  font-size: 12px;
  color: #334155;
  padding: 0 8px;
  line-height: 20px;
}

.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
