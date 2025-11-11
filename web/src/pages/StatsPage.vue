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
          <button @click="goHome" class="q-btn-ghost px-3 py-1 flex items-center gap-1">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M3 10.5L12 3l9 7.5V20a1 1 0 0 1-1 1h-5v-6H9v6H4a1 1 0 0 1-1-1v-9.5Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
            </svg>
            <span>{{ $t('stats.backHome') }}</span>
          </button>
          <button @click="goDashboard" class="q-btn-ghost px-3 py-1 flex items-center gap-1">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M4 4h7v7H4V4Zm9 0h7v5h-7V4ZM4 13h5v7H4v-7Zm9 7v-9h7v9h-7Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
            </svg>
            <span>{{ $t('stats.backDashboard') }}</span>
          </button>
          <a :href="shortUrl" @click.prevent="copy(shortUrl)" class="q-btn-ghost px-3 py-1 flex items-center gap-1">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M9 7a2 2 0 0 1 2-2h7a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2h-7a2 2 0 0 1-2-2V7Zm-5 4a2 2 0 0 1 2-2h1v9a3 3 0 0 0 3 3h7v1a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2v-9Z" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
            </svg>
            <span>{{ $t('stats.copyShort') }}</span>
          </a>
        </div>
      </div>

      <!-- 近七天访问趋势（折线图） -->
      <div class="grid grid-cols-1 gap-8">
        <div class="q-card p-5">
          <div class="q-card-title mb-3 flex items-center gap-2">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M4 17l6-6 4 4 6-7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>{{ $t('dashboard.toggleShowTrend') }}</span>
          </div>
          <div class="relative" style="height:260px;background:#fafafa;border:1px dashed #ddd;border-radius:8px">
            <Suspense>
              <TrendChart v-if="trendLabels.length" :values="trendValues" :labels="trendLabels" :showValues="true" />
              <template #fallback><div class="h-[220px] q-card"></div></template>
            </Suspense>
          </div>
          <div v-if="!trendLabels.length && !loadingTrend" class="q-muted text-center mt-2">{{ $t('common.noData') }}</div>
        </div>
      </div>

      <!-- 单饼图：来源渠道占比 -->
      <div class="grid grid-cols-1 gap-8">
        <div class="q-card p-5">
          <div class="q-card-title mb-3 flex items-center gap-2">
            <svg class="w-4 h-4" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg" aria-hidden="true">
              <path d="M12 12m-9 0a9 9 0 1 0 18 0a9 9 0 1 0-18 0" stroke="currentColor" stroke-width="1.5" />
              <path d="M12 3v9l8 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round" />
            </svg>
            <span>{{ $t('stats.referrerShare') }}</span>
          </div>
          <Suspense>
            <DistributionChart :type="'pie'" :data="refererData" :engine="'echarts'" />
            <template #fallback><div class="h-[240px] q-card"></div></template>
          </Suspense>
          <div v-if="!refererData.length && !loadingDist" class="q-muted">{{ $t('common.noData') }}</div>
        </div>
      </div>

      <!-- 详细统计数据表单（白+蓝紫科技风，舒适排版） -->
      <div class="q-card p-6">
        <div class="q-card-title mb-4">{{ $t('stats.details') }}</div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label class="q-muted text-[13px]">{{ $t('stats.labels.shortCode') }}</label>
            <input class="q-input mt-1 w-full" :value="shortCode" readonly disabled />
          </div>
          <div>
            <label class="q-muted text-[13px]">{{ $t('stats.labels.shortUrl') }}</label>
            <input class="q-input mt-1 w-full" :value="shortUrl" readonly disabled />
          </div>
          <div>
            <label class="q-muted text-[13px]">{{ $t('stats.labels.totalVisits') }}</label>
            <input class="q-input mt-1 w-full" :value="overview?.totalVisits ?? '-'" readonly disabled />
          </div>
          <div>
            <label class="q-muted text-[13px]">{{ $t('stats.labels.todayVisits') }}</label>
            <input class="q-input mt-1 w-full" :value="overview?.todayVisits ?? '-'" readonly disabled />
          </div>
          <div>
            <label class="q-muted text-[13px]">{{ $t('stats.labels.createdAt') }}</label>
            <input class="q-input mt-1 w-full" :value="formatDate(overview?.createdAt)" readonly disabled />
          </div>
          <div>
            <label class="q-muted text-[13px]">{{ $t('stats.labels.topCities') }}</label>
            <div class="mt-2 flex flex-wrap gap-2">
              <span v-for="c in cityData" :key="c.label" class="q-chip">{{ c.label }}（{{ c.value }}）</span>
              <span v-if="!cityData.length" class="q-muted">{{ $t('common.noData') }}</span>
            </div>
          </div>
          <div class="md:col-span-2">
            <label class="q-muted text-[13px]">{{ $t('stats.labels.devices') }}</label>
            <div class="mt-2 flex flex-wrap gap-2">
              <span v-for="d in deviceData" :key="d.label" class="q-chip">{{ d.label }}（{{ d.value }}）</span>
              <span v-if="!deviceData.length" class="q-muted">{{ $t('common.noData') }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Error -->
      <div v-if="errorMsg" class="p-4 rounded q-card">
        {{ errorMsg }}
        <button class="ml-3 q-btn-ghost px-3 py-1" @click="refreshAll">{{ $t('stats.retry') }}</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, defineAsyncComponent } from 'vue'
  import { useI18n } from 'vue-i18n'
  import { useFetchOverview, useFetchDistribution, useFetchTrend } from '../composables/useStats'
  import { SHORT_BASE } from '../composables/shortBase'
const DistributionChart = defineAsyncComponent(() => import('../components/DistributionChart.vue'))
const TrendChart = defineAsyncComponent(() => import('../components/TrendChart.vue'))

import { useRoute, useRouter } from 'vue-router'
import { useClipboard } from '../composables/useStats'
const route = useRoute()
const router = useRouter()
const shortCode = route.params.shortCode
const shortUrl = `${SHORT_BASE}/${encodeURIComponent(shortCode)}`
const { copy } = useClipboard()
const { t } = useI18n()

const { data: overviewRef, loading: loadingOverview, error: errorOverview, refresh: refreshOverview } = useFetchOverview(shortCode)
const { data: distRef, loading: loadingDistRef, error: errorDist, refresh: refreshDist } = useFetchDistribution(shortCode)
const { data: trendRef, loading: loadingTrendRef, error: errorTrend, refresh: refreshTrend } = useFetchTrend(shortCode, 7)

const overview = overviewRef
const deviceData = ref([])
const cityData = ref([])
const refererData = ref([])
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
  deviceData.value = (dist.device||[]).map(kv => ({ label: kv.label || kv.name || kv.key || kv.device || t('common.unknown'), value: kv.value || kv.count || 0 }))
  cityData.value = (dist.city||[]).slice(0,5).map(kv => ({ label: kv.city || kv.label || kv.name || t('common.unknown'), value: kv.count || kv.value || 0 }))
  refererData.value = (dist.referer||[]).map(kv => ({ label: kv.referer || kv.label || kv.name || t('common.unknown'), value: kv.count || kv.value || 0 }))

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

onMounted(refreshAll)
</script>

<style scoped>
svg { width: 20px; height: 20px; }
</style>