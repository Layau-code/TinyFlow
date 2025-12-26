<template>
  <div class="min-h-screen stats-page pt-24 md:pt-28">
    <div class="max-w-7xl mx-auto p-6 space-y-6">
      <!-- 顶部操作栏 -->
      <div class="flex items-center justify-between flex-wrap gap-4">
        <div class="flex items-center gap-3">
          <button @click="goHome" class="btn btn-secondary">返回首页</button>
          <button @click="goDashboard" class="btn btn-secondary">数据看板</button>
        </div>
        <div class="flex items-center gap-3">
          <button @click="copy(shortUrl)" class="btn btn-secondary">复制短链</button>
          <button @click="openFilter" class="btn btn-primary">筛选</button>
          <button @click="exportCsv" class="btn btn-secondary">导出CSV</button>
        </div>
      </div>

      <!-- 核心指标卡片 -->
      <div class="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-6 gap-4">
        <div class="metric-card">
          <div class="metric-label">总PV</div>
          <div class="metric-value">{{ detailedStats?.pv ?? overview?.totalVisits ?? '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">总UV</div>
          <div class="metric-value">{{ detailedStats?.uv ?? '-' }}</div>
        </div>
        <div class="metric-card">
          <div class="metric-label">今日PV</div>
          <div class="metric-value">{{ overview?.todayVisits ?? '-' }}</div>
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
      <div class="card">
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
      <div class="card">
        <div class="card-header flex items-center justify-between">
          <span>访问趋势</span>
          <div class="flex gap-2">
            <button class="btn-tab" :class="{ active: selectedDays === 7 }" @click="selectDays(7)">7天</button>
            <button class="btn-tab" :class="{ active: selectedDays === 14 }" @click="selectDays(14)">14天</button>
            <button class="btn-tab" :class="{ active: selectedDays === 30 }" @click="selectDays(30)">30天</button>
          </div>
        </div>
        <div class="card-body">
          <div class="chart-container">
            <Suspense>
              <TrendChart :values="trendValues" :labels="trendLabels" :showValues="true" />
              <template #fallback><div class="chart-placeholder"></div></template>
            </Suspense>
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

        <!-- 城市TOP10 -->
        <div class="card">
          <div class="card-header">城市 TOP 10</div>
          <div class="card-body">
            <div class="rank-list">
              <div v-for="(item, idx) in cityDistribution.slice(0, 10)" :key="item.key" class="rank-item">
                <span class="rank-num">{{ idx + 1 }}</span>
                <span class="rank-name">{{ item.key || '未知' }}</span>
                <span class="rank-count">{{ item.count }}</span>
              </div>
              <div v-if="!cityDistribution.length" class="empty-state">暂无数据</div>
            </div>
          </div>
        </div>

        <!-- 国家分布 -->
        <div class="card">
          <div class="card-header">国家/地区分布</div>
          <div class="card-body">
            <div class="rank-list">
              <div v-for="(item, idx) in countryDistribution.slice(0, 10)" :key="item.key" class="rank-item">
                <span class="rank-num">{{ idx + 1 }}</span>
                <span class="rank-name">{{ item.key || '未知' }}</span>
                <span class="rank-count">{{ item.count }}</span>
              </div>
              <div v-if="!countryDistribution.length" class="empty-state">暂无数据</div>
            </div>
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
                <tr v-for="(event, idx) in eventsList.slice(0, 50)" :key="idx">
                  <td>{{ formatDate(event.ts) }}</td>
                  <td>{{ event.ip || '-' }}</td>
                  <td><span class="device-tag">{{ event.deviceType || '-' }}</span></td>
                  <td>{{ event.city || '-' }}</td>
                  <td>{{ event.country || '-' }}</td>
                  <td class="truncate max-w-[200px]">{{ event.sourceHost || '-' }}</td>
                </tr>
                <tr v-if="!eventsList.length">
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
import { ref, computed, onMounted, defineAsyncComponent, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRoute, useRouter } from 'vue-router'
import { SHORT_BASE, API_BASE } from '../composables/shortBase'
import { copyToClipboard } from '../composables/useCopy'
import axios from 'axios'

const TrendChart = defineAsyncComponent(() => import('../components/TrendChart.vue'))

const route = useRoute()
const router = useRouter()
const { t } = useI18n()

const shortCode = route.params.shortCode
const shortUrl = `${SHORT_BASE}/${encodeURIComponent(shortCode)}`

// 数据状态
const overview = ref(null)
const detailedStats = ref(null)
const eventsList = ref([])
const trendLabels = ref([])
const trendValues = ref([])
const selectedDays = ref(7)

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

async function copy(text) {
  try {
    await copyToClipboard(text)
  } catch (e) {
    console.error('复制失败:', e)
  }
}

// API调用
async function fetchOverview() {
  try {
    const res = await axios.get(`${API_BASE}/api/stats/overview/${encodeURIComponent(shortCode)}`)
    overview.value = res.data
  } catch (e) { console.error('fetchOverview error:', e) }
}

async function fetchDetailedStats() {
  try {
    const params = new URLSearchParams()
    if (filterStart.value) params.set('start', filterStart.value)
    if (filterEnd.value) params.set('end', filterEnd.value)
    const res = await axios.get(`${API_BASE}/api/stats/detailed/${encodeURIComponent(shortCode)}?${params}`)
    detailedStats.value = res.data
    
    // 解析分布数据
    hourDistribution.value = (res.data.hourDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    weekdayDistribution.value = (res.data.weekdayDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    deviceDistribution.value = (res.data.deviceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    browserDistribution.value = (res.data.browserDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    cityDistribution.value = (res.data.cityDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    countryDistribution.value = (res.data.countryDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    sourceDistribution.value = (res.data.sourceDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
    refererDistribution.value = (res.data.refererDistribution || []).map(i => ({ key: i.key || i.label, count: Number(i.count || i.value || 0) }))
  } catch (e) { console.error('fetchDetailedStats error:', e) }
}

async function fetchTrend() {
  try {
    const res = await axios.get(`${API_BASE}/api/stats/trend/${encodeURIComponent(shortCode)}?days=${selectedDays.value}`)
    const data = Array.isArray(res.data) ? res.data : []
    const labels = data.map(d => d.date).sort()
    trendLabels.value = labels
    trendValues.value = labels.map(l => {
      const item = data.find(d => d.date === l)
      return item ? Number(item.visits || item.count || 0) : 0
    })
  } catch (e) { console.error('fetchTrend error:', e) }
}

async function fetchEvents() {
  try {
    const body = { code: shortCode, start: filterStart.value, end: filterEnd.value, source: filterSource.value, device: filterDevice.value, city: filterCity.value, page: 0, size: 100 }
    const res = await axios.post(`${API_BASE}/api/stats/events`, body)
    eventsList.value = Array.isArray(res.data) ? res.data : []
  } catch (e) { console.error('fetchEvents error:', e) }
}

async function refreshAll() {
  await Promise.all([fetchOverview(), fetchDetailedStats(), fetchTrend(), fetchEvents()])
}

function selectDays(d) {
  selectedDays.value = d
}

watch(selectedDays, () => fetchTrend())

// 筛选
function openFilter() { showFilter.value = true }
function closeFilter() { showFilter.value = false }
function resetFilters() {
  filterStart.value = ''
  filterEnd.value = ''
  filterSource.value = ''
  filterDevice.value = ''
  filterCity.value = ''
}
function applyFilters() {
  closeFilter()
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

// 导航
function goHome() { router.push('/') }
function goDashboard() { router.push('/dashboard') }

// 初始化
onMounted(() => {
  const now = new Date()
  filterStart.value = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-01`
  filterEnd.value = now.toISOString().split('T')[0]
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
.chart-container {
  height: 280px;
  background: #fafbfc;
  border: 1px dashed #e2e8f0;
  border-radius: 6px;
}

.chart-placeholder {
  height: 280px;
  background: #f8fafc;
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
</style>
