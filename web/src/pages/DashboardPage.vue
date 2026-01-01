<template>
  <div class="min-h-screen dashboard-page pt-24">
    <div class="max-w-7xl mx-auto p-6 space-y-6">
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div class="dashboard-header">
          <h1 class="dashboard-title">数据看板</h1>
          <div class="dashboard-subtitle">实时监控短链接访问数据</div>
        </div>
        <div class="flex items-center gap-3">
          <input v-model="searchQuery" type="text" placeholder="搜索短链..." class="search-input" />
          <button @click="refreshAll" class="action-btn-secondary">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" class="mr-1">
              <path d="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4l-2.35 2.35z"/>
            </svg>
            刷新
          </button>
          <button @click="exportCSV" class="action-btn-secondary">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor" class="mr-1">
              <path d="M19 9h-4V3H9v6H5l7 7 7-7zM5 18v2h14v-2H5z"/>
            </svg>
            导出CSV
          </button>
        </div>
      </div>

      <!-- 数据筛选 -->
      <DataFilter @filterChange="handleFilterChange" />
      <!-- 全局统计概览 -->
      <div class="metrics-grid">
        <div class="metric-card tf-card">
          <div class="metric-icon">🔗</div>
          <div class="metric-content">
            <div class="metric-label">总短链数</div>
            <div class="metric-value">
              <AnimatedNumber :value="globalStats?.totalUrls ?? 0" :duration="1200" />
            </div>
          </div>
          <div class="metric-trend" v-if="globalStats?.totalUrls">
            <span class="trend-icon">📈</span> 系统总计
          </div>
        </div>

        <div class="metric-card tf-card">
          <div class="metric-icon">👁️</div>
          <div class="metric-content">
            <div class="metric-label">总点击量(PV)</div>
            <div class="metric-value">
              <AnimatedNumber :value="globalStats?.totalClicks ?? 0" :duration="1200" />
            </div>
          </div>
          <div class="metric-trend positive" v-if="globalStats?.totalClicks">
            <span class="trend-icon">↗️</span> 累计访问
          </div>
        </div>

        <div class="metric-card tf-card">
          <div class="metric-icon">👥</div>
          <div class="metric-content">
            <div class="metric-label">独立访客(UV)</div>
            <div class="metric-value">
              <AnimatedNumber :value="globalStats?.totalUniqueIps ?? 0" :duration="1200" />
            </div>
          </div>
          <div class="metric-trend" v-if="globalStats?.totalUniqueIps">
            <span class="trend-icon">📍</span> 独立 IP
          </div>
        </div>

        <div class="metric-card tf-card highlight">
          <div class="metric-icon">🔥</div>
          <div class="metric-content">
            <div class="metric-label">今日点击</div>
            <div class="metric-value">
              <AnimatedNumber :value="globalStats?.todayClicks ?? 0" :duration="1200" />
            </div>
          </div>
          <div class="metric-trend positive" v-if="globalStats?.todayClicks">
            <span class="trend-icon">⚡</span> 实时数据
          </div>
        </div>

        <div class="metric-card tf-card">
          <div class="metric-icon">✅</div>
          <div class="metric-content">
            <div class="metric-label">活跃短链</div>
            <div class="metric-value">
              <AnimatedNumber :value="globalStats?.activeUrls ?? 0" :duration="1200" />
            </div>
          </div>
          <div class="metric-trend" v-if="globalStats?.activeUrls">
            <span class="trend-icon">🎯</span> 有访问记录
          </div>
        </div>

        <div class="metric-card tf-card">
          <div class="metric-icon">📊</div>
          <div class="metric-content">
            <div class="metric-label">PV/UV 比率</div>
            <div class="metric-value">
              {{ pvUvRatio.toFixed(2) }}
            </div>
          </div>
          <div class="metric-trend">
            <span class="trend-icon">📐</span> 平均访问深度
          </div>
        </div>
      </div>
      <!-- 图表区域 -->
      <div class="charts-grid">
        <!-- 高级趋势图表 -->
        <div class="chart-section tf-card">
          <div class="chart-header">
            <div class="chart-title">
              <span class="title-icon">📈</span>
              访问趋势分析
            </div>
            <div class="chart-actions">
              <button class="chart-action-btn" @click="toggleDataMode">
                {{ showRealTime ? '历史数据' : '实时数据' }}
              </button>
            </div>
          </div>
          <div class="chart-body">
            <AdvancedTrendChart
              :data="trendChartData"
              :height="320"
              :show-stats="true"
            />
            <div v-if="trendInsight" class="chart-insight-enhanced">
              <span class="insight-icon">💡</span>
              <span class="insight-text">{{ trendInsight }}</span>
            </div>
          </div>
        </div>

        <!-- 设备分布 - 饼图 -->
        <div class="tf-card">
          <div class="tf-card-header tf-font-semibold">设备分布</div>
          <div class="tf-card-body">
            <div class="tf-chart-container">
              <Suspense>
                <G2PieChart
                  v-if="devicePieData.length > 0"
                  :data="devicePieData"
                  :height="280"
                  value-field="value"
                  label-field="label"
                  :colors="['#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe']"
                />
                <template #fallback><div class="tf-chart-placeholder">加载中...</div></template>
              </Suspense>
              <div v-if="devicePieData.length === 0" class="tf-empty-state">
                <div class="tf-empty-icon">📱</div>
                <div class="tf-empty-title">暂无设备数据</div>
                <div class="tf-empty-description">当有访问者点击短链时，此处将显示设备分布</div>
              </div>
              <!-- 设备统计说明 -->
              <div v-if="deviceSummary" class="tf-chart-insight">
                <span class="insight-text">{{ deviceSummary }}</span>
              </div>
            </div>
          </div>
        </div>
        <!-- 设备与OS分布 -->
        <div class="chart-section tf-card">
          <div class="chart-header">
            <div class="chart-title">
              <span class="title-icon">💻</span>
              设备与系统分布
            </div>
          </div>
          <div class="chart-body">
            <BrowserOSChart :data="allEvents" />
            <div class="distribution-insight" v-if="deviceSummary">
              <span class="insight-icon">📱</span>
              <span class="insight-text">{{ deviceSummary }}</span>
            </div>
          </div>
        </div>

        <!-- 地理分布 -->
        <div class="chart-section tf-card">
          <div class="chart-header">
            <div class="chart-title">
              <span class="title-icon">🌍</span>
              地理分布分析
            </div>
          </div>
          <div class="chart-body">
            <div class="geo-grid">
              <div class="geo-section">
                <div class="sub-title">城市 TOP 10</div>
                <div class="geo-chart-container">
                  <Suspense>
                    <G2HBarChart
                      v-if="cityChartData.length > 0"
                      :data="cityChartData"
                      :height="120"
                      x-field="count"
                      y-field="name"
                    />
                    <template #fallback><div class="chart-placeholder">加载中...</div></template>
                  </Suspense>
                </div>
              </div>
              <div class="geo-section">
                <div class="sub-title">来源域名 TOP 10</div>
                <div class="geo-chart-container">
                  <Suspense>
                    <G2HBarChart
                      v-if="sourceChartData.length > 0"
                      :data="sourceChartData"
                      :height="120"
                      x-field="count"
                      y-field="name"
                    />
                    <template #fallback><div class="chart-placeholder">加载中...</div></template>
                  </Suspense>
                </div>
              </div>
            </div>
            <div v-if="geoInsight" class="chart-insight-enhanced">
              <span class="insight-icon">📍</span>
              <span class="insight-text">{{ geoInsight }}</span>
            </div>
          </div>
        </div>

        <!-- 实时数据 -->
        <div class="chart-section tf-card" v-if="showRealTime">
          <div class="chart-header">
            <div class="chart-title">
              <span class="title-icon">⚡</span>
              实时访问监控
            </div>
            <div class="real-time-indicator">
              <span class="indicator-dot"></span>
              实时更新
            </div>
          </div>
          <div class="chart-body">
            <div class="real-time-stats">
              <div class="real-time-item">
                <div class="rt-label">当前在线</div>
                <div class="rt-value">{{ realTimeStats.onlineUsers }}</div>
              </div>
              <div class="real-time-item">
                <div class="rt-label">近5分钟PV</div>
                <div class="rt-value">{{ realTimeStats.last5MinPV }}</div>
              </div>
              <div class="real-time-item">
                <div class="rt-label">服务器状态</div>
                <div class="rt-status" :class="realTimeStats.serverStatus">
                  {{ realTimeStats.serverStatus === 'normal' ? '正常' : '异常' }}
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 热门短链排行 -->
      <div class="tf-card tf-mb-6">
        <div class="tf-card-header tf-font-semibold">热门短链 TOP 10</div>
        <div class="tf-card-body">
          <div class="tf-hoturl-list">
            <div v-for="(url, idx) in topUrls" :key="url.shortCode" class="tf-hoturl-item">
              <!-- 排名徽章 -->
              <span class="tf-rank-badge" :class="{ 'tf-rank-top3': idx < 3 }">{{ idx + 1 }}</span>

              <!-- 短链信息 -->
              <div class="tf-hoturl-info">
                <div class="tf-hoturl-header">
                  <a :href="`${SHORT_BASE}/${url.shortCode}`" target="_blank" class="tf-hoturl-code">
                    {{ url.shortCode }}
                  </a>
                  <span class="tf-hoturl-clicks">{{ url.totalClicks }} 次</span>
                </div>
                <div class="tf-hoturl-url">{{ url.longUrl }}</div>

                <!-- 可视化条形图 -->
                <div class="tf-hoturl-bar">
                  <div class="tf-hoturl-bar-bg">
                    <div class="tf-hoturl-bar-fill" :style="{ width: getHotUrlPercent(url.totalClicks, topUrls) + '%' }"></div>
                  </div>
                  <div class="tf-hoturl-stats">
                    <span class="tf-stat-item">今日: {{ url.todayClicks || 0 }}</span>
                    <span class="tf-stat-divider">|</span>
                    <router-link :to="'/stats/' + url.shortCode" class="tf-stat-link">详情</router-link>
                    <button @click="copyUrl(url.shortCode)" class="tf-stat-link">
                      {{ copyingCode === url.shortCode ? '已复制' : '复制' }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <div v-if="!topUrls.length" class="tf-empty-state">
              <div class="tf-empty-icon">🔥</div>
              <div class="tf-empty-title">暂无热门短链</div>
              <div class="tf-empty-description">创建短链并获得访问后，此处将显示热门排行</div>
            </div>
          </div>
        </div>
      </div>
      <!-- 所有短链列表 -->
      <div class="tf-card">
        <div class="tf-card-header tf-flex tf-justify-between tf-items-center">
          <span class="tf-font-semibold">所有短链</span>
          <span class="tf-text-sm tf-text-muted">共 {{ totalElements }} 条</span>
        </div>
        <div class="tf-table-container">
          <table class="tf-table">
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
                <td>
                  <a :href="`${SHORT_BASE}/${url.shortCode}`" target="_blank" class="tf-code-badge">
                    {{ url.shortCode }}
                  </a>
                </td>
                <td class="tf-truncate">{{ url.longUrl }}</td>
                <td class="tf-font-semibold" style="color: var(--tf-brand-primary);">{{ url.totalVisits ?? '-' }}</td>
                <td>{{ url.todayVisits === 0 ? '-' : url.todayVisits }}</td>
                <td>
                  <div class="tf-share-bar">
                    <div class="tf-share-fill" :style="{ width: getSharePercent(url) + '%' }"></div>
                    <span class="tf-share-text">{{ getSharePercent(url).toFixed(1) }}%</span>
                  </div>
                </td>
                <td>{{ formatDate(url.createdAt) }}</td>
                <td>
                  <div class="tf-flex tf-gap-2">
                    <router-link :to="'/stats/' + url.shortCode" class="tf-action-link">详情</router-link>
                    <button @click="copyUrl(url.shortCode)" class="tf-action-link">
                      {{ copyingCode === url.shortCode ? '已复制' : '复制' }}
                    </button>
                    <button @click="deleteUrl(url.shortCode)" class="tf-action-link" style="color: var(--tf-gray-500);">删除</button>
                  </div>
                </td>
              </tr>
              <tr v-if="loading">
                <td colspan="7" class="tf-text-center tf-py-4 tf-text-muted">
                  <div class="tf-loading">加载中...</div>
                </td>
              </tr>
              <tr v-if="!loading && !filteredList.length">
                <td colspan="7" class="tf-empty-state tf-text-center">
                  <div class="tf-empty-icon">📄</div>
                  <div class="tf-empty-title">暂无数据</div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <!-- 分页 -->
        <div class="tf-card-footer tf-flex tf-justify-between tf-items-center">
          <span class="tf-text-sm tf-text-muted">第 {{ page }} 页 / 共 {{ totalPages }} 页</span>
          <div class="tf-flex tf-gap-2">
            <button class="action-btn-secondary tf-btn-sm" @click="prevPage" :disabled="page <= 1">上一页</button>
            <button class="action-btn-secondary tf-btn-sm" @click="nextPage" :disabled="page >= totalPages">下一页</button>
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

// 新导入的组件
import DataFilter from '../components/data/DataFilter.vue'
import AdvancedTrendChart from '../components/charts/AdvancedTrendChart.vue'
import BrowserOSChart from '../components/charts/BrowserOSChart.vue'

// 保持原有组件导入
const G2LineChart = defineAsyncComponent(() => import('../components/charts/G2LineChart.vue'))
const G2PieChart = defineAsyncComponent(() => import('../components/charts/G2PieChart.vue'))
const G2BarChart = defineAsyncComponent(() => import('../components/charts/G2BarChart.vue'))
const G2HBarChart = defineAsyncComponent(() => import('../components/charts/G2HBarChart.vue'))
const G2HeatmapChart = defineAsyncComponent(() => import('../components/charts/G2HeatmapChart.vue'))
const AnimatedNumber = defineAsyncComponent(() => import('../components/AnimatedNumber.vue'))

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
const showRealTime = ref(false)
const allEvents = ref([])
const realTimeStats = ref({
  onlineUsers: 0,
  last5MinPV: 0,
  serverStatus: 'normal'
})

// 筛选状态
const activeFilters = ref({})

// 分布数据
const deviceDistribution = ref([])
const cityTop10 = ref([])
const sourceTop10 = ref([])
const dailyTrendLabels = ref([])
const dailyTrendValues = ref([])

// PV/UV 比率计算
const pvUvRatio = computed(() => {
  const pv = globalStats.value?.totalClicks || 0
  const uv = globalStats.value?.totalUniqueIps || 1
  return pv / uv
})

// 地理分布洞察
const geoInsight = computed(() => {
  const cities = cityTop10.value
  const sources = sourceTop10.value

  if (!cities.length && !sources.length) return ''

  let insight = ''

  if (cities.length > 0) {
    const topCity = cities[0]
    insight += `主要访问来自 ${topCity.key}，占比 ${Math.round(topCity.count / deviceTotal.value * 100)}%`
  }

  if (sources.length > 0) {
    const topSource = sources[0]
    insight += `，主要来源为 ${topSource.key}`
  }

  return insight
})

// 计算属性
const deviceTotal = computed(() => deviceDistribution.value.reduce((a, b) => a + b.count, 0))

// 饼图数据转换
const devicePieData = computed(() => {
  return deviceDistribution.value.map(item => ({
    label: item.key || '未知',
    value: item.count
  }))
})

// G2 折线图数据转换
const trendChartData = computed(() => {
  return dailyTrendLabels.value.map((date, index) => ({
    date,
    value: dailyTrendValues.value[index] || 0
  }))
})

// G2 横向柱状图数据转换 - 城市
const cityChartData = computed(() => {
  return cityTop10.value.slice(0, 10).map(item => ({
    name: item.key || '未知',
    count: item.count
  }))
})

// G2 横向柱状图数据转换 - 来源
const sourceChartData = computed(() => {
  return sourceTop10.value.slice(0, 10).map(item => ({
    name: item.key || '直接访问',
    count: item.count
  }))
})

// 趋势洞察
const trendInsight = computed(() => {
  const values = dailyTrendValues.value
  if (!values || values.length < 2) return ''

  const total = values.reduce((a, b) => a + b, 0)
  const avg = Math.round(total / values.length)
  const lastValue = values[values.length - 1]
  const prevValue = values[values.length - 2]

  if (lastValue > prevValue) {
    const growth = Math.round((lastValue - prevValue) / Math.max(prevValue, 1) * 100)
    return `近${trendDays.value}天平均每天 ${avg} 次访问，最近一天增长 ${growth}%`
  } else if (lastValue < prevValue) {
    return `近${trendDays.value}天平均每天 ${avg} 次访问，最近一天略有下降`
  } else {
    return `近${trendDays.value}天平均每天 ${avg} 次访问，访问量保持稳定`
  }
})

// 设备分布总结
const deviceSummary = computed(() => {
  const devices = deviceDistribution.value
  if (!devices || devices.length === 0) return ''

  const total = devices.reduce((a, b) => a + b.count, 0)
  const topDevice = devices.reduce((max, item) => item.count > max.count ? item : max, devices[0])
  const percent = Math.round(topDevice.count / total * 100)

  const deviceName = {
    'desktop': '桌面设备',
    'mobile': '移动设备',
    'tablet': '平板设备'
  }[topDevice.key] || topDevice.key

  return `${deviceName}访问占比最高，达 ${percent}%`
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

// 新增方法
function handleFilterChange(filters) {
  activeFilters.value = filters
  // 应用筛选条件重新获取数据
  fetchDataWithFilters()
}

function fetchDataWithFilters() {
  // 根据筛选条件重新获取数据
  refreshAll()
}

function toggleDataMode() {
  showRealTime.value = !showRealTime.value
  if (showRealTime.value) {
    startRealTimeMonitoring()
  } else {
    stopRealTimeMonitoring()
  }
}

function startRealTimeMonitoring() {
  // 启动实时监控
  // 这里可以集成WebSocket或轮询
}

function stopRealTimeMonitoring() {
  // 停止实时监控
}

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

// 计算条形图宽度
function getBarWidth(count, dataArray) {
  if (!dataArray || dataArray.length === 0) return 0
  const maxCount = Math.max(...dataArray.map(item => item.count))
  return maxCount > 0 ? (count / maxCount * 100) : 0
}

// 计算热门短链百分比
function getHotUrlPercent(clicks, urlArray) {
  if (!urlArray || urlArray.length === 0) return 0
  const maxClicks = Math.max(...urlArray.map(u => u.totalClicks || 0))
  return maxClicks > 0 ? (clicks / maxClicks * 100) : 0
}

// 复制状态
const copyingCode = ref(null)

async function copyUrl(shortCode) {
  const url = `${SHORT_BASE}/${shortCode}`
  try {
    copyingCode.value = shortCode
    await copyToClipboard(url)
    // 显示复制成功提示
    setTimeout(() => {
      copyingCode.value = null
    }, 2000)
  } catch (e) {
    console.error('复制失败:', e)
    alert('复制失败，请手动复制')
    copyingCode.value = null
  }
}

// API调用
async function fetchGlobalStats() {
  try {
    const now = new Date()
    const start = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0]
    const end = now.toISOString().split('T')[0]

    // 应用筛选条件
    const params = new URLSearchParams()
    params.set('start', start)
    params.set('end', end)

    if (activeFilters.value.startDate) params.set('start', activeFilters.value.startDate)
    if (activeFilters.value.endDate) params.set('end', activeFilters.value.endDate)
    if (activeFilters.value.devices?.length) params.set('devices', activeFilters.value.devices.join(','))
    if (activeFilters.value.os) params.set('os', activeFilters.value.os)
    if (activeFilters.value.browser) params.set('browser', activeFilters.value.browser)
    if (activeFilters.value.region) params.set('region', activeFilters.value.region)

    const res = await axios.get(`${API_BASE}/api/stats/global?${params}`)
    globalStats.value = res.data

    // 解析分布数据
    deviceDistribution.value = (res.data.deviceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    cityTop10.value = (res.data.cityTop10 || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    sourceTop10.value = (res.data.sourceTop10 || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    topUrls.value = res.data.topUrls || []

    // 获取所有事件数据用于浏览器OS分析
    if (res.data.recentEvents) {
      allEvents.value = res.data.recentEvents
    }

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
    console.log('fetchUrlList response:', data) // 调试信息
    urlList.value = Array.isArray(data) ? data : (data.content || data.items || [])
    totalElements.value = data.totalElements || urlList.value.length
    totalPages.value = data.totalPages || Math.ceil(totalElements.value / pageSize) || 1
    console.log('urlList.value:', urlList.value) // 调试信息
    console.log('totalElements:', totalElements.value)
  } catch (e) {
    console.error('fetchUrlList error:', e)
    urlList.value = []
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
@import '../style/dashboard-optimized.css';
/* 热门短链列表样式 */
.tf-hoturl-list {
  display: flex;
  flex-direction: column;
  gap: var(--tf-space-4);
}

.tf-hoturl-item {
  display: flex;
  gap: var(--tf-space-3);
  padding: var(--tf-space-4);
  background: var(--tf-gray-50);
  border: 1px solid var(--tf-border-light);
  border-radius: var(--tf-radius-md);
  transition: all var(--tf-duration-normal) var(--tf-ease-in-out);
}

.tf-hoturl-item:hover {
  border-color: var(--tf-gray-500);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  transform: translateY(-1px);
}

.tf-rank-badge {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--tf-gray-400);
  color: white;
  font-size: var(--tf-text-sm);
  font-weight: var(--tf-font-semibold);
  border-radius: var(--tf-radius-full);
  flex-shrink: 0;
}

.tf-rank-badge.tf-rank-top3 {
  background: var(--tf-gray-600);
}

.tf-hoturl-info {
  flex: 1;
  min-width: 0;
}

.tf-hoturl-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: var(--tf-space-3);
  margin-bottom: var(--tf-space-2);
}

.tf-hoturl-code {
  font-size: var(--tf-text-base);
  font-weight: var(--tf-font-semibold);
  color: var(--tf-gray-700);
  text-decoration: none;
  transition: color var(--tf-duration-fast) var(--tf-ease-in-out);
}

.tf-hoturl-code:hover {
  color: var(--tf-gray-900);
  text-decoration: underline;
}

.tf-hoturl-clicks {
  font-size: var(--tf-text-base);
  font-weight: var(--tf-font-semibold);
  color: var(--tf-gray-800);
}

.tf-hoturl-url {
  font-size: var(--tf-text-sm);
  color: var(--tf-gray-600);
  margin-bottom: var(--tf-space-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tf-hoturl-bar {
  display: flex;
  flex-direction: column;
  gap: var(--tf-space-2);
}

.tf-hoturl-bar-bg {
  height: 8px;
  background: var(--tf-gray-200);
  border-radius: var(--tf-radius-full);
  overflow: hidden;
  position: relative;
}

.tf-hoturl-bar-fill {
  height: 100%;
  background: var(--tf-gray-600);
  border-radius: var(--tf-radius-full);
  transition: width var(--tf-duration-slow) var(--tf-ease-out);
}

.tf-hoturl-stats {
  display: flex;
  align-items: center;
  gap: var(--tf-space-2);
  font-size: var(--tf-text-xs);
}

.tf-stat-item {
  color: var(--tf-gray-600);
}

.tf-stat-divider {
  color: var(--tf-gray-300);
}

.tf-stat-link {
  color: var(--tf-gray-600);
  text-decoration: none;
  cursor: pointer;
  border: none;
  background: none;
  padding: 0;
  font-size: var(--tf-text-xs);
  transition: color var(--tf-duration-fast) var(--tf-ease-in-out);
}

.tf-stat-link:hover {
  color: var(--tf-gray-800);
  text-decoration: underline;
}

/* 表格相关样式 */
.tf-code-badge {
  font-family: monospace;
  background: var(--tf-gray-100);
  padding: var(--tf-space-1) var(--tf-space-2);
  border-radius: var(--tf-radius-sm);
  color: var(--tf-gray-700);
  font-size: var(--tf-text-xs);
  text-decoration: none;
  transition: all var(--tf-duration-fast) var(--tf-ease-in-out);
}

.tf-code-badge:hover {
  background: var(--tf-gray-200);
  color: var(--tf-gray-900);
  text-decoration: underline;
}

.tf-share-bar {
  position: relative;
  height: 20px;
  background: var(--tf-gray-100);
  border-radius: var(--tf-radius-sm);
  overflow: hidden;
  min-width: 100px;
}

.tf-share-fill {
  position: absolute;
  left: 0;
  top: 0;
  height: 100%;
  background: var(--tf-gray-600);
  border-radius: var(--tf-radius-sm);
  transition: width var(--tf-duration-slow) var(--tf-ease-out);
}

.tf-share-text {
  position: relative;
  z-index: 1;
  font-size: var(--tf-text-xs);
  color: var(--tf-gray-800);
  padding: 0 var(--tf-space-2);
  line-height: 20px;
  font-weight: var(--tf-font-semibold);
}

/* 单色按钮样式 */
.action-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--tf-space-3) var(--tf-space-4);
  border: 1px solid var(--tf-gray-500);
  border-radius: var(--tf-radius);
  background: white;
  color: var(--tf-gray-700);
  font-size: var(--tf-text-sm);
  font-weight: var(--tf-font-medium);
  cursor: pointer;
  transition: all var(--tf-duration-normal) var(--tf-ease-in-out);
  text-decoration: none;
}

.action-btn:hover:not(:disabled) {
  background: var(--tf-gray-100);
  border-color: var(--tf-gray-600);
  color: var(--tf-gray-800);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn-secondary {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: var(--tf-space-2) var(--tf-space-3);
  border: 1px solid var(--tf-gray-300);
  border-radius: var(--tf-radius);
  background: white;
  color: var(--tf-gray-600);
  font-size: var(--tf-text-sm);
  font-weight: var(--tf-font-medium);
  cursor: pointer;
  transition: all var(--tf-duration-normal) var(--tf-ease-in-out);
}

.action-btn-secondary:hover:not(:disabled) {
  background: var(--tf-gray-100);
  border-color: var(--tf-gray-400);
  color: var(--tf-gray-700);
}

.action-btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-btn-sm {
  padding: var(--tf-space-1) var(--tf-space-2);
  font-size: var(--tf-text-xs);
}

.tf-action-link {
  color: var(--tf-gray-600);
  font-size: var(--tf-text-xs);
  text-decoration: none;
  cursor: pointer;
  background: none;
  border: none;
  padding: 0;
  transition: color var(--tf-duration-fast) var(--tf-ease-in-out);
}

.tf-action-link:hover {
  color: var(--tf-gray-800);
  text-decoration: underline;
}

/* 兼容性保留样式，可逐渐移除 */
.dashboard-page {
  background: var(--tf-gray-50);
  min-height: 100vh;
}
</style>