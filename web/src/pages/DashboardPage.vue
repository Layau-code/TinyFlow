<template>
  <div class="dashboard-container" v-loading="loading">
    <!-- 顶部标题栏 -->
    <div class="dashboard-header">
      <div>
        <h1 class="page-title">数据看板</h1>
        <p class="page-subtitle">实时监控短链访问数据 · 最后更新: {{ lastUpdateTime }}</p>
      </div>
      <div class="header-actions">
        <!-- 时间维度切换 -->
        <div class="time-selector">
          <button 
            v-for="period in timePeriods" 
            :key="period.value"
            :class="['time-btn', { active: selectedPeriod === period.value }]"
            @click="changePeriod(period.value)">
            {{ period.label }}
          </button>
        </div>
        <button @click="refreshAll" class="btn-icon" :class="{ rotating: loading }">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21.5 2v6h-6M2.5 22v-6h6M2 11.5a10 10 0 0 1 18.8-4.3M22 12.5a10 10 0 0 1-18.8 4.2"/>
          </svg>
        </button>
        <button @click="exportCSV" class="btn-icon">
          <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3"/>
          </svg>
        </button>
      </div>
    </div>

    <!-- 核心指标卡片 -->
    <div class="metrics-section">
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">总链接数</span>
          <div class="metric-icon-circle blue">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71"/>
              <path d="M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71"/>
            </svg>
          </div>
        </div>
        <div class="metric-value">{{ formatNumber(globalStats?.totalUrls ?? 0) }}</div>
        <div class="metric-footer">系统总计</div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">总访问量 (PV)</span>
          <div class="metric-icon-circle green">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
              <circle cx="12" cy="12" r="3"/>
            </svg>
          </div>
        </div>
        <div class="metric-value">{{ formatNumber(globalStats?.totalClicks ?? 0) }}</div>
        <div class="metric-footer">累计点击</div>
      </div>

      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">独立访客 (UV)</span>
          <div class="metric-icon-circle purple">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
              <circle cx="9" cy="7" r="4"/>
              <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
            </svg>
          </div>
        </div>
        <div class="metric-value">{{ formatNumber(globalStats?.totalUniqueIps ?? 0) }}</div>
        <div class="metric-footer">独立 IP 数</div>
      </div>

      <div class="metric-card highlight">
        <div class="metric-header">
          <span class="metric-label">今日访问</span>
          <div class="metric-icon-circle orange">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z"/>
            </svg>
          </div>
        </div>
        <div class="metric-value">{{ formatNumber(globalStats?.todayClicks ?? 0) }}</div>
        <div class="metric-footer">实时数据</div>
      </div>
    </div>

    <!-- 访问趋势图 -->
    <div class="chart-card">
      <div class="card-header">
        <h3 class="card-title">访问趋势</h3>
        <div class="card-actions">
          <button 
            :class="['time-filter-btn', { active: trendDays === 7 }]" 
            @click="changeTrendDays(7)">
            近7天
          </button>
          <button 
            :class="['time-filter-btn', { active: trendDays === 30 }]" 
            @click="changeTrendDays(30)">
            近30天
          </button>
        </div>
      </div>
      <div class="card-body">
        <div class="trend-chart-container" ref="trendChart"></div>
        <div v-if="trendInsight" class="chart-insight">
          <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="12" cy="12" r="10"/>
            <path d="M12 16v-4M12 8h.01"/>
          </svg>
          <span>{{ trendInsight }}</span>
        </div>
      </div>
    </div>

    <!-- 热门链接 TOP 10 -->
    <div class="chart-card">
      <div class="card-header">
        <h3 class="card-title">热门链接 TOP 10</h3>
        <input 
          v-model="searchQuery" 
          type="text" 
          placeholder="搜索短链..." 
          class="search-input-small" 
        />
      </div>
      <div class="card-body">
        <div class="top-links-grid">
          <div 
            v-for="(item, index) in topLinks" 
            :key="item.shortCode" 
            class="top-link-item"
            @click="viewStats(item)">
            <div class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</div>
            <div class="link-info">
              <div class="link-code">{{ item.shortCode }}</div>
              <div class="link-url">{{ truncate(item.longUrl, 40) }}</div>
            </div>
            <div class="link-stats">
              <div class="stat-value">{{ formatNumber(item.clickCount) }}</div>
              <div class="stat-label">访问量</div>
            </div>
          </div>
        </div>
        <div v-if="topLinks.length === 0" class="empty-state">
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
            <line x1="9" y1="9" x2="15" y2="15"/>
            <line x1="15" y1="9" x2="9" y2="15"/>
          </svg>
          <p>暂无数据</p>
        </div>
      </div>
    </div>

    <!-- 详细数据表格 -->
    <div class="chart-card">
      <div class="card-header">
        <h3 class="card-title">所有短链</h3>
        <div class="card-actions">
          <span class="data-count">共 {{ filteredUrls.length }} 条</span>
        </div>
      </div>
      <div class="card-body">
        <div class="data-table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>短码</th>
                <th>原始链接</th>
                <th>访问量</th>
                <th>创建时间</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in paginatedUrls" :key="item.id" @click="viewStats(item)" class="table-row">
                <td>
                  <code class="code-badge">{{ item.shortCode }}</code>
                </td>
                <td class="url-cell">{{ truncate(item.longUrl, 60) }}</td>
                <td class="number-cell">{{ formatNumber(item.clickCount) }}</td>
                <td class="date-cell">{{ formatDate(item.createdAt) }}</td>
                <td>
                  <button @click.stop="viewStats(item)" class="btn-link">详情</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="filteredUrls.length === 0" class="empty-state">
            <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.35-4.35"/>
            </svg>
            <p>未找到匹配的短链</p>
          </div>
        </div>
        
        <!-- 分页 -->
        <div v-if="totalPages > 1" class="pagination">
          <button @click="prevPage" :disabled="currentPage === 1" class="pagination-btn">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="15 18 9 12 15 6"/>
            </svg>
            上一页
          </button>
          <span class="pagination-info">第 {{ currentPage }} / {{ totalPages }} 页</span>
          <button @click="nextPage" :disabled="currentPage === totalPages" class="pagination-btn">
            下一页
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <polyline points="9 18 15 12 9 6"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import axios from 'axios'
import * as echarts from 'echarts'

const router = useRouter()

// 数据状态
const globalStats = ref({})
const allUrls = ref([])
const trendData = ref([])
const searchQuery = ref('')
const trendDays = ref(7)
const currentPage = ref(1)
const pageSize = 20
const trendChart = ref(null)
let chartInstance = null

// 计算属性
const filteredUrls = computed(() => {
  const query = searchQuery.value.toLowerCase().trim()
  if (!query) return allUrls.value
  return allUrls.value.filter(item => 
    item.shortCode?.toLowerCase().includes(query) ||
    item.longUrl?.toLowerCase().includes(query)
  )
})

const paginatedUrls = computed(() => {
  const start = (currentPage.value - 1) * pageSize
  const end = start + pageSize
  return filteredUrls.value.slice(start, end)
})

const totalPages = computed(() => Math.ceil(filteredUrls.value.length / pageSize))

const topLinks = computed(() => {
  return [...allUrls.value]
    .sort((a, b) => (b.clickCount || 0) - (a.clickCount || 0))
    .slice(0, 10)
})

const trendInsight = computed(() => {
  if (trendData.value.length < 2) return ''
  const last = trendData.value[trendData.value.length - 1]?.visits || 0
  const prev = trendData.value[trendData.value.length - 2]?.visits || 0
  const change = last - prev
  const percent = prev === 0 ? 0 : ((change / prev) * 100).toFixed(1)
  
  if (change > 0) return `相比昨日增长 ${percent}%，访问量呈上升趋势`
  if (change < 0) return `相比昨日下降 ${Math.abs(percent)}%，建议关注链接活跃度`
  return '访问量保持稳定'
})

// 工具函数
const formatNumber = (num) => {
  if (num >= 10000) return (num / 10000).toFixed(1) + 'w'
  if (num >= 1000) return (num / 1000).toFixed(1) + 'k'
  return num?.toString() || '0'
}

const truncate = (str, len) => {
  if (!str) return '-'
  return str.length > len ? str.substring(0, len) + '...' : str
}

const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

// 数据加载
const fetchGlobalStats = async () => {
  try {
    const res = await axios.get('/api/stats/global')
    globalStats.value = res.data || {}
  } catch (err) {
    console.error('获取全局统计失败:', err)
  }
}

const fetchAllUrls = async () => {
  try {
    const res = await axios.get('/api/urls', { params: { page: 0, size: 1000 } })
    allUrls.value = res.data?.content || res.data || []
  } catch (err) {
    console.error('获取链接列表失败:', err)
  }
}

const fetchTrendData = async (days = 7) => {
  try {
    const res = await axios.get(`/api/stats/trend/all?days=${days}`)
    trendData.value = res.data || []
    await nextTick()
    renderTrendChart()
  } catch (err) {
    console.error('获取趋势数据失败:', err)
  }
}

const renderTrendChart = () => {
  if (!trendChart.value) return
  
  if (!chartInstance) {
    chartInstance = echarts.init(trendChart.value)
  }

  const dates = trendData.value.map(d => d.date)
  const visits = trendData.value.map(d => d.visits || 0)

  const option = {
    grid: { left: '3%', right: '3%', bottom: '10%', top: '10%', containLabel: true },
    xAxis: {
      type: 'category',
      data: dates,
      axisLine: { lineStyle: { color: '#E2E8F0' } },
      axisLabel: { color: '#64748B', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: { lineStyle: { color: '#F1F5F9' } },
      axisLabel: { color: '#64748B', fontSize: 12 }
    },
    series: [{
      data: visits,
      type: 'line',
      smooth: true,
      symbol: 'circle',
      symbolSize: 6,
      lineStyle: { color: '#3370FF', width: 3 },
      itemStyle: { color: '#3370FF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(51, 112, 255, 0.3)' },
          { offset: 1, color: 'rgba(51, 112, 255, 0.05)' }
        ])
      }
    }],
    tooltip: {
      trigger: 'axis',
      backgroundColor: '#fff',
      borderColor: '#E2E8F0',
      borderWidth: 1,
      textStyle: { color: '#1E293B' },
      formatter: (params) => {
        const p = params[0]
        return `${p.axisValue}<br/>访问量: <strong>${p.value}</strong>`
      }
    }
  }

  chartInstance.setOption(option)
}

const changeTrendDays = (days) => {
  trendDays.value = days
  fetchTrendData(days)
}

const refreshAll = async () => {
  await Promise.all([
    fetchGlobalStats(),
    fetchAllUrls(),
    fetchTrendData(trendDays.value)
  ])
}

const exportCSV = () => {
  const headers = ['短码', '原始链接', '访问量', '创建时间']
  const rows = allUrls.value.map(item => [
    item.shortCode,
    item.longUrl,
    item.clickCount || 0,
    formatDate(item.createdAt)
  ])
  
  let csv = headers.join(',') + '\n'
  rows.forEach(row => {
    csv += row.map(cell => `"${cell}"`).join(',') + '\n'
  })
  
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const link = document.createElement('a')
  link.href = url
  link.download = `tinyflow-stats-${new Date().toISOString().split('T')[0]}.csv`
  link.click()
  URL.revokeObjectURL(url)
}

const viewStats = (item) => {
  router.push(`/stats/${item.shortCode}`)
}

const prevPage = () => {
  if (currentPage.value > 1) currentPage.value--
}

const nextPage = () => {
  if (currentPage.value < totalPages.value) currentPage.value++
}

onMounted(() => {
  refreshAll()
  window.addEventListener('resize', () => {
    chartInstance?.resize()
  })
})
</script>

<style scoped>
.dashboard-container {
  min-height: 100vh;
  background: #F8FAFC;
  padding: 96px 24px 40px;
}

.dashboard-header {
  max-width: 1400px;
  margin: 0 auto 32px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-title {
  font-size: 28px;
  font-weight: 600;
  color: #1E293B;
  margin: 0 0 4px 0;
}

.page-subtitle {
  font-size: 14px;
  color: #64748B;
  margin: 0;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.btn-secondary {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 8px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  border-color: #3370FF;
  color: #3370FF;
  background: #F8FAFC;
}

/* 核心指标卡片 */
.metrics-section {
  max-width: 1400px;
  margin: 0 auto 24px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.metric-card {
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  padding: 24px;
  transition: all 0.2s;
}

.metric-card:hover {
  box-shadow: 0 4px 12px rgba(51, 112, 255, 0.08);
  transform: translateY(-2px);
}

.metric-card.highlight {
  background: linear-gradient(135deg, #3370FF 0%, #5B8EFF 100%);
  border: none;
  color: white;
}

.metric-card.highlight .metric-label,
.metric-card.highlight .metric-value,
.metric-card.highlight .metric-footer {
  color: white;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.metric-label {
  font-size: 14px;
  color: #64748B;
  font-weight: 500;
}

.metric-icon-circle {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.metric-icon-circle.blue { background: #EFF6FF; color: #3B82F6; }
.metric-icon-circle.green { background: #F0FDF4; color: #22C55E; }
.metric-icon-circle.purple { background: #F5F3FF; color: #8B5CF6; }
.metric-icon-circle.orange { background: #FFF7ED; color: #F97316; }

.metric-value {
  font-size: 36px;
  font-weight: 700;
  color: #1E293B;
  margin-bottom: 8px;
  font-variant-numeric: tabular-nums;
}

.metric-footer {
  font-size: 13px;
  color: #94A3B8;
}

/* 图表卡片 */
.chart-card {
  max-width: 1400px;
  margin: 0 auto 24px;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 12px;
  overflow: hidden;
}

.card-header {
  padding: 20px 24px;
  border-bottom: 1px solid #F1F5F9;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 18px;
  font-weight: 600;
  color: #1E293B;
  margin: 0;
}

.card-actions {
  display: flex;
  gap: 8px;
  align-items: center;
}

.time-filter-btn {
  padding: 6px 14px;
  border: 1px solid #E2E8F0;
  background: white;
  border-radius: 6px;
  font-size: 13px;
  color: #64748B;
  cursor: pointer;
  transition: all 0.2s;
}

.time-filter-btn:hover {
  border-color: #3370FF;
  color: #3370FF;
}

.time-filter-btn.active {
  background: #3370FF;
  border-color: #3370FF;
  color: white;
}

.search-input-small {
  padding: 6px 12px;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  font-size: 13px;
  color: #1E293B;
  width: 200px;
  transition: all 0.2s;
}

.search-input-small:focus {
  outline: none;
  border-color: #3370FF;
  box-shadow: 0 0 0 3px rgba(51, 112, 255, 0.1);
}

.card-body {
  padding: 24px;
}

.trend-chart-container {
  height: 320px;
  width: 100%;
}

.chart-insight {
  margin-top: 16px;
  padding: 12px 16px;
  background: #F8FAFC;
  border-left: 3px solid #3370FF;
  border-radius: 6px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #475569;
}

/* 热门链接 */
.top-links-grid {
  display: grid;
  gap: 12px;
}

.top-link-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #F8FAFC;
  border: 1px solid #F1F5F9;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s;
}

.top-link-item:hover {
  background: white;
  border-color: #3370FF;
  box-shadow: 0 2px 8px rgba(51, 112, 255, 0.1);
}

.rank-badge {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  background: #E2E8F0;
  color: #64748B;
  flex-shrink: 0;
}

.rank-badge.rank-gold { background: #FEF3C7; color: #F59E0B; }
.rank-badge.rank-silver { background: #F1F5F9; color: #64748B; }
.rank-badge.rank-bronze { background: #FED7AA; color: #EA580C; }

.link-info {
  flex: 1;
  min-width: 0;
}

.link-code {
  font-size: 14px;
  font-weight: 600;
  color: #3370FF;
  font-family: 'Monaco', 'Courier New', monospace;
  margin-bottom: 4px;
}

.link-url {
  font-size: 13px;
  color: #94A3B8;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.link-stats {
  text-align: right;
  flex-shrink: 0;
}

.stat-value {
  font-size: 20px;
  font-weight: 700;
  color: #1E293B;
  font-variant-numeric: tabular-nums;
}

.stat-label {
  font-size: 12px;
  color: #94A3B8;
  margin-top: 2px;
}

/* 数据表格 */
.data-table-container {
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.data-table thead th {
  background: #F8FAFC;
  color: #64748B;
  font-weight: 600;
  text-align: left;
  padding: 12px 16px;
  border-bottom: 1px solid #E2E8F0;
  white-space: nowrap;
}

.data-table tbody tr {
  border-bottom: 1px solid #F1F5F9;
  cursor: pointer;
  transition: background 0.2s;
}

.data-table tbody tr:hover {
  background: #F8FAFC;
}

.data-table tbody td {
  padding: 14px 16px;
  color: #1E293B;
}

.code-badge {
  display: inline-block;
  padding: 4px 8px;
  background: #EFF6FF;
  border: 1px solid #DBEAFE;
  border-radius: 4px;
  color: #3370FF;
  font-family: 'Monaco', 'Courier New', monospace;
  font-size: 13px;
}

.url-cell {
  max-width: 400px;
  color: #64748B;
}

.number-cell {
  font-variant-numeric: tabular-nums;
  font-weight: 600;
  color: #3370FF;
}

.date-cell {
  color: #94A3B8;
  font-size: 13px;
}

.btn-link {
  padding: 4px 12px;
  background: transparent;
  border: 1px solid #E2E8F0;
  border-radius: 4px;
  color: #3370FF;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-link:hover {
  background: #EFF6FF;
  border-color: #3370FF;
}

/* 分页 */
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #F1F5F9;
}

.pagination-btn {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  background: white;
  border: 1px solid #E2E8F0;
  border-radius: 6px;
  font-size: 14px;
  color: #475569;
  cursor: pointer;
  transition: all 0.2s;
}

.pagination-btn:hover:not(:disabled) {
  border-color: #3370FF;
  color: #3370FF;
}

.pagination-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.pagination-info {
  font-size: 14px;
  color: #64748B;
}

.data-count {
  font-size: 13px;
  color: #94A3B8;
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #94A3B8;
}

.empty-state svg {
  margin-bottom: 16px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

@media (max-width: 768px) {
  .dashboard-container {
    padding: 80px 16px 24px;
  }
  
  .dashboard-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .metrics-section {
    grid-template-columns: 1fr;
  }
  
  .top-link-item {
    flex-wrap: wrap;
  }
}
</style>
