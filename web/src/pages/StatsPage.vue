<template>
  <div class="min-h-screen stats-page">
    <!-- 粘性顶部栏 -->
    <div class="topbar">
      <div class="container">
        <div class="left">
          <button class="btn btn-ghost" @click="goHome" title="返回首页">首页</button>
          <button class="btn btn-ghost" @click="goDashboard" title="数据看板">看板</button>
        </div>
        <div class="center">
          <div class="search-wrap">
            <input class="search-input" placeholder="输入短码或关键词搜索" @keyup.enter="$router.push({ path: '/dashboard', query: { q: $event.target.value } })" />
          </div>
        </div>
        <div class="right">
          <div class="preset">
            <button class="btn btn-sm" :class="{active: selectedDays===7}" @click="setPreset(7)">7天</button>
            <button class="btn btn-sm" :class="{active: selectedDays===14}" @click="setPreset(14)">14天</button>
            <button class="btn btn-sm" :class="{active: selectedDays===30}" @click="setPreset(30)">30天</button>
          </div>
          <button class="btn btn-primary" @click="debouncedRefreshAll" title="刷新">刷新</button>
        </div>
      </div>
    </div>

    <div class="container main">
      <div class="demo-banner" v-if="isDemo">正在演示模式 (DEMO)：页面使用本地示例数据。</div>

      <!-- metrics -->
      <transition-group name="card-fade" tag="div" class="metrics grid">
        <div class="metric-card elevated" key="pv">
          <div class="metric-label">总PV</div>
          <div class="metric-value">
            <AnimatedNumber v-if="totalPvNum" :value="totalPvNum" :duration="900" />
            <span v-else>-</span>
          </div>
        </div>
        <div class="metric-card elevated" key="uv">
          <div class="metric-label">总UV</div>
          <div class="metric-value">
            <AnimatedNumber v-if="totalUvNum" :value="totalUvNum" :duration="900" />
            <span v-else>-</span>
          </div>
        </div>
        <div class="metric-card elevated" key="today">
          <div class="metric-label">今日PV</div>
          <div class="metric-value">
            <AnimatedNumber v-if="todayPvNum" :value="todayPvNum" :duration="900" />
            <span v-else>-</span>
          </div>
        </div>
        <div class="metric-card elevated" key="ratio">
          <div class="metric-label">PV/UV比</div>
          <div class="metric-value">{{ detailedStats?.pvUvRatio ? detailedStats.pvUvRatio.toFixed(2) : '-' }}</div>
        </div>
        <div class="metric-card elevated" key="avg">
          <div class="metric-label">日均PV</div>
          <div class="metric-value">{{ avgVisits }}</div>
        </div>
        <div class="metric-card elevated" key="peak">
          <div class="metric-label">峰值PV</div>
          <div class="metric-value">{{ peakVisits }}</div>
        </div>
      </transition-group>

      <!-- two-column layout -->
      <div class="grid-main">
        <div class="left-col">
          <div class="card elevated">
            <div class="card-header">
              <div class="title">访问趋势</div>
              <div class="actions">
                <button class="btn-ghost" @click="exportPng" :disabled="loadingTrend">导出PNG</button>
                <button class="btn-ghost" @click="exportCsv">导出CSV</button>
              </div>
            </div>
            <div class="card-body chart-area" ref="trendContainerRef">
              <AsyncChartLoader
                :loader="TrendChartLoader"
                :passThroughProps="{ labels: trendLabels, values: trendValues, height: 320 }"
                ref="trendChartRef"
                :eager="isDemo || trendVisible"
                @point-click="onTrendPointClick"
              >
                <template #fallback>
                  <div class="skeleton-wrap">
                    <SkeletonLoader variant="card" :height="320" />
                  </div>
                </template>
              </AsyncChartLoader>

              <div v-if="!loadingTrend && trendLabels.length === 0" class="empty-state enhanced">
                <svg width="80" height="60" viewBox="0 0 80 60"><g fill="none" stroke="#94a3b8" stroke-width="1.5"><path d="M6 50 L20 30 L34 40 L50 20 L66 28 L74 18"/></g></svg>
                <div class="empty-text">暂无趋势数据，可尝试调整时间范围</div>
              </div>

              <div v-if="loadingTrend" class="mt-4"><SkeletonLoader :height="120" /></div>

              <div v-if="trendInsight" class="chart-insight">
                <span class="insight-text">{{ trendInsight }}</span>
              </div>
            </div>
          </div>

          <!-- 分布卡片 -->
          <div class="card-grid">
            <div class="card elevated">
              <div class="card-header">24小时分布</div>
              <div class="card-body">
                <div class="bar-chart small">
                  <div v-for="item in hourDistribution" :key="item.key" class="bar-item hoverable">
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

            <div class="card elevated">
              <div class="card-header">设备分布</div>
              <div class="card-body">
                <div class="distribution-list">
                  <div v-for="item in deviceDistribution" :key="item.key" class="dist-item hoverable">
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

            <div class="card elevated">
              <div class="card-header">城市 TOP10</div>
              <div class="card-body">
                <AsyncChartLoader :loader="CityBarLoader" :passThroughProps="{ labels: cityDistribution.slice(0,10).map(i => i.key), values: cityDistribution.slice(0,10).map(i => i.count), height: 240 }" :eager="isDemo" @point-click="(p) => drillCity(p.label)" />
                <div v-if="!cityDistribution.length" class="empty-state">暂无数据</div>
              </div>
            </div>

            <div class="card elevated">
              <div class="card-header">国家/地区分布</div>
              <div class="card-body">
                <AsyncChartLoader :loader="CountryPieLoader" :passThroughProps="{ data: countryDistribution.slice(0,10).map(i => ({ label: i.key, name: i.key, value: i.count })), height: 240 }" :eager="isDemo" />
                <div v-if="!countryDistribution.length" class="empty-state">暂无数据</div>
              </div>
            </div>
          </div>
        </div>

        <div class="right-col">
          <div class="card elevated info-card">
            <div class="card-header">短链信息</div>
            <div class="card-body">
              <div class="info-grid">
                <div class="info-item">
                  <span class="info-label">短码</span>
                  <code class="info-code">{{ shortCode }}</code>
                </div>
                <div class="info-item">
                  <span class="info-label">短链地址</span>
                  <div class="link-line">
                    <a :href="shortUrl" target="_blank" class="info-link">{{ shortUrl }}</a>
                    <button class="btn-ghost tiny" @click="copy(shortUrl)">{{ copying ? '已复制' : '复制' }}</button>
                  </div>
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
                <div class="info-item full">
                  <span class="info-label">原始链接</span>
                  <div class="info-url">{{ overview?.longUrl || '-' }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="card elevated">
            <div class="card-header">最近访问记录 <span class="muted">共 {{ eventsList.length }} 条</span></div>
            <div class="card-body p-0">
              <div class="events-table-container">
                <table class="events-table">
                  <thead>
                    <tr>
                      <th>时间</th><th>IP</th><th>设备</th><th>城市</th><th>国家</th><th>来源</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-if="loadingEvents"><td colspan="6"><SkeletonLoader :height="120" /></td></tr>
                    <tr v-else v-for="(event, idx) in eventsList.slice(0, 50)" :key="idx" class="hoverable">
                      <td>{{ formatDate(event.ts) }}</td>
                      <td>{{ event.ip || '-' }}</td>
                      <td><span class="device-tag">{{ event.deviceType || '-' }}</span></td>
                      <td>{{ event.city || '-' }}</td>
                      <td>{{ event.country || '-' }}</td>
                      <td class="truncate max-w">{{ event.sourceHost || '-' }}</td>
                    </tr>
                    <tr v-if="!loadingEvents && !eventsList.length"><td colspan="6" class="empty-state">暂无访问记录</td></tr>
                  </tbody>
                </table>
              </div>
            </div>
          </div>

          <div v-if="apiErrors.length" class="error-banner sticky-error">
            <div class="error-content">
              <span class="error-message">数据加载失败：{{ apiErrors[0].message }}</span>
              <button class="btn btn-retry" @click="retryFetch">重试</button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Filter Modal -->
    <div v-if="showFilter" class="modal-backdrop" @click.self="closeFilter">
      <div class="modal">
        <div class="modal-header">筛选条件</div>
        <div class="modal-body">
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div class="form-group"><label>开始日期</label><input v-model="filterStart" type="date" class="form-input" /></div>
            <div class="form-group"><label>结束日期</label><input v-model="filterEnd" type="date" class="form-input" /></div>
            <div class="form-group"><label>来源</label><input v-model="filterSource" type="text" placeholder="不填为全部" class="form-input" /></div>
            <div class="form-group"><label>设备</label>
              <select v-model="filterDevice" class="form-input"><option value="">全部</option><option value="mobile">移动</option><option value="desktop">桌面</option><option value="tablet">平板</option></select>
            </div>
            <div class="form-group"><label>城市</label><input v-model="filterCity" type="text" placeholder="不填为全部" class="form-input" /></div>
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
</template>

<script setup lang="ts">
import { ref, computed, onMounted, defineAsyncComponent, watch, onBeforeUnmount, nextTick } from 'vue'
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
// 统一为浅色企业冷静风
const useDarkTheme = false

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const routeShortCode = route.params.shortCode
const isDemo = route.query && (route.query.demo === '1' || route.query.demo === 'true')
const shortCode = isDemo ? (routeShortCode || 'demo123') : routeShortCode
const shortUrl = `${SHORT_BASE}/${encodeURIComponent(shortCode)}`

// Data
const overview = ref(null)
const detailedStats = ref(null)
const eventsList = ref([])
const trendLabels = ref([])
const trendValues = ref([])
const selectedDays = ref(7)

// loading
const loadingOverview = ref(false)
const loadingDetailed = ref(false)
const loadingTrend = ref(false)
const loadingEvents = ref(false)

// errors
const apiErrors = ref([])
function pushApiError(key, err) {
  try {
    const info: any = { key, time: new Date().toISOString() }
    if (!err) { info.message = 'unknown error'; apiErrors.value.push(info); return }
    if (err.response) {
      info.status = err.response.status
      info.url = err.config && err.config.url
      info.message = err.response.data && (err.response.data.message || JSON.stringify(err.response.data)) || err.message
    } else if (err.request) {
      info.message = 'no response from server'
      info.url = err.config && err.config.url
    } else {
      info.message = err.message
    }
    apiErrors.value.unshift(info)
    if (apiErrors.value.length > 10) apiErrors.value.splice(10)
  } catch (e) {
    console.error('pushApiError failed', e)
  }
}

// distributions
const hourDistribution = ref([])
const weekdayDistribution = ref([])
const deviceDistribution = ref([])
const browserDistribution = ref([])
const cityDistribution = ref([])
const countryDistribution = ref([])
const sourceDistribution = ref([])
const refererDistribution = ref([])

// filters
const showFilter = ref(false)
const filterStart = ref('')
const filterEnd = ref('')
const filterSource = ref('')
const filterDevice = ref('')
const filterCity = ref('')

// realtime
const realtime = ref(false)

// computed
const peakVisits = computed(() => trendValues.value.length ? Math.max(...trendValues.value) : 0)
const avgVisits = computed(() => {
  if (!trendValues.value.length) return 0
  return Math.round(trendValues.value.reduce((a, b) => a + b, 0) / trendValues.value.length)
})

const hourMax = computed(() => Math.max(...hourDistribution.value.map(i => i.count), 1))
const weekdayMax = computed(() => Math.max(...weekdayDistribution.value.map(i => i.count), 1))
const deviceTotal = computed(() => deviceDistribution.value.reduce((a, b) => a + b.count, 0))
const browserTotal = computed(() => browserDistribution.value.reduce((a, b) => a + b.count, 0))

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

// utils
function formatDate(ts) {
  if (!ts) return '-'
  try { return new Date(ts).toLocaleString('zh-CN') } catch { return String(ts) }
}
function getBarWidth(value, max) { return max > 0 ? (value / max * 100) : 0 }
function getPercent(value, total) { return total > 0 ? Math.round(value / total * 100) : 0 }

// copy
const copying = ref(false)
async function copy(text) {
  try {
    copying.value = true
    await copyToClipboard(text)
    setTimeout(() => { copying.value = false }, 2000)
  } catch (e) {
    console.error('复制失败:', e)
    alert('复制失败，请手动复制')
    copying.value = false
  }
}

// API calls (same as before)
async function fetchOverview() {
  try {
    loadingOverview.value = true
    if (isDemo) { overview.value = demoData.overview; loadingOverview.value = false; return }
    const res = await axios.get(`${API_BASE}/api/stats/overview/${encodeURIComponent(shortCode)}`)
    overview.value = res.data
  } catch (e) { console.error('fetchOverview error:', e); pushApiError('fetchOverview', e) } finally { loadingOverview.value = false }
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
      loadingDetailed.value = false
      return
    }
    const params = new URLSearchParams()
    if (filterStart.value) params.set('start', filterStart.value)
    if (filterEnd.value) params.set('end', filterEnd.value)
    const res = await axios.get(`${API_BASE}/api/stats/detailed/${encodeURIComponent(shortCode)}?${params}`)
    detailedStats.value = res.data
    hourDistribution.value = (res.data.hourDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    weekdayDistribution.value = (res.data.weekdayDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    deviceDistribution.value = (res.data.deviceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    browserDistribution.value = (res.data.browserDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    cityDistribution.value = (res.data.cityDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    countryDistribution.value = (res.data.countryDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    sourceDistribution.value = (res.data.sourceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    refererDistribution.value = (res.data.refererDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
  } catch (e) {
    console.error('fetchDetailedStats error:', e); pushApiError('fetchDetailedStats', e)
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
      loadingTrend.value = false
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
  } catch (e) { console.error('fetchTrend error:', e); pushApiError('fetchTrend', e) } finally { loadingTrend.value = false }
}

async function fetchEvents() {
  try {
    loadingEvents.value = true
    if (isDemo) { eventsList.value = demoData.events; loadingEvents.value = false; return }
    const body = { code: shortCode, start: filterStart.value, end: filterEnd.value, source: filterSource.value, device: filterDevice.value, city: filterCity.value, page: 0, size: 100 }
    const res = await axios.post(`${API_BASE}/api/stats/events`, body)
    eventsList.value = Array.isArray(res.data) ? res.data : []
  } catch (e) { console.error('fetchEvents error:', e); pushApiError('fetchEvents', e) } finally { loadingEvents.value = false }
}

async function refreshAll() {
  // Guard: avoid overlapping refreshes
  if (refreshLock.value) return
  refreshLock.value = true
  try {
    await Promise.all([fetchOverview(), fetchDetailedStats(), fetchTrend(), fetchEvents()])
  } finally {
    refreshLock.value = false
  }
}

function selectDays(d) {
  selectedDays.value = d
}

watch(selectedDays, () => fetchTrend())

function onTrendPointClick(payload) {
  try {
    const date = payload?.label
    if (!date) return
    router.push({ path: `/stats/${encodeURIComponent(shortCode)}/detail`, query: { date } })
  } catch (e) { console.error(e) }
}

function drillCity(city){
  if (!city) return
  router.push({ path: `/stats/${encodeURIComponent(shortCode)}/detail`, query: { city } })
}

const totalPvNum = computed(() => Number(detailedStats.value?.pv ?? overview.value?.totalVisits ?? 0))
const totalUvNum = computed(() => Number(detailedStats.value?.uv ?? 0))
const todayPvNum = computed(() => Number(overview.value?.todayVisits ?? 0))

function applyFilters() {
  closeFilter()
  const q: any = {}
  if (filterStart.value) q.start = filterStart.value
  if (filterEnd.value) q.end = filterEnd.value
  if (filterSource.value) q.source = filterSource.value
  if (filterDevice.value) q.device = filterDevice.value
  if (filterCity.value) q.city = filterCity.value
  router.replace({ path: `/stats/${encodeURIComponent(shortCode)}`, query: q })
  debouncedRefreshAll()
}

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
const trendContainerRef = ref(null)
const trendVisible = ref(false)
let io = null

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

// realtime polling with SSE fallback (same)
let pollTimer: any = null
let sseSub: any = null
watch(realtime, (val) => {
  try {
    if (val) {
      if (sseSub) { sseSub.stop(); sseSub = null }
      sseSub = subscribeRealtime(shortCode, (msg) => {
        if (!msg) return
        if (msg.type === 'trend' && msg.data) { fetchTrend() }
        if (msg.type === 'event' || msg.type === 'events') { fetchEvents() }
      }, (info) => {
        if (!info || !info.ok) {
          console.warn('SSE unavailable, fallback to short-poll', info)
          refreshAll()
          pollTimer = setInterval(() => { refreshAll() }, 15000)
        } else {
          refreshAll()
        }
      })
      pollTimer = setInterval(() => { refreshAll() }, 60000)
    } else {
      if (sseSub) { sseSub.stop(); sseSub = null }
      if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
    }
  } catch (e) { console.error('realtime watch error', e) }
})

// cleanup
onBeforeUnmount(() => { if (pollTimer) clearInterval(pollTimer); if (sseSub) sseSub.stop(); if (io) io.disconnect && io.disconnect() })

function goHome() { router.push('/') }
function goDashboard() { router.push('/dashboard') }

// init
onMounted(async () => {
  const now = new Date()
  filterStart.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
  filterEnd.value = now.toISOString().split('T')[0]
  const q = route.query
  if (q) {
    if (q.start) filterStart.value = q.start
    if (q.end) filterEnd.value = q.end
    if (q.source) filterSource.value = q.source
    if (q.device) filterDevice.value = q.device
    if (q.city) filterCity.value = q.city
  }

  // intersection observer for lazy-loading charts
  await nextTick()
  try {
    const el = trendContainerRef.value && (trendContainerRef.value.$el || trendContainerRef.value)
    if (el && 'IntersectionObserver' in window) {
      io = new IntersectionObserver((entries) => {
        entries.forEach(e => {
          if (e.isIntersecting) {
            trendVisible.value = true
            fetchTrend()
            io.disconnect()
          }
        })
      }, { rootMargin: '200px' })
      io.observe(el)
    } else {
      trendVisible.value = true
      fetchTrend()
    }
  } catch (e) {
    trendVisible.value = true
    fetchTrend()
  }

  refreshAll()
})

// refresh debounce & lock
const refreshLock = ref(false)
let refreshTimer: any = null
function debouncedRefreshAll() {
  if (refreshTimer) clearTimeout(refreshTimer)
  refreshTimer = setTimeout(() => { refreshAll() }, 250)
}

// small helper for quick presets
function setPreset(days: number) {
  selectedDays.value = days
  debouncedRefreshAll()
}

function openFilter(){ showFilter.value = true }
function closeFilter(){ showFilter.value = false }
function resetFilters() {
  filterStart.value = ''
  filterEnd.value = ''
  filterSource.value = ''
  filterDevice.value = ''
  filterCity.value = ''
  debouncedRefreshAll()
}
function retryFetch(){ apiErrors.value = []; debouncedRefreshAll() }
</script>

<style scoped>
:root {
  /* 统一蓝白企业冷静风配色，使用设计系统变量 */
  --bg: var(--tf-bg-page);
  --card: var(--tf-bg-card);
  --muted: var(--tf-text-muted);
  --accent: var(--tf-brand-primary);
  --glass: rgba(255,255,255,0.7);
}

.stats-page { background: var(--bg); font-family: Inter, ui-sans-serif, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial; color: var(--tf-text-body); min-height: 100vh; padding-top: 84px; }

/* container */
.container { max-width: 1200px; margin: 0 auto; padding: 0 18px; }
.main { padding-top: 18px; padding-bottom: 48px; }

/* topbar sticky */
.topbar {
  position: fixed; top: 0; left: 0; right: 0; height: 68px;
  backdrop-filter: blur(6px); -webkit-backdrop-filter: blur(6px);
  background: linear-gradient(180deg, rgba(255,255,255,0.9), rgba(255,255,255,0.75));
  border-bottom: 1px solid var(--tf-border);
  display: flex; align-items: center; z-index: 80;
}
.topbar .container { display:flex; align-items:center; justify-content:space-between; }
.topbar .left, .topbar .center, .topbar .right { display:flex; align-items:center; gap:12px; }
.search-wrap { width: 420px; }
.search-input { width:100%; padding:10px 12px; border-radius:8px; border:1px solid var(--tf-border); background:linear-gradient(180deg,#fff,#fbfdff); font-size:14px; box-shadow:0 1px 2px rgba(16,24,40,0.03); }
.btn { padding:8px 12px; border-radius:8px; background:transparent; border:none; cursor:pointer; color:var(--tf-brand-primary); font-weight:600; }
.btn-ghost { background:transparent; color:#334155; padding:6px 10px; border-radius:8px; }
.btn-ghost.tiny { padding:6px 8px; font-size:12px; }
.btn-sm { padding:6px 8px; border-radius:6px; background:#f1f5f9; color:#334155; margin-right:6px; }
.btn-sm.active { background: var(--accent); color:#fff; }

/* metrics */
.metrics { display:grid; grid-template-columns: repeat(6,1fr); gap:16px; margin-bottom:18px; }
.metric-card { padding:16px; border-radius:12px; background:var(--card); border:1px solid var(--tf-border); text-align:center; transition:transform .18s ease, box-shadow .18s ease; }
.metric-card .metric-label { font-size:12px; color:var(--muted); margin-bottom:8px; text-transform:uppercase; letter-spacing:0.6px; }
.metric-value { font-size:26px; font-weight:700; color:#0f172a; }
.metric-card.elevated { box-shadow: var(--tf-shadow-card); }

/* layout */
.grid-main { display:grid; grid-template-columns: 1fr 360px; gap:20px; align-items:start; }
.left-col { display:flex; flex-direction:column; gap:16px; }
.right-col { display:flex; flex-direction:column; gap:16px; }

/* cards */
.card { border-radius:12px; background:var(--card); border:1px solid var(--tf-border); overflow:hidden; }
.card.elevated { box-shadow: var(--tf-shadow-card); }
/* 移除暗色变体，保持统一蓝白风格 */

.card-header { display:flex; justify-content:space-between; align-items:center; padding:16px 18px; border-bottom:1px solid var(--tf-divider); font-weight:700; }
.card-body { padding:18px; }

/* charts */
.chart-area { min-height: 360px; position:relative; display:flex; flex-direction:column; gap:12px; }
.skeleton-wrap { display:flex; align-items:center; justify-content:center; height:320px; }
.chart-insight { margin-top:8px; padding:12px; background:#eef2ff; border-radius:8px; border-left:4px solid var(--accent); color:#1e40af; }

/* bar charts & distributions */
.bar-chart.small .bar-item { display:grid; grid-template-columns: 48px 1fr 56px; gap:10px; align-items:center; padding:6px 0; }
.bar-track { height:14px; background:#f1f5f9; border-radius:6px; overflow:hidden; }
.bar-fill { height:100%; background:linear-gradient(90deg,var(--accent), #1d4ed8); transition:width .35s cubic-bezier(.2,.8,.2,1); border-radius:6px; }
.dist-bar { height:8px; background:#f1f5f9; border-radius:4px; overflow:hidden; }
.dist-fill { height:100%; background:linear-gradient(90deg,var(--accent), #60a5fa); transition:width .35s ease; border-radius:4px; }

/* info grid */
.info-grid { display:grid; grid-template-columns: repeat(1,1fr); gap:12px; }
.info-item.full { grid-column:1/-1; }
.info-code { background:#f8fafc; padding:6px 10px; border-radius:8px; font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, "Roboto Mono", monospace; color:var(--accent); }
.link-line { display:flex; gap:8px; align-items:center; }

/* events table */
.events-table { width:100%; border-collapse:collapse; }
.events-table th, .events-table td { padding:12px 14px; text-align:left; font-size:13px; border-bottom:1px solid #f1f5f9; }
.events-table tr.hoverable:hover td { background:#fbfdff; transform:translateX(0); }

/* empty state */
.empty-state.enhanced { display:flex; gap:12px; align-items:center; justify-content:center; flex-direction:column; padding:28px; color:var(--muted); }
.empty-text { color:var(--muted); }

/* small UI */
.muted { color:#64748b; font-size:12px; margin-left:8px; }
.device-tag { background:#f1f5f9; padding:4px 8px; border-radius:6px; font-size:12px; color:#64748b; }

/* modal */
.modal { width:560px; background:var(--card); border-radius:12px; overflow:hidden; box-shadow: var(--tf-shadow-modal); }
.modal-header { padding:16px; font-weight:700; border-bottom:1px solid var(--tf-divider); }
.modal-body { padding:16px; }
.modal-footer { padding:12px 16px; display:flex; justify-content:flex-end; gap:8px; border-top:1px solid var(--tf-divider); }

/* error banner */
.error-banner { padding:12px 16px; border-radius:10px; background:#fff5f5; border:1px solid #fee2e2; color:#991b1b; display:flex; justify-content:space-between; align-items:center; gap:12px; }

/* transitions */
.card-fade-enter-from { opacity:0; transform:translateY(8px); }
.card-fade-enter-active { transition:all .28s cubic-bezier(.2,.8,.2,1); }
.card-fade-leave-to { opacity:0; transform:translateY(-6px); }

/* responsive */
@media (max-width: 1100px) {
  .metrics { grid-template-columns: repeat(3,1fr); }
  .grid-main { grid-template-columns: 1fr; }
  .right-col { order:2 }
  .left-col { order:1 }
  .search-wrap { display:none; }
}

/* hoverable */
.hoverable { transition: background .12s ease, transform .12s ease; cursor:default; }
.hoverable:hover { background: #fbfdff; transform: translateY(-2px); }

/* utility */
.btn-retry { background:var(--accent); color:white; padding:8px 12px; border-radius:8px; border:none; }
</style>
