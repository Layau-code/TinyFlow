<template>
  <div class="min-h-screen stats-page pt-24 md:pt-28" :class="{ 'tf-card-root': useDarkTheme }">
    <div class="max-w-7xl mx-auto p-6 space-y-6">
      <!-- 顶部操作栏 -->
      <!-- DEMO 横幅（仅 demo 模式显示） -->
      <div v-if="isDemo" class="demo-banner">
        正在演示模式 (DEMO)：页面使用本地示例数据。如需真实数据，请关闭 demo 参数或在后端提供数据。
      </div>
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div class="flex items-center gap-3">
          <button @click="goHome" class="btn btn-secondary">返回首页</button>
          <button @click="goDashboard" class="btn btn-secondary">数据看板</button>
        </div>
        <div class="flex items-center gap-3">
          <button @click="copy(shortUrl)" class="btn btn-secondary">
            {{ copying ? '已复制' : '复制短链' }}
          </button>
          <button @click="openFilter" class="btn btn-primary">筛选</button>
          <button @click="exportCsv" class="btn btn-secondary">导出CSV</button>
          <button @click="exportPng" class="btn btn-secondary">导出PNG</button>
          <button @click="refreshAll" class="btn btn-secondary">刷新</button>
          <label class="flex items-center gap-2 text-sm">
            <input type="checkbox" v-model="realtime" /> 实时
          </label>
        </div>
      </div>

      <!-- 核心指标卡片 -->
      <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
        <div class="metric-card">
          <div class="metric-label">总PV</div>
          <div class="metric-value">
            <AnimatedNumber v-if="totalPvNum" :value="totalPvNum" :duration="1000" />
            <span v-else>-</span>
          </div>
        </div>
        <div class="metric-card">
          <div class="metric-label">总UV</div>
          <div class="metric-value">
            <AnimatedNumber v-if="totalUvNum" :value="totalUvNum" :duration="1000" />
            <span v-else>-</span>
          </div>
        </div>
        <div class="metric-card">
          <div class="metric-label">今日PV</div>
          <div class="metric-value">
            <AnimatedNumber v-if="todayPvNum" :value="todayPvNum" :duration="1000" />
            <span v-else>-</span>
          </div>
        </div>
        <div class="metric-card">
          <div class="metric-label">PV/UV比</div>
          <div class="metric-value">{{ detailedStats?.pvUvRatio ? detailedStats.pvUvRatio.toFixed(2) : '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">日均PV</div>
          <div class="metric-value">{{ avgVisits }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">峰值PV</div>
          <div class="metric-value">{{ peakVisits }}</div>
        </div>
      </div>

      <!-- 短链信息卡片 -->
      <div :class="['card', useDarkTheme ? 'tf-card' : '']">
        <div class="card-header">短链信息</div>
        <div class="card-body">
          <div class="info-grid">
            <div class="info-item">
              <span class="info-label">短码</span>
              <code class="info-code">{{ shortCode }}</code>
            </div>
            <div class="info-item">
              <span class="info-label">短链地址</span>
              <a :href="shortUrl" target="_blank" class="info-link">{{ shortUrl }}</a>
            </div>
            <div class="info-item">
              <span class="info-label">创建时间</span>
              <span class="info-value">{{ formatDate(overview?.createdAt) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">首次访问</span>
              <span class="info-value">{{ formatDate(detailedStats?.firstClick) }}</span>
            </div>
            <div class="info-item">
              <span class="info-label">最后访问</span>
              <span class="info-value">{{ formatDate(detailedStats?.lastClick) }}</span>
            </div>
          </div>
          <div class="info-item mt-4">
            <span class="info-label">原始链接</span>
            <div class="info-url">{{ overview?.longUrl || '-' }}</div>
          </div>
        </div>
      </div>

      <!-- 趋势图表 -->
      <div :class="['card', useDarkTheme ? 'tf-card' : '']">
        <div class="card-header flex items-center justify-between">
          <span>访问趋势</span>
          <div class="flex gap-2">
            <button class="btn-tab" :class="{ active: selectedDays === 7 }" @click="selectDays(7)">7天</button>
            <button class="btn-tab" :class="{ active: selectedDays === 14 }" @click="selectDays(14)">14天</button>
            <button class="btn-tab" :class="{ active: selectedDays === 30 }" @click="selectDays(30)">30天</button>
          </div>
        </div>
        <div class="card-body">
          <div class="chart-container-enhanced">
            <AsyncChartLoader :loader="TrendChartLoader" :passThroughProps="{ labels: trendLabels, values: trendValues, height: 280 }" ref="trendChartRef" @point-click="onTrendPointClick">
              <template #fallback>
                <SkeletonLoader variant="card" :height="280" />
              </template>
            </AsyncChartLoader>
            <div v-if="!loadingTrend && trendLabels.length === 0" class="empty-state">暂无趋势数据</div>
            <div v-if="loadingTrend" class="mt-4"><SkeletonLoader :height="120" /></div>
            <!-- 趋势洞察 -->
            <div v-if="trendInsight" class="chart-insight">
              <span class="insight-text">{{ trendInsight }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 分布图表网格 -->
      <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
        <!-- 小时分布 -->
        <div class="card">
          <div class="card-header">24小时分布</div>
          <div class="card-body">
            <div class="bar-chart">
              <div v-for="item in hourDistribution" :key="item.key" class="bar-item">
                <div class="bar-label">{{ item.key }}</div>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: getBarWidth(item.count, hourMax) + '%' }"></div>
                </div>
                <div class="bar-value">{{ item.count }}</div>
              </div>
              <div v-if="!hourDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- 星期分布 -->
        <div class="card">
          <div class="card-header">星期分布</div>
          <div class="card-body">
            <div class="bar-chart">
              <div v-for="item in weekdayDistribution" :key="item.key" class="bar-item">
                <div class="bar-label">{{ item.key }}</div>
                <div class="bar-track">
                  <div class="bar-fill" :style="{ width: getBarWidth(item.count, weekdayMax) + '%' }"></div>
                </div>
                <div class="bar-value">{{ item.count }}</div>
              </div>
              <div v-if="!weekdayDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- 设备分布 -->
        <div class="card">
          <div class="card-header">设备分布</div>
          <div class="card-body">
            <div class="distribution-list">
              <div v-for="item in deviceDistribution" :key="item.key" class="dist-item">
                <div class="dist-info">
                  <span class="dist-name">{{ item.key || '未知' }}</span>
                  <span class="dist-count">{{ item.count }}</span>
                  <span class="dist-percent">{{ getPercent(item.count, deviceTotal) }}%</span>
                </div>
                <div class="dist-bar">
                  <div class="dist-fill" :style="{ width: getPercent(item.count, deviceTotal) + '%' }"></div>
                </div>
              </div>
              <div v-if="!deviceDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- 浏览器分布 -->
        <div class="card">
          <div class="card-header">浏览器分布</div>
          <div class="card-body">
            <div class="distribution-list">
              <div v-for="item in browserDistribution" :key="item.key" class="dist-item">
                <div class="dist-info">
                  <span class="dist-name">{{ item.key || '未知' }}</span>
                  <span class="dist-count">{{ item.count }}</span>
                  <span class="dist-percent">{{ getPercent(item.count, browserTotal) }}%</span>
                </div>
                <div class="dist-bar">
                  <div class="dist-fill" :style="{ width: getPercent(item.count, browserTotal) + '%' }"></div>
                </div>
              </div>
              <div v-if="!browserDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- 城市TOP10 (按需 ECharts 柱状图) -->
        <div class="card">
          <div class="card-header">城市 TOP 10</div>
          <div class="card-body">
            <div class="chart-placeholder">
              <AsyncChartLoader :loader="CityBarLoader" :passThroughProps="{ labels: cityDistribution.slice(0,10).map(i => i.key), values: cityDistribution.slice(0,10).map(i => i.count), height: 260 }" @point-click="(p) => drillCity(p.label)" />
            </div>
            <div v-if="!cityDistribution.length" class="empty-state">暂无数据</div>
          </div>
        </div>

        <!-- 国家分布 (按需 ECharts 饼图) -->
        <div class="card">
          <div class="card-header">国家/地区分布</div>
          <div class="card-body">
            <AsyncChartLoader :loader="CountryPieLoader" :passThroughProps="{ data: countryDistribution.slice(0,10).map(i => ({ label: i.key, name: i.key, value: i.count })), height: 260 }" @point-click="(p) => { const name = p?.name || p?.data?.name; if (name) drillCity(name) }" />
            <div v-if="!countryDistribution.length" class="empty-state">暂无数据</div>
          </div>
        </div>

        <!-- 来源域名 -->
        <div class="card">
          <div class="card-header">来源域名 TOP 10</div>
          <div class="card-body">
            <div class="rank-list">
              <div v-for="(item, idx) in sourceDistribution.slice(0, 10)" :key="item.key" class="rank-item">
                <span class="rank-num">{{ idx + 1 }}</span>
                <span class="rank-name truncate">{{ item.key || '直接访问' }}</span>
                <span class="rank-count">{{ item.count }}</span>
              </div>
              <div v-if="!sourceDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- Referer详情 -->
        <div class="card">
          <div class="card-header">Referer 详情</div>
          <div class="card-body">
            <div class="referer-list">
              <div v-for="item in refererDistribution.slice(0, 10)" :key="item.key" class="referer-item">
                <span class="referer-url truncate">{{ item.key || '直接访问' }}</span>
                <span class="referer-count">{{ item.count }}</span>
              </div>
              <div v-if="!refererDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>
      </div>

      <!-- 访问事件列表 -->
      <div class="card">
        <div class="card-header flex items-center justify-between">
          <span>最近访问记录</span>
          <span class="text-sm text-gray-500">共 {{ eventsList.length }} 条</span>
        </div>
        <div class="card-body p-0">
          <div class="events-table-container">
            <table class="events-table">
              <thead>
                <tr>
                  <th>时间</th>
                  <th>IP</th>
                  <th>设备</th>
                  <th>城市</th>
                  <th>国家</th>
                  <th>来源</th>
                </tr>
              </thead>
              <tbody>
                <tr v-if="loadingEvents">
                  <td colspan="6"><SkeletonLoader :height="120" /></td>
                </tr>
                <tr v-else v-for="(event, idx) in eventsList.slice(0, 50)" :key="idx">
                  <td>{{ formatDate(event.ts) }}</td>
                  <td>{{ event.ip || '-' }}</td>
                  <td><span class="device-tag">{{ event.deviceType || '-' }}</span></td>
                  <td>{{ event.city || '-' }}</td>
                  <td>{{ event.country || '-' }}</td>
                  <td class="truncate max-w-[200px]">{{ event.sourceHost || '-' }}</td>
                </tr>
                <tr v-if="!loadingEvents && !eventsList.length">
                  <td colspan="6" class="empty-state">暂无访问记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

      <!-- 筛选弹窗 -->
      <div v-if="showFilter" class="modal-backdrop" @click.self="closeFilter">
        <div class="modal">
          <div class="modal-header">筛选条件</div>
          <div class="modal-body">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div class="form-group">
                <label>开始日期</label>
                <input v-model="filterStart" type="date" class="form-input" />
              </div>
              <div class="form-group">
                <label>结束日期</label>
                <input v-model="filterEnd" type="date" class="form-input" />
              </div>
              <div class="form-group">
                <label>来源</label>
                <input v-model="filterSource" type="text" placeholder="不填为全部" class="form-input" />
              </div>
              <div class="form-group">
                <label>设备</label>
                <select v-model="filterDevice" class="form-input">
                  <option value="">全部</option>
                  <option value="mobile">移动</option>
                  <option value="desktop">桌面</option>
                  <option value="tablet">平板</option>
                </select>
              </div>
              <div class="form-group">
                <label>城市</label>
                <input v-model="filterCity" type="text" placeholder="不填为全部" class="form-input" />
              </div>
            </div>
          </div>
          <div class="modal-footer">
            <button class="btn btn-secondary" @click="resetFilters">重置</button>
            <button class="btn btn-primary" @click="applyFilters">应用</button>
            <button class="btn btn-secondary" @click="closeFilter">取消</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, defineAsyncComponent, watch, onBeforeUnmount } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { SHORT_BASE, API_BASE } from '../composables/shortBase'
import { copyToClipboard } from '../composables/useCopy'
import { subscribeRealtime } from '../composables/useRealtime'
import axios from 'axios'
import demoData from '../mock/demoData'
import AsyncChartLoader from '../components/AsyncChartLoader.vue'
const TrendChartLoader = () => import('../components/charts/TrendEChartsLine.vue')
const AnimatedNumber = defineAsyncComponent(() => import('../components/AnimatedNumber.vue'))
const SkeletonLoader = defineAsyncComponent(() => import('../components/SkeletonLoader.vue'))
const CityBarLoader = () => import('../components/charts/EChartsBar.vue')
const CountryPieLoader = () => import('../components/charts/EChartsPie.vue')
import '../style/charts-theme.css'
const useDarkTheme = true // 只在统计页启用企业冷静风主题

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const routeShortCode = route.params.shortCode
const isDemo = route.query && (route.query.demo === '1' || route.query.demo === 'true')
// 当处于 demo 模式并且没有在路由中提供 shortCode 时，使用 demo 的短码
const shortCode = isDemo ? (routeShortCode || 'demo123') : routeShortCode
const shortUrl = `${SHORT_BASE}/${encodeURIComponent(shortCode)}`

// 数据状态
const overview = ref(null)
const detailedStats = ref(null)
const eventsList = ref([])
const trendLabels = ref([])
const trendValues = ref([])
const selectedDays = ref(7)


// loading states
const loadingOverview = ref(false)
const loadingDetailed = ref(false)
const loadingTrend = ref(false)
const loadingEvents = ref(false)

// 分布数据
const hourDistribution = ref([])
const weekdayDistribution = ref([])
const deviceDistribution = ref([])
const browserDistribution = ref([])
const cityDistribution = ref([])
const countryDistribution = ref([])
const sourceDistribution = ref([])
const refererDistribution = ref([])

// 筛选条件
const showFilter = ref(false)
const filterStart = ref('')
const filterEnd = ref('')
const filterSource = ref('')
const filterDevice = ref('')
const filterCity = ref('')

// 实时开关
const realtime = ref(false)

// 计算属性
const peakVisits = computed(() => trendValues.value.length ? Math.max(...trendValues.value) : 0)
const avgVisits = computed(() => {
  if (!trendValues.value.length) return 0
  return Math.round(trendValues.value.reduce((a, b) => a + b, 0) / trendValues.value.length)
})

const hourMax = computed(() => Math.max(...hourDistribution.value.map(i => i.count), 1))
const weekdayMax = computed(() => Math.max(...weekdayDistribution.value.map(i => i.count), 1))
const deviceTotal = computed(() => deviceDistribution.value.reduce((a, b) => a + b.count, 0))
const browserTotal = computed(() => browserDistribution.value.reduce((a, b) => a + b.count, 0))

// 趋势洞察
const trendInsight = computed(() => {
  const values = trendValues.value
  if (!values || values.length < 2) return ''
  
  const total = values.reduce((a, b) => a + b, 0)
  const avg = Math.round(total / values.length)
  const lastValue = values[values.length - 1]
  const prevValue = values[values.length - 2]
  
  if (lastValue > prevValue) {
    const growth = Math.round((lastValue - prevValue) / Math.max(prevValue, 1) * 100)
    return `近${selectedDays.value}天平均每天 ${avg} 次访问，最近一天增长 ${growth}%`
  } else if (lastValue < prevValue) {
    return `近${selectedDays.value}天平均每天 ${avg} 次访问，最近一天略有下降`
  } else {
    return `近${selectedDays.value}天平均每天 ${avg} 次访问，访问量保持稳定`
  }
})

// 工具函数
function formatDate(ts) {
  if (!ts) return '-'
  try { return new Date(ts).toLocaleString('zh-CN') } catch { return String(ts) }
}

function getBarWidth(value, max) {
  return max > 0 ? (value / max * 100) : 0
}

function getPercent(value, total) {
  return total > 0 ? Math.round(value / total * 100) : 0
}

// 复制状态
const copying = ref(false)

async function copy(text) {
  try {
    copying.value = true
    await copyToClipboard(text)
    // 显示复制成功提示
    setTimeout(() => {
      copying.value = false
    }, 2000)
  } catch (e) {
    console.error('复制失败:', e)
    alert('复制失败，请手动复制')
    copying.value = false
  }
}

// API调用
async function fetchOverview() {
  try {
    loadingOverview.value = true
    if (isDemo) { overview.value = demoData.overview; return }
    const res = await axios.get(`${API_BASE}/api/stats/overview/${encodeURIComponent(shortCode)}`)
    overview.value = res.data
  } catch (e) { console.error('fetchOverview error:', e) } finally { loadingOverview.value = false }
}

async function fetchDetailedStats() {
  try {
    loadingDetailed.value = true
    if (isDemo) {
      detailedStats.value = demoData.detailed
      hourDistribution.value = demoData.detailed.hourDistribution
      weekdayDistribution.value = demoData.detailed.weekdayDistribution
      deviceDistribution.value = demoData.detailed.deviceDistribution
      browserDistribution.value = demoData.detailed.browserDistribution
      cityDistribution.value = demoData.detailed.cityDistribution
      countryDistribution.value = demoData.detailed.countryDistribution
      sourceDistribution.value = demoData.detailed.sourceDistribution
      refererDistribution.value = demoData.detailed.refererDistribution
      return
    }
    const params = new URLSearchParams()
    if (filterStart.value) params.set('start', filterStart.value)
    if (filterEnd.value) params.set('end', filterEnd.value)
    const res = await axios.get(`${API_BASE}/api/stats/detailed/${encodeURIComponent(shortCode)}?${params}`)
    detailedStats.value = res.data
    // 解析分布数据
    console.log('Detailed stats response:', res.data) // 调试信息
    hourDistribution.value = (res.data.hourDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    weekdayDistribution.value = (res.data.weekdayDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    deviceDistribution.value = (res.data.deviceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    browserDistribution.value = (res.data.browserDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    cityDistribution.value = (res.data.cityDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    countryDistribution.value = (res.data.countryDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    sourceDistribution.value = (res.data.sourceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    refererDistribution.value = (res.data.refererDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    
    console.log('Hour distribution:', hourDistribution.value)
    console.log('Device distribution:', deviceDistribution.value)
  } catch (e) { 
    console.error('fetchDetailedStats error:', e) 
  } finally { loadingDetailed.value = false }
}

async function fetchTrend() {
  try {
    loadingTrend.value = true
    if (isDemo) {
      const data = demoData.trend
      const labels = data.map(d => d.date).sort()
      trendLabels.value = labels
      trendValues.value = labels.map(l => { const item = data.find(d => d.date === l); return item ? Number(item.visits || item.count || 0) : 0 })
      return
    }
    const res = await axios.get(`${API_BASE}/api/stats/trend/${encodeURIComponent(shortCode)}?days=${selectedDays.value}`)
    const data = Array.isArray(res.data) ? res.data : []
    const labels = data.map(d => d.date).sort()
    trendLabels.value = labels
    trendValues.value = labels.map(l => {
      const item = data.find(d => d.date === l)
      return item ? Number(item.visits || item.count || 0) : 0
    })
  } catch (e) { console.error('fetchTrend error:', e) } finally { loadingTrend.value = false }
}

async function fetchEvents() {
  try {
    loadingEvents.value = true
    if (isDemo) { eventsList.value = demoData.events; return }
    const body = { code: shortCode, start: filterStart.value, end: filterEnd.value, source: filterSource.value, device: filterDevice.value, city: filterCity.value, page: 0, size: 100 }
    const res = await axios.post(`${API_BASE}/api/stats/events`, body)
    eventsList.value = Array.isArray(res.data) ? res.data : []
  } catch (e) { console.error('fetchEvents error:', e) } finally { loadingEvents.value = false }
}

async function refreshAll() {
  await Promise.all([fetchOverview(), fetchDetailedStats(), fetchTrend(), fetchEvents()])
}

function selectDays(d) {
  selectedDays.value = d
}

watch(selectedDays, () => fetchTrend())

// 点击折线点钻取到详情页
function onTrendPointClick(payload) {
  try {
    const date = payload?.label
    if (!date) return
    // 导航到详情页，传入 date 作为 query
    router.push({ path: `/stats/${encodeURIComponent(shortCode)}/detail`, query: { date } })
  } catch (e) { console.error(e) }
}

function drillCity(city){
  if (!city) return
  router.push({ path: `/stats/${encodeURIComponent(shortCode)}/detail`, query: { city } })
}

// numeric wrappers for AnimatedNumber (fallback to 0)
const totalPvNum = computed(() => Number(detailedStats.value?.pv ?? overview.value?.totalVisits ?? 0))
const totalUvNum = computed(() => Number(detailedStats.value?.uv ?? 0))
const todayPvNum = computed(() => Number(overview.value?.todayVisits ?? 0))

function applyFilters() {
  closeFilter()
  // 将筛选写入 URL 的 query，便于分享和回退
  const q = {}
  if (filterStart.value) q.start = filterStart.value
  if (filterEnd.value) q.end = filterEnd.value
  if (filterSource.value) q.source = filterSource.value
  if (filterDevice.value) q.device = filterDevice.value
  if (filterCity.value) q.city = filterCity.value
  // 保持 path 为当前 shortCode 的 stats 页面
  router.replace({ path: `/stats/${encodeURIComponent(shortCode)}`, query: q })
  refreshAll()
}

// 导出
async function exportCsv() {
  const body = { code: shortCode, start: filterStart.value, end: filterEnd.value }
  try {
    const res = await axios.post(`${API_BASE}/api/stats/export?format=csv`, body, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `stats-${shortCode}.csv`
    a.click()
    URL.revokeObjectURL(url)
  } catch (e) { console.error('export error:', e) }
}

const trendChartRef = ref(null)

async function exportPng() {
  try {
    const loaderComp = trendChartRef.value
    if (!loaderComp || !loaderComp.getInnerInstance) {
      alert('图表未就绪，无法导出')
      return
    }
    const inner = loaderComp.getInnerInstance()
    if (!inner || !inner.getDataURL) { alert('图表尚未加载或不支持导出'); return }
    const dataUrl = inner.getDataURL({ type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' })
    if (!dataUrl) { alert('导出失败'); return }
    const a = document.createElement('a')
    a.href = dataUrl
    a.download = `trend-chart-${shortCode}.png`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
  } catch (e) {
    console.error('导出PNG失败:', e)
    alert('导出PNG失败，请重试')
  }
}

// 实时轮询
let pollTimer = null
let sseSub = null
watch(realtime, (val) => {
  try {
    if (val) {
      // 首先尝试 SSE
      if (sseSub) { sseSub.stop(); sseSub = null }
      sseSub = subscribeRealtime(shortCode, (msg) => {
        // msg: { type, data }
        if (!msg) return
        if (msg.type === 'trend' && msg.data) {
          // 简单合并：假设 data 为 { date, visits }
          // 更复杂的合并可以由后端保证或在此实现
          // 直接刷新全部以保证一致性
          fetchTrend()
        }
        if (msg.type === 'event' || msg.type === 'events') { fetchEvents() }
      }, (info) => {
        if (!info || !info.ok) {
          // SSE 不可用，回退到短轮询
          console.warn('SSE unavailable, fallback to short-poll', info)
          refreshAll()
          pollTimer = setInterval(() => { refreshAll() }, 15000)
        } else {
          // SSE 成功连接时可选择仅加载必要数据
          refreshAll()
        }
      })
      // 设置一个保底短轮询，以防 SSE 中断（但避免重复）
      pollTimer = setInterval(() => { refreshAll() }, 60000)
    } else {
      if (sseSub) { sseSub.stop(); sseSub = null }
      if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
    }
  } catch (e) { console.error('realtime watch error', e) }
})

// cleanup on unmount
onBeforeUnmount(() => { if (pollTimer) clearInterval(pollTimer); if (sseSub) sseSub.stop() })

// 导航
function goHome() { router.push('/') }
function goDashboard() { router.push('/dashboard') }

// 初始化
onMounted(() => {
  const now = new Date()
  filterStart.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
  filterEnd.value = now.toISOString().split('T')[0]
  // 如果 URL 有 query，优先应用
  const q = route.query
  if (q) {
    if (q.start) filterStart.value = q.start
    if (q.end) filterEnd.value = q.end
    if (q.source) filterSource.value = q.source
    if (q.device) filterDevice.value = q.device
    if (q.city) filterCity.value = q.city
  }
  refreshAll()
})
</script>

<style scoped>
.stats-page {
  background: #f8fafc;
  min-height: 100vh;
}

/* 按钮样式 - 单色 */
.btn {
  padding: 8px 16px;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
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
  border-color: #cbd5e1;
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
  transition: all 0.2s;
}

.btn-tab.active {
  background: #2563eb;
  color: white;
}

/* 卡片样式 */
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
  letter-spacing: 0.5px;
}

.metric-value {
  font-size: 28px;
  font-weight: 700;
  color: #1e293b;
}

/* 信息网格 */
.info-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.info-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.info-label {
  font-size: 12px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #334155;
}

.info-code {
  font-family: monospace;
  background: #f1f5f9;
  padding: 4px 8px;
  border-radius: 4px;
  color: #2563eb;
  font-size: 14px;
}

.info-link {
  color: #2563eb;
  text-decoration: none;
  font-size: 14px;
}

.info-link:hover {
  text-decoration: underline;
}

.info-url {
  font-size: 13px;
  font-family: monospace;
  color: #64748b;
  background: #f8fafc;
  padding: 8px 12px;
  border-radius: 4px;
  word-break: break-all;
  margin-top: 4px;
}

/* 图表容器 */
.chart-container { /* removed unused style to avoid lint warning - kept for reference */
/*   height: 280px;
  background: #fafbfc;
  border: 1px dashed #e2e8f0;
  border-radius: 6px; */
}

.chart-container-enhanced {
  min-height: 320px;
  position: relative;
}

.chart-placeholder {
  height: 280px;
  background: #f8fafc;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  color: #94a3b8;
  border: 1px dashed #e2e8f0;
  border-radius: 6px;
}

/* 图表洞察提示 */
.chart-insight {
  margin-top: 12px;
  padding: 12px 16px;
  background: #f0f9ff;
  border-left: 3px solid #2563eb;
  border-radius: 4px;
  font-size: 13px;
  color: #1e40af;
}

.insight-text {
  line-height: 1.5;
}

/* 柱状图 */
.bar-chart {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.bar-item {
  display: grid;
  grid-template-columns: 60px 1fr 50px;
  align-items: center;
  gap: 12px;
}

.bar-label {
  font-size: 13px;
  color: #64748b;
  text-align: right;
}

.bar-track {
  height: 20px;
  background: #f1f5f9;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  background: #2563eb;
  border-radius: 4px;
  transition: width 0.3s ease;
}

.bar-value {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  text-align: right;
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
  min-width: 50px;
  text-align: right;
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
  transition: width 0.3s ease;
}

/* 排行榜 */
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
  flex-shrink: 0;
}

.rank-item:nth-child(n+4) .rank-num {
  background: #94a3b8;
}

.rank-name {
  flex: 1;
  font-size: 14px;
  color: #334155;
  min-width: 0;
}

.rank-count {
  font-size: 14px;
  font-weight: 600;
  color: #2563eb;
  min-width: 50px;
  text-align: right;
}

/* Referer列表 */
.referer-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.referer-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #f8fafc;
  border-radius: 4px;
}

.referer-url {
  flex: 1;
  font-size: 13px;
  color: #64748b;
  font-family: monospace;
  min-width: 0;
}

.referer-count {
  font-size: 13px;
  font-weight: 600;
  color: #334155;
  margin-left: 12px;
}

/* 事件表格 */
.events-table-container {
  overflow-x: auto;
}

.events-table {
  width: 100%;
  border-collapse: collapse;
}

.events-table th,
.events-table td {
  padding: 12px 16px;
  text-align: left;
  font-size: 13px;
}

.events-table th {
  background: #f8fafc;
  font-weight: 600;
  color: #64748b;
  border-bottom: 1px solid #e2e8f0;
}

.events-table td {
  border-bottom: 1px solid #f1f5f9;
  color: #334155;
}

.events-table tr:hover td {
  background: #fafbfc;
}

.device-tag {
  display: inline-block;
  padding: 2px 8px;
  background: #f1f5f9;
  border-radius: 4px;
  font-size: 12px;
  color: #64748b;
}

/* 空状态 */
.empty-state {
  padding: 40px 20px;
  text-align: center;
  color: #94a3b8;
  font-size: 14px;
}

/* 弹窗 */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal {
  width: 520px;
  max-width: 90vw;
  background: white;
  border-radius: 12px;
  overflow: hidden;
}

.modal-header {
  padding: 16px 20px;
  font-size: 16px;
  font-weight: 600;
  color: #1e293b;
  border-bottom: 1px solid #e2e8f0;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  padding: 16px 20px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #e2e8f0;
}

/* 表单 */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  font-size: 14px;
  color: #334155;
  background: white;
}

.form-input:focus {
  outline: none;
  border-color: #2563eb;
}

.truncate {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.rank-item { cursor: default; }
.rank-item.clickable { cursor: pointer; }
.rank-item.clickable:hover { background: #fbfcfe; }

/* DEMO 横幅样式 */
.demo-banner {
  padding: 12px 16px;
  background: #2563eb;
  color: white;
  font-size: 14px;
  font-weight: 500;
  border-radius: 4px;
  margin-bottom: 16px;
  text-align: center;
}
</style>
