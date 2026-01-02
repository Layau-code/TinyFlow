<template>
  <div class="min-h-screen stats-detail pt-24 md:pt-28" style="background-color: #F8FAFC;">
    <div class="max-w-5xl mx-auto p-6 space-y-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button class="btn btn-secondary" @click="goBack">返回</button>
          <h2 class="text-lg font-semibold">短链详情：{{ shortCode }}</h2>
        </div>
        <div class="flex items-center gap-3">
          <button class="btn btn-secondary" @click="exportCsv">导出CSV</button>
        </div>
      </div>

      <div class="card">
        <div class="card-header">选择时间</div>
        <div class="card-body flex items-center gap-4">
          <input type="date" v-model="selectedDate" class="form-input" />
          <button class="btn btn-primary" @click="applyDate">应用</button>
        </div>
      </div>

      <div class="card">
        <div class="card-header">当天趋势（小范围）</div>
        <div class="card-body">
          <Suspense>
            <TrendChart v-if="trendLabels.length" :labels="trendLabels" :values="trendValues" :height="220" />
            <template #fallback><div class="chart-placeholder">加载中...</div></template>
          </Suspense>
        </div>
      </div>

      <div class="card">
        <div class="card-header">访问事件（共 {{ eventsList.length }} 条）</div>
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
                <tr v-for="(e, idx) in eventsList" :key="idx">
                  <td>{{ formatDate(e.ts) }}</td>
                  <td>{{ e.ip || '-' }}</td>
                  <td>{{ e.deviceType || '-' }}</td>
                  <td>{{ e.city || '-' }}</td>
                  <td>{{ e.country || '-' }}</td>
                  <td class="truncate max-w-[200px]">{{ e.sourceHost || '-' }}</td>
                </tr>
                <tr v-if="!eventsList.length">
                  <td colspan="6" class="empty-state">暂无访问记录</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>

    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import axios from 'axios'
import { API_BASE } from '../composables/shortBase'
import { defineAsyncComponent } from 'vue'

const TrendChart = defineAsyncComponent(() => import('../components/charts/TrendEChartsLine.vue'))

const route = useRoute()
const router = useRouter()
const shortCode = route.params.shortCode

const selectedDate = ref(route.query.date || '')

const eventsList = ref([])
const trendLabels = ref([])
const trendValues = ref([])

function formatDate(ts){ if (!ts) return '-'; try { return new Date(ts).toLocaleString('zh-CN') } catch { return String(ts) } }

async function fetchEventsForDate(date){
  try{
    const body = { code: shortCode, start: date, end: date, page: 0, size: 1000 }
    const res = await axios.post(`${API_BASE}/api/stats/events`, body)
    eventsList.value = Array.isArray(res.data) ? res.data : []
  }catch(e){ console.error('fetchEventsForDate error', e); eventsList.value = [] }
}

async function fetchTrendForDate(){
  try{
    // 后端可能不支持按 end 参数，这里请求短期趋势（7天）作为示例
    const res = await axios.get(`${API_BASE}/api/stats/trend/${encodeURIComponent(shortCode)}?days=7`)
    const data = Array.isArray(res.data) ? res.data : []
    const labels = data.map(d => d.date).sort()
    trendLabels.value = labels
    trendValues.value = labels.map(l => {
      const item = data.find(d => d.date === l)
      return item ? Number(item.visits || item.count || 0) : 0
    })
  }catch(e){ console.error('fetchTrendForDate error', e); trendLabels.value = []; trendValues.value = [] }
}

function goBack(){ router.push({ path: `/stats/${encodeURIComponent(shortCode)}` }) }

function applyDate(){
  // 将日期写入 URL 并刷新数据
  router.replace({ path: `/stats/${encodeURIComponent(shortCode)}/detail`, query: { date: selectedDate.value } })
  refresh()
}

async function refresh(){
  const d = selectedDate.value || ''
  await Promise.all([fetchEventsForDate(d), fetchTrendForDate(d)])
}

async function exportCsv(){
  try{
    const body = { code: shortCode, start: selectedDate.value, end: selectedDate.value }
    const res = await axios.post(`${API_BASE}/api/stats/export?format=csv`, body, { responseType: 'blob' })
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = `stats-${shortCode}-${selectedDate.value || 'all'}.csv`
    a.click()
    URL.revokeObjectURL(url)
  }catch(e){ console.error('export csv error', e) }
}

onMounted(()=>{ refresh() })
</script>

<style scoped>
/* 强制使用白色背景，避免暗色模式影响 */
.stats-detail {
  background: #F8FAFC !important;
  color: #1E293B;
}

.card {
  background: white;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  overflow: hidden;
}

.card-header {
  padding: 12px 16px;
  font-weight: 600;
  border-bottom: 1px solid #f1f5f9;
  background: white;
  color: #1E293B;
}

.card-body {
  padding: 16px;
  background: white;
}

.form-input {
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 6px;
  background: white;
  color: #1E293B;
}

.btn {
  padding: 8px 12px;
  border-radius: 6px;
  background: white;
  color: #1E293B;
  border: 1px solid #e2e8f0;
}

.btn:hover {
  background: #F1F5F9;
}

/* 表格样式 */
.events-table-container {
  background: white;
}

.events-table {
  width: 100%;
  background: white;
}

.events-table th {
  background: #F8FAFC;
  color: #64748B;
  padding: 12px;
  text-align: left;
  font-weight: 600;
  border-bottom: 1px solid #e2e8f0;
}

.events-table td {
  padding: 12px;
  color: #1E293B;
  border-bottom: 1px solid #f1f5f9;
}

.events-table tr:hover td {
  background: #F8FAFC;
}

.empty-state {
  padding: 40px;
  text-align: center;
  color: #64748B;
}
</style>
