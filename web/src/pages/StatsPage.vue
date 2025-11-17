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

      <!-- 详情模块（精简版） -->
      <div class="q-card p-6">
        <div class="grid grid-cols-1 md:grid-cols-3 gap-6 items-start">
          <div>
            <div class="q-muted text-[13px]">短码</div>
            <div class="mt-1 q-strong">{{ shortCode }}</div>
          </div>
          <div>
            <div class="q-muted text-[13px]">短链</div>
            <a :href="shortUrl" target="_blank" class="mt-1 q-link">{{ shortUrl }}</a>
          </div>
          <div>
            <div class="q-muted text-[13px]">创建时间</div>
            <div class="mt-1 q-strong">{{ formatDate(overview?.createdAt) }}</div>
          </div>
          <div>
            <div class="q-muted text-[13px]">总访问量</div>
            <div class="mt-1 q-nums">{{ overview?.totalVisits ?? '-' }}</div>
          </div>
          <div>
            <div class="q-muted text-[13px]">今日访问量</div>
            <div class="mt-1 q-nums">{{ overview?.todayVisits ?? '-' }}</div>
          </div>
          <div>
            <div class="q-muted text-[13px]">Top 城市</div>
            <div class="mt-2 space-y-1">
              <div v-for="c in cityData" :key="c.label" class="flex items-center justify-between">
                <span class="q-muted">{{ c.label }}</span>
                <span class="q-nums">{{ c.value }}</span>
              </div>
              <div v-if="!cityData.length" class="q-muted">{{ $t('common.noData') }}</div>
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
.tf-modal { width: 680px; max-width: 92vw; border-radius: 16px; padding: 24px; background: linear-gradient(135deg,#ffffff 0%, #f7f9ff 100%); box-shadow: 0 12px 40px rgba(43,108,239,0.25); border: 1px solid rgba(138,107,255,0.25); }
.tf-modal-header { font-size: 16px; font-weight: 600; color: #2B6CEF; margin-bottom: 16px; }

.fx-btn { font-family: Arial, Helvetica, sans-serif; font-weight: bold; color: #2b2b2b; background-color: #f5f6fa; padding: 0.75em 1.4em; border: 1px solid rgba(138,107,255,0.2); border-radius: 0.6rem; position: relative; cursor: pointer; overflow: hidden; display: inline-flex; align-items: center; justify-content: center; }
.fx-btn span:not(.btn-text) { position: absolute; left: 50%; top: 50%; transform: translate(-50%, -50%); height: 26px; width: 26px; background-color: #8A6BFF; border-radius: 50%; transition: .6s ease; }
.fx-btn span:nth-child(1) { transform: translate(-3.3em, -4em); }
.fx-btn span:nth-child(2) { transform: translate(-6em, 1.3em); }
.fx-btn span:nth-child(3) { transform: translate(-.2em, 1.8em); }
.fx-btn span:nth-child(4) { transform: translate(3.5em, 1.4em); }
.fx-btn span:nth-child(5) { transform: translate(3.5em, -3.8em); }
.fx-btn:hover span:not(.btn-text) { transform: translate(-50%, -50%) scale(4); transition: 1.5s ease; }
.fx-btn .btn-text { position: relative; z-index: 1; }
.fx-purple { background-color: #f0f1f7; }
.fx-gray { background-color: #f7f8fc; }
.fx-sm { padding: 0.4em 0.9em; }
.fx-sm span:not(.btn-text) { height: 18px; width: 18px; }
</style>
function selectDays(d){ selectedDays.value = d; refreshTrend() }
watch(selectedDays, async () => {
  await refreshTrend()
  await refreshAll()
})