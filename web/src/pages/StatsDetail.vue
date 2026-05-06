<template>
  <div class="min-h-screen stats-detail pt-24 md:pt-28" style="background-color:#F8FAFC !important;color:#1E293B">
    <div class="max-w-5xl mx-auto p-6 space-y-6">
      <div class="flex items-center justify-between">
        <div class="flex items-center gap-3">
          <button class="btn btn-secondary" @click="goBack">返回</button>
          <h2 class="text-lg font-semibold" style="color:#1E293B">短链详情：{{ shortCode }}</h2>
        </div>
        <div class="flex items-center gap-3">
          <button class="btn btn-secondary" :disabled="!hasAnyData" @click="exportCsv">导出CSV</button>
        </div>
      </div>

      <!-- 无数据提示条 -->
      <div v-if="noDataBanner" class="card" style="background:#FFFFFF !important">
        <div class="card-body flex items-center gap-3" style="background:#FFFFFF !important">
          <span class="inline-flex items-center justify-center w-6 h-6 rounded-full" style="background:#E6F0FF; color:#2563EB">i</span>
          <div>
            <div class="font-semibold" style="color:#1E293B">暂无点击数据</div>
            <div class="text-sm" style="color: var(--tf-text-muted)">所选日期内没有记录，您可以更换日期或查看趋势概览。</div>
          </div>
        </div>
      </div>

      <div class="card" style="background:#FFFFFF !important">
        <div class="card-header" style="background:#FFFFFF !important;color:#1E293B">选择时间</div>
        <div class="card-body flex items-center gap-4" style="background:#FFFFFF !important">
          <input type="date" v-model="selectedDate" class="form-input" />
          <button class="btn btn-primary" @click="applyDate">应用</button>
        </div>
      </div>

      <div class="card" style="background:#FFFFFF !important">
        <div class="card-header" style="background:#FFFFFF !important;color:#1E293B">当天趋势（小范围）</div>
        <div class="card-body" style="background:#FFFFFF !important">
          <Suspense>
            <template v-if="trendLabels.length">
              <TrendChart :labels="trendLabels" :values="trendValues" :height="220" />
            </template>
            <template v-else>
              <div class="chart-placeholder">该时间段暂无趋势数据</div>
            </template>
            <template #fallback><div class="chart-placeholder">加载中...</div></template>
          </Suspense>
        </div>
      </div>

      <div class="card" style="background:#FFFFFF !important">
        <div class="card-header" style="background:#FFFFFF !important;color:#1E293B">访问事件（共 {{ eventsList.length }} 条）</div>
        <div class="card-body p-0" style="background:#FFFFFF !important">
          <div class="events-table-container" style="background:#FFFFFF !important">
            <table class="events-table" style="background:#FFFFFF !important">
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
import { ref, onMounted, computed } from 'vue'
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

// 是否有任何数据（用于控制导出按钮等）
const hasAnyData = computed(() => {
  const eventsHas = Array.isArray(eventsList.value) && eventsList.value.length > 0
  const trendHas = Array.isArray(trendValues.value) && trendValues.value.some(v => Number(v) > 0)
  return eventsHas || trendHas
})

// 顶部无数据提示：当两侧都为空或为0时显示
const noDataBanner = computed(() => {
  const eventsEmpty = !eventsList.value || eventsList.value.length === 0
  const trendEmptyOrZero = !trendValues.value || trendValues.value.length === 0 || trendValues.value.every(v => Number(v) === 0)
  return eventsEmpty && trendEmptyOrZero
})

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
/* 统一蓝白企业冷静风 */
.stats-detail { background: var(--tf-bg-page) !important; color: var(--tf-text-body) !important; }

.card { background: var(--tf-bg-card) !important; border-radius: 12px; border: 1px solid var(--tf-border); overflow: hidden; box-shadow: var(--tf-shadow-card); }
.card-header { padding: 12px 16px; font-weight: 600; border-bottom: 1px solid var(--tf-divider); background: var(--tf-bg-card) !important; color: var(--tf-text-title) !important; }
.card-body { padding: 16px; background: var(--tf-bg-card) !important; color: var(--tf-text-body) !important; }

.form-input { padding: 8px 10px; border: 1px solid var(--tf-border); border-radius: 8px; background: #FFFFFF !important; color: var(--tf-text-body) !important; }

.btn { padding: 8px 12px; border-radius: 8px; background: #FFFFFF !important; color: var(--tf-text-body) !important; border: 1px solid var(--tf-border); }
.btn[disabled] { opacity: 0.6; cursor: not-allowed; }
.btn:hover { background: var(--tf-bg-hover) !important; }
.btn-primary { background: var(--tf-brand-primary) !important; color: #FFFFFF !important; border: none; }
.btn-primary:hover { background: var(--tf-brand-primary-hover) !important; }

.events-table-container { background: var(--tf-bg-card) !important; }
.events-table { width: 100%; background: var(--tf-bg-card) !important; color: var(--tf-text-body) !important; }
.events-table th { background: var(--tf-bg-page) !important; color: var(--tf-text-muted) !important; padding: 12px; text-align: left; font-weight: 500; font-size: 13px; border-bottom: 1px solid var(--tf-border); }
.events-table td { padding: 12px; border-bottom: 1px solid var(--tf-divider); font-size: 13px; color: var(--tf-text-body) !important; background: var(--tf-bg-card) !important; }
.events-table tbody tr:hover { background: var(--tf-bg-hover) !important; }

.empty-state { text-align: center; padding: 24px; color: var(--tf-text-muted) !important; }
.chart-placeholder { text-align: center; padding: 40px; color: var(--tf-text-muted); }
</style>
