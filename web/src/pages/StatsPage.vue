<template>
  <div class="min-h-screen q-page pt-24 md:pt-28">
    <div class="max-w-6xl mx-auto p-6 space-y-10">
      <!-- Breadcrumb & Actions -->
      <div class="flex items-center justify-between">
        <div class="q-muted flex items-center gap-2">
          <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
            <path d="M3 10.5L12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
          </svg>
          <span>{{ $t('stats.breadcrumb', { code: shortCode }) }}</span>
        </div>
        <div class="flex items-center gap-3">
          <button @click="goHome" class="fx-btn fx-gray">
            <span></span><span></span><span></span><span></span><span></span><span class="btn-text">返回首页</span>
          </button>
          <button @click="goDashboard" class="fx-btn fx-gray">
            <span></span><span></span><span></span><span></span><span></span><span class="btn-text">返回看板</span>
          </button>
          <button @click="copy(shortUrl)" class="fx-btn fx-gray">
            <span></span><span></span><span></span><span></span><span></span><span class="btn-text">复制短链</span>
          </button>
          <button @click="openFilter" class="fx-btn fx-purple">
            <span></span><span></span><span></span><span></span><span></span><span class="btn-text">筛选</span>
          </button>
        </div>
      </div>

      

      <!-- 近七天访问趋势（折线图） -->
      <div class="grid grid-cols-1 gap-8">
      <div class="q-card p-5">
          <div class="q-card-title mb-3 flex items-center gap-2">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M4 17l6-6 4 4 6-7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>访问趋势</span>
            <div class="ml-auto flex items-center gap-2">
              <button class="fx-btn fx-gray fx-sm" :class="{ 'font-semibold': selectedDays===7 }" @click="selectDays(7)"><span></span><span></span><span></span><span></span><span></span><span class="btn-text">7天</span></button>
              <button class="fx-btn fx-purple fx-sm" :class="{ 'font-semibold': selectedDays===30 }" @click="selectDays(30)"><span></span><span></span><span></span><span></span><span></span><span class="btn-text">30天</span></button>
            </div>
          </div>
          <div class="relative" style="height:260px;background:#fafafa;border:1px dashed #ddd;border-radius:8px">
            <Suspense>
              <TrendChart :values="trendValues" :labels="trendLabels" :showValues="true" />
              <template #fallback><div class="h-[220px] q-card"></div></template>
            </Suspense>
          </div>
          <div v-if="!trendLabels.length && !loadingTrend" class="q-muted text-center mt-2">{{ $t('common.noData') }}</div>
        </div>
      </div>

      <!-- 顶部：设备饼图 + 来源分布 -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-8">
        <div class="q-card p-5">
          <div class="q-card-title mb-3">来源渠道占比</div>
          <Suspense>
            <SourceHBarChart :data="refererBars" />
            <template #fallback><div class="h-[240px] q-card"></div></template>
          </Suspense>
          <div v-if="!refererBars.length && !loadingDist" class="q-muted">{{ $t('common.noData') }}</div>
        </div>
        <div class="q-card p-5">
          <div class="q-card-title mb-3">设备占比</div>
          <Suspense>
            <DistributionChart :type="'pie'" :data="deviceChartData" :engine="'echarts'" />
            <template #fallback><div class="h-[240px] q-card"></div></template>
          </Suspense>
          <div v-if="!hasDeviceChartData && !loadingDist" class="q-muted">{{ $t('common.noData') }}</div>
        </div>
        
      </div>

      

      <!-- 筛选弹窗 -->
      <div v-if="showFilter" class="tf-modal-backdrop">
        <div class="tf-modal">
          <div class="tf-modal-header">筛选条件</div>
          <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label class="q-muted text-[13px]">开始日期</label>
              <input v-model="start" type="date" class="q-input mt-1 w-full" />
            </div>
            <div>
              <label class="q-muted text-[13px]">结束日期</label>
              <input v-model="end" type="date" class="q-input mt-1 w-full" />
            </div>
            <div>
              <label class="q-muted text-[13px]">来源</label>
              <input v-model="source" type="text" placeholder="不填为全部" class="q-input mt-1 w-full" />
            </div>
            <div>
              <label class="q-muted text-[13px]">设备</label>
              <select v-model="device" class="q-input mt-1 w-full">
                <option value="">全部</option>
                <option value="mobile">移动</option>
                <option value="desktop">桌面</option>
                <option value="tablet">平板</option>
                <option value="bot">爬虫</option>
              </select>
            </div>
            <div>
              <label class="q-muted text-[13px]">城市</label>
              <input v-model="city" type="text" placeholder="不填为全部" class="q-input mt-1 w-full" />
            </div>
          </div>
          <div class="mt-6 flex items-center justify-end gap-3">
            <button class="fx-btn fx-gray fx-sm" @click="resetFilters"><span></span><span></span><span></span><span></span><span></span><span class="btn-text">重置</span></button>
            <button class="fx-btn fx-purple fx-sm" @click="confirmFilters"><span></span><span></span><span></span><span></span><span></span><span class="btn-text">应用筛选</span></button>
            <button class="fx-btn fx-gray fx-sm" @click="closeFilter"><span></span><span></span><span></span><span></span><span></span><span class="btn-text">取消</span></button>
          </div>
        </div>
      </div>

      <!-- 关键指标卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <div class="stat-card">
          <div class="stat-label">总访问量</div>
          <div class="stat-value">{{ overview?.totalVisits ?? '-' }}</div>
          <div class="stat-unit">次</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">今日访问量</div>
          <div class="stat-value">{{ overview?.todayVisits ?? '-' }}</div>
          <div class="stat-unit">次</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">创建时间</div>
          <div class="stat-value-text">{{ formatDate(overview?.createdAt) }}</div>
        </div>
        <div class="stat-card">
          <div class="stat-label">访问转化</div>
          <div class="stat-value">{{ overview?.totalVisits ? Math.round((overview.todayVisits / overview.totalVisits) * 100) : 0 }}%</div>
          <div class="stat-unit">今日占比</div>
        </div>
      </div>

      <!-- 短链详情与Top城市 -->
      <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div class="col-span-1 md:col-span-2">
          <div class="q-card p-6">
            <div class="q-card-title mb-4">短链信息</div>
            <div class="space-y-4">
              <div class="detail-row">
                <span class="detail-label">短码</span>
                <code class="detail-value code-block">{{ shortCode }}</code>
              </div>
              <div class="detail-row">
                <span class="detail-label">短链地址</span>
                <a :href="shortUrl" target="_blank" class="detail-value link-style">{{ shortLabel }}</a>
              </div>
              <div class="detail-row">
                <span class="detail-label">原始链接</span>
                <div class="mt-2 text-xs font-mono bg-gray-100 p-2 rounded break-all text-gray-600">{{ overview?.longUrl || '-' }}</div>
              </div>
            </div>
          </div>
        </div>
        <div>
          <div class="q-card p-6">
            <div class="q-card-title mb-4">城市 Top 5</div>
            <div class="space-y-2">
              <div v-for="(c, idx) in cityData" :key="c.label" class="city-item">
                <div class="flex items-center justify-between">
                  <span class="city-rank">{{ idx + 1 }}</span>
                  <span class="city-name flex-1 ml-3">{{ c.label }}</span>
                  <span class="city-count">{{ c.value }}</span>
                </div>
                <div class="city-bar">
                  <div class="city-bar-fill" :style="{ width: (c.value / (cityData[0]?.value || 1) * 100) + '%' }"></div>
                </div>
              </div>
              <div v-if="!cityData.length" class="text-center py-4 text-gray-400">{{ $t('common.noData') }}</div>
            </div>
          </div>
        </div>
      </div>

      <!-- Error -->
      <div v-if="errorMsg" class="p-4 rounded q-card">
        {{ errorMsg }}
        <button class="ml-3 q-btn-ghost px-3 py-1" @click="refreshAll">{{ $t('stats.retry') }}</button>
        <button class="ml-3 q-btn-ghost px-3 py-1" @click="exportCsv">导出 CSV</button>
        <button class="ml-3 q-btn-ghost px-3 py-1" @click="exportJson">导出 JSON</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineAsyncComponent, computed, watch } from 'vue'
  import { useI18n } from 'vue-i18n'
import { useFetchOverview, useFetchDistribution, useFetchTrend, exportStats } from '../composables/useStats'
  import { SHORT_BASE } from '../composables/shortBase'
const DistributionChart = defineAsyncComponent(() => import('../components/DistributionChart.vue'))
const TrendChart = defineAsyncComponent(() => import('../components/TrendChart.vue'))
const SourceHBarChart = defineAsyncComponent(() => import('../components/charts/SourceHBarChart.vue'))
/* DevicePieChart removed in favor of unified DistributionChart pie */
const CityBarChart = defineAsyncComponent(() => import('../components/charts/CityBarChart.vue'))

import { useRoute, useRouter } from 'vue-router'
import { useClipboard } from '../composables/useStats'
const route = useRoute()
const router = useRouter()
const shortCode = route.params.shortCode
const shortUrl = `${SHORT_BASE}/${encodeURIComponent(shortCode)}`
const shortLabel = `${SHORT_BASE}/${String(shortCode)}`
const { copy } = useClipboard()
const { t } = useI18n()

const { data: overviewRef, loading: loadingOverview, error: errorOverview, refresh: refreshOverview } = useFetchOverview(shortCode)
const filters = ref({ start: '', end: '', source: '', device: '', city: '', page: 0, size: 20 })
const { data: distRef, loading: loadingDistRef, error: errorDist, refresh: refreshDist } = useFetchDistribution(shortCode, filters)
const selectedDays = ref(7)
const { data: trendRef, loading: loadingTrendRef, error: errorTrend, refresh: refreshTrend } = useFetchTrend(shortCode, selectedDays)

const overview = overviewRef
const deviceData = ref([])
const cityData = ref([])
const refererData = ref([])
const refererBars = ref([])
const deviceChartData = ref([])
const hasDeviceChartData = computed(() => (deviceChartData.value || []).reduce((a,b)=> a + Number(b.value||0), 0) > 0)
const cityBars = ref([])
const errorMsg = ref('')

const loadingDist = loadingDistRef
const loadingTrend = loadingTrendRef
const trendLabels = ref([])
const trendValues = ref([])

function formatDate(ts){ try { return new Date(ts).toLocaleString() } catch { return String(ts) } }

async function refreshAll(){
  errorMsg.value = ''
  await Promise.all([refreshOverview(), refreshDist(), refreshTrend()])
  if (errorOverview.value || errorDist.value || errorTrend.value) {
    errorMsg.value = t('stats.errorNotFound')
  }
  const dist = distRef.value || {}
  refererBars.value = (dist.referer||[]).map(kv => ({ source: kv.referer || kv.label || kv.name || t('common.unknown'), count: Number(kv.count || kv.value || 0) }))
  const devArr = (dist.device||[]).map(kv => ({ key: String(kv.device || kv.label || kv.name || 'unknown').toLowerCase(), count: Number(kv.count || kv.value || 0) }))
  const mobile = devArr.filter(d => d.key.includes('mobile') || d.key.includes('phone') || d.key.includes('ios') || d.key.includes('android')).reduce((a,b)=>a+b.count,0)
  const desktop = devArr.filter(d => d.key.includes('desktop') || d.key.includes('pc')).reduce((a,b)=>a+b.count,0)
  const tablet = devArr.filter(d => d.key.includes('tablet') || d.key.includes('pad')).reduce((a,b)=>a+b.count,0)
  deviceChartData.value = [
    { label: '移动', value: mobile },
    { label: '平板', value: tablet },
    { label: '桌面', value: desktop }
  ]
  cityBars.value = (dist.city||[]).slice(0,10).map(kv => ({ city: kv.city || kv.label || kv.name || t('common.unknown'), count: Number(kv.count || kv.value || 0) }))
  deviceData.value = (dist.device||[]).map(kv => ({ label: kv.label || kv.name || kv.key || t('common.unknown'), value: Number(kv.count || kv.value || 0) }))
  cityData.value = (dist.city||[]).slice(0,5).map(kv => ({ label: kv.city || kv.label || kv.name || t('common.unknown'), value: Number(kv.count || kv.value || 0) }))

  // 构建近7天趋势数据（labels 升序对齐，values 与 labels 一一对应）
  const trendList = Array.isArray(trendRef.value) ? trendRef.value : []
  const labels = Array.from(new Set(trendList.map(p => String(p.date)))).sort()
  trendLabels.value = labels
  const map = new Map(trendList.map(p => [String(p.date), Number(p.visits || p.count || 0)]))
  trendValues.value = labels.map(d => map.get(d) ?? 0)
}

function goHome(){ router.push('/') }
function goDashboard(){ router.push('/dashboard') }

// 移除图标与渐变，采用纯文本信息结构
function selectDays(d){ selectedDays.value = d }

watch(selectedDays, async () => {
  console.debug('[watch] selectedDays ->', selectedDays.value)
  await refreshTrend()
  await refreshAll()
})

function applyFilters(){
  filters.value = { start: start.value, end: end.value, source: source.value, device: device.value, city: city.value, page: 0, size: 20 }
  refreshAll()
}

async function exportCsv(){ await doExport('csv') }
async function exportJson(){ await doExport('json') }
async function doExport(fmt){
  const params = { start: start.value, end: end.value, source: source.value, device: device.value, city: city.value }
  await exportStats(shortCode, params, fmt)
}

const start = ref('')
const end = ref('')
const source = ref('')
const device = ref('')
const city = ref('')

const showFilter = ref(false)
function openFilter(){ showFilter.value = true }
function closeFilter(){ showFilter.value = false }
function confirmFilters(){ showFilter.value = false; applyFilters() }
function resetFilters(){ source.value=''; device.value=''; city.value='' }

onMounted(refreshAll)

;(function initDefaultDates(){
  try {
    const now = new Date()
    const y = now.getFullYear()
    const m = now.getMonth()
    const startDate = new Date(y, m, 1)
    const endDate = new Date(y, m + 1, 0)
    const fmt = (d) => {
      const yy = d.getFullYear()
      const mm = String(d.getMonth() + 1).padStart(2, '0')
      const dd = String(d.getDate()).padStart(2, '0')
      return `${yy}-${mm}-${dd}`
    }
    start.value = fmt(startDate)
    end.value = fmt(endDate)
  } catch {}
})()
</script>

<style scoped>
svg { width: 20px; height: 20px; }
.tf-modal-backdrop { position: fixed; inset: 0; background: rgba(4,16,40,0.35); display: flex; align-items: center; justify-content: center; z-index: 50; backdrop-filter: blur(2px); }
.tf-modal { width: 680px; max-width: 92vw; border-radius: 16px; padding: 24px; background: linear-gradient(135deg,#ffffff 0%, #f3f7ff 100%); box-shadow: 0 12px 40px rgba(37,99,235,0.22); border: 1px solid rgba(37,99,235,0.18); }
.tf-modal-header { font-size: 16px; font-weight: 600; color: #2563EB; margin-bottom: 16px; }

/* 统计卡片窗口 */
.stat-card {
  background: linear-gradient(135deg, #ffffff 0%, #f8fafc 100%);
  border: 1px solid rgba(226, 232, 240, 0.6);
  border-radius: 12px;
  padding: 20px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}
.stat-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(37, 99, 235, 0.15);
  border-color: rgba(37, 99, 235, 0.3);
}
.stat-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.stat-value {
  font-size: 32px;
  font-weight: 700;
  color: #1e293b;
  line-height: 1;
  margin-bottom: 4px;
}
.stat-value-text {
  font-size: 14px;
  color: #334155;
  font-weight: 500;
  word-break: break-all;
}
.stat-unit {
  font-size: 12px;
  color: #94a3b8;
  font-weight: 400;
}

/* 详情行样式 */
.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}
.detail-row:last-child {
  border-bottom: none;
}
.detail-label {
  min-width: 80px;
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
  flex-shrink: 0;
}
.detail-value {
  font-size: 14px;
  color: #1e293b;
  flex: 1;
  word-break: break-all;
}
.code-block {
  background: #f1f5f9;
  padding: 6px 10px;
  border-radius: 6px;
  color: #2563eb;
  font-weight: 500;
  font-size: 13px;
}
.link-style {
  color: #2563eb;
  text-decoration: none;
  border-bottom: 1px dashed #2563eb;
  transition: all 0.2s ease;
}
.link-style:hover {
  color: #1d4ed8;
  border-bottom-color: #1d4ed8;
}

/* 城市接流样式 */
.city-item {
  padding: 8px 0;
}
.city-rank {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
  color: white;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}
.city-name {
  font-size: 13px;
  color: #334155;
  font-weight: 500;
}
.city-count {
  font-size: 13px;
  color: #2563eb;
  font-weight: 600;
  min-width: 40px;
  text-align: right;
  flex-shrink: 0;
}
.city-bar {
  width: 100%;
  height: 4px;
  background: #e2e8f0;
  border-radius: 2px;
  margin-top: 4px;
  overflow: hidden;
}
.city-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #2563eb, #3b82f6);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.fx-btn { font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #0F172A; background-color: #F8FAFC; padding: 0.75em 1.4em; border: 1px solid rgba(148,163,184,0.6); border-radius: 0.6rem; position: relative; cursor: pointer; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; }
.fx-btn span:not(.btn-text) { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); height: 26px; width: 26px; background-color: #2563EB; border-radius: 50%; transition: .6s ease; }
.fx-btn span:nth-child(1) { transform: translate(-3.3em, -4em); }
.fx-btn span:nth-child(2) { transform: translate(-6em, 1.3em); }
.fx-btn span:nth-child(3) { transform: translate(-.2em, 1.8em); }
.fx-btn span:nth-child(4) { transform: translate(3.5em, 1.4em); }
.fx-btn span:nth-child(5) { transform: translate(3.5em, -3.8em); }
.fx-btn:hover span:not(.btn-text) { transform: translate(-50%, -50%) scale(4); transition: 1.5s ease; }
.fx-btn .btn-text { position: relative; z-index: 1; }
.fx-purple { background-color: #E0F2FE; }
.fx-gray { background-color: #EFF6FF; }
.fx-sm { padding: 0.4em 0.9em; }
.fx-sm span:not(.btn-text) { height: 18px; width: 18px; }
</style>