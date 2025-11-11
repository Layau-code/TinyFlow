<template>
  <div class="min-h-screen q-page font-[Inter,system-ui,-apple-system,sans-serif]">
    <div class="max-w-6xl mx-auto p-6">
      <!-- Header -->
      <div class="flex items-center justify-between mb-8">
        <h1 class="md-text text-[24px] font-medium">{{ $t('dashboard.title') }}</h1>
        <div class="flex items-center gap-4">
          <input v-model="query" type="text" :placeholder="$t('dashboard.searchPlaceholder')" class="h-9 px-3 rounded-lg border" style="border-color:#E5E7EB; background:#FFFFFF; color:#333333" />
          <button @click="refresh" class="md-btn-text">{{ $t('dashboard.refresh') }}</button>
        </div>
      </div>

      <!-- 今日与总点击分布饼图 / 近七天趋势切换 -->
      <div class="flex items-center justify-between mb-3">
        <div class="md-label">{{ $t('dashboard.overview') }}</div>
        <button class="md-btn-text" @click="toggleShowTrend7">{{ showTrend7 ? $t('dashboard.toggleShowPie') : $t('dashboard.toggleShowTrend') }}</button>
      </div>
      <div v-if="!showTrend7" class="grid grid-cols-1 lg:grid-cols-2 gap-8 mb-8">
        <div class="q-card p-6" style="box-shadow: 0 2px 12px rgba(0,0,0,0.04)">
          <div class="q-card-title mb-4">{{ $t('dashboard.todayDist') }}</div>
          <div class="relative" style="height:280px;background:#fafafa;border:1px dashed #ddd;border-radius:8px">
            <DistributionChart :type="'pie'" :data="pieTodayData" :colors="pieColors" :engine="'echarts'" />
          </div>
          <div v-if="!hasPieTodayData" class="q-muted text-center mt-2">{{ $t('common.noData') }}</div>
          <div class="q-help mt-1">数据条数：{{ pieTodayData.length }}，总和：{{ pieTodaySum }}</div>
          <div class="q-help mt-3">悬停查看名称、百分比与数值</div>
        </div>
        <div class="q-card p-6" style="box-shadow: 0 2px 12px rgba(0,0,0,0.04)">
          <div class="q-card-title mb-4">{{ $t('dashboard.totalDist') }}</div>
          <div class="relative" style="height:280px;background:#fafafa;border:1px dashed #ddd;border-radius:8px">
            <DistributionChart :type="'pie'" :data="pieTotalData" :colors="pieColors" :engine="'echarts'" />
          </div>
          <div v-if="!hasPieTotalData" class="q-muted text-center mt-2">{{ $t('common.noData') }}</div>
          <div class="q-help mt-1">数据条数：{{ pieTotalData.length }}，总和：{{ pieTotalSum }}</div>
          <div class="q-help mt-3">悬停查看名称、百分比与数值</div>
        </div>
      </div>
      <div v-else class="q-card p-6 mb-8" style="box-shadow: 0 2px 12px rgba(0,0,0,0.04)">
        <div class="q-card-title mb-4">{{ $t('dashboard.toggleShowTrend') }}（Top 5）</div>
        <div class="relative" style="height:280px;background:#fafafa;border:1px dashed #ddd;border-radius:8px">
          <Suspense>
            <TrendChart v-if="trendLabelsDash.length && compareTrends.length" :multi="true" :series="compareTrends" :labels="trendLabelsDash" />
            <template #fallback><div class="h-[240px] md-card"></div></template>
          </Suspense>
        </div>
        <div v-if="!trendLabelsDash.length || !compareTrends.length" class="q-muted text-center mt-2">{{ $t('dashboard.noDataOrNone') }}</div>
        <div class="q-help mt-3">{{ $t('dashboard.descTop5Trend') }}</div>
      </div>

      <!-- 合并后的统一表格（紫白配色） -->
      <div class="md-card p-5 mb-10">
        <div class="flex items-center justify-between mb-3">
          <div class="md-label">{{ $t('history.title') }}</div>
          <button class="md-btn" @click="exportCSV">{{ $t('dashboard.exportCsv') }}</button>
        </div>
        <div class="overflow-x-auto">
          <table class="md-table">
            <thead>
              <tr>
                <th class="md-th text-left">{{ $t('dashboard.table.shortCode') }}</th>
                <th class="md-th text-left">{{ $t('dashboard.table.url') }}</th>
                <th class="md-th text-right">{{ $t('dashboard.table.total') }}</th>
                <th class="md-th text-right">{{ $t('dashboard.table.today') }}</th>
                <th class="md-th text-left">{{ $t('dashboard.table.share') }}</th>
                <th class="md-th text-left">{{ $t('dashboard.table.createdAt') }}</th>
                <th class="md-th text-left">{{ $t('dashboard.table.actions') }}</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="row in pagedList" :key="row.shortCode" class="md-tr">
                <td class="md-td font-medium">{{ row.shortCode }}</td>
                <td class="md-td truncate max-w-[420px] md-muted">{{ row.longUrl }}</td>
                <td class="md-td text-right md-nums"><span style="color:#6E44FF">{{ row.totalVisits ?? '-' }}</span></td>
                <td class="md-td text-right md-nums">{{ row.todayVisits === 0 ? '-' : (row.todayVisits ?? '-') }}</td>
                <td class="md-td">
                  <div class="flex items-center gap-2">
                    <div class="w-[160px] h-2 rounded" style="background: var(--divider)">
                      <div class="h-2 rounded" :style="{ width: (percentMap[row.shortCode]||0).toFixed(2) + '%', background: '#8A7CFD' }"></div>
                    </div>
                    <span class="md-muted">{{ (percentMap[row.shortCode]||0).toFixed(2) }}%</span>
                  </div>
                </td>
                <td class="md-td">{{ formatDate(row.createdAt) }}</td>
                <td class="md-td">
                  <div class="flex items-center gap-3 md-actions">
                    <router-link :to="'/stats/' + encodeURIComponent(row.shortCode)" class="md-btn-text">{{ $t('stats.details') }}</router-link>
<button class="md-btn-text" @click="copy(SHORT_BASE + '/' + encodeURIComponent(row.shortCode))">{{ $t('actions.copy') }}</button>
                    <button class="md-btn-text" @click="remove(row.shortCode)">{{ $t('actions.delete') }}</button>
                  </div>
                </td>
              </tr>
              <tr v-if="loading"><td colspan="7" class="p-4 text-center md-muted">{{ $t('common.loading') }}</td></tr>
              <tr v-if="!loading && filteredList.length===0"><td colspan="7" class="p-4 text-center md-muted">{{ $t('common.noData') }}</td></tr>
            </tbody>
          </table>
        </div>
        <!-- Pagination -->
        <div class="flex items-center justify-between mt-4">
          <div class="md-muted">{{ $t('dashboard.pagination.total', { count: filteredList.length, page: page, pages: totalPages }) }}</div>
          <div class="flex items-center gap-3">
            <button @click="prev" class="md-btn-text">{{ $t('dashboard.pagination.prev') }}</button>
            <button @click="next" class="md-btn-text">{{ $t('dashboard.pagination.next') }}</button>
          </div>
        </div>
      </div>

      
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, defineAsyncComponent, watch } from 'vue'
import { useI18n } from 'vue-i18n'
import { useRouter } from 'vue-router'
import { useFetchList, useFetchClickStats, useFetchOverview, useFetchTrend, useCompareTrend } from '../composables/useStats'
import axios from 'axios'
import { SHORT_BASE } from '../composables/shortBase'
const DistributionChart = defineAsyncComponent(() => import('../components/DistributionChart.vue'))
const TrendLineChart = defineAsyncComponent(() => import('../components/charts/TrendLineChart.vue'))
const TrendChart = defineAsyncComponent(() => import('../components/TrendChart.vue'))
const router = useRouter()

const { t } = useI18n()

const query = ref('')
const page = ref(1)
const pageSize = 10

const { data: listRef, loading, error, refresh } = useFetchList()
// 新增：点击统计（用于饼图）来源于 /api/urls/click-stats
const { data: clickStatsRef, loading: clickLoading, error: clickError, refresh: refreshClickStats } = useFetchClickStats()
const list = listRef

// Pie data and colors
const pieColors = ['#6E44FF','#8A7CFD','#7A5BFF','#9D8BFF','#B0A4FF','#C7BEFF','#D6D1FF','#E0DCFF','#EAE8FF']
// 使用 click-stats 数据源生成饼图数据
const pieTotalData = computed(() => buildPieData(clickStatsRef.value || [], 'totalVisits'))
const pieTodayData = computed(() => buildPieData(clickStatsRef.value || [], 'todayVisits'))
// 计算是否有数据，避免模板中的复杂表达式导致误判
const hasPieTotalData = computed(() => (pieTotalData.value || []).reduce((a,b)=> a + Number(b.value||0), 0) > 0)
const hasPieTodayData = computed(() => (pieTodayData.value || []).reduce((a,b)=> a + Number(b.value||0), 0) > 0)
const pieTotalSum = computed(() => (pieTotalData.value || []).reduce((a,b)=> a + Number(b.value||0), 0))
const pieTodaySum = computed(() => (pieTodayData.value || []).reduce((a,b)=> a + Number(b.value||0), 0))
// 调试日志，便于定位数据来源与渲染问题
watch([pieTodayData, pieTotalData], ([t, all]) => {
  try {
    const sumToday = (t||[]).reduce((a,b)=> a + Number(b.value||0), 0)
    const sumAll = (all||[]).reduce((a,b)=> a + Number(b.value||0), 0)
    console.log('[Dashboard] Pie sums => today:', sumToday, ', total:', sumAll)
  } catch {}
})
watch(list, (val) => {
  try {
    console.log('[Dashboard] list length:', (val||[]).length, 'first item:', (val||[])[0])
  } catch {}
})
watch(clickStatsRef, (val) => {
  try {
    console.log('[Dashboard] click-stats length:', (val||[]).length, 'first item:', (val||[])[0])
  } catch {}
})
function buildPieData(arr, field){
  const items = (arr||[]).map(it => ({ name: it.shortCode, label: it.shortCode, value: Number(it[field]||0) }))
  const sorted = items.sort((a,b)=> b.value-a.value)
  const total = sorted.reduce((a,b)=> a+b.value, 0)
  if (sorted.length <= 8) return sorted
  const top = sorted.slice(0,7)
  const otherValue = sorted.slice(7).reduce((a,b)=> a+b.value, 0)
  if (otherValue > 0) top.push({ name: t('dashboard.other'), label: t('dashboard.other'), value: otherValue })
  return top
}

// 近七天趋势切换与数据（Top5 多折线，对齐日期）
const showTrend7 = ref(false)
const trendLabelsDash = ref([])
const compareTrends = ref([])
const { data: compareDataRef, refresh: refreshCompareApi } = useCompareTrend()
async function refreshDashboardTrend7(){
  try {
    const topKeys = [...(list.value||[])].sort((a,b)=> (b.totalVisits||0)-(a.totalVisits||0)).slice(0,5).map(it=> it.shortCode)
    if (!topKeys.length) { trendLabelsDash.value = []; compareTrends.value = []; return }
    await refreshCompareApi(topKeys, 7)
    const data = compareDataRef.value || {}
    // 统一日期标签（升序），并为每个短码补齐缺失日期的0值
    const labelSet = new Set()
    topKeys.forEach(k => (data[k]||[]).forEach(p => labelSet.add(String(p.date))))
    const labels = Array.from(labelSet).sort()
    const series = topKeys.map((k, idx) => {
      const map = new Map((data[k]||[]).map(p => [String(p.date), Number(p.visits||0)]))
      const s = labels.map(d => ({ label: d, value: map.get(d) ?? 0 }))
      return { name: k, color: ['#4F46E5','#06B6D4','#F59E0B','#10B981','#EF4444'][idx%5], series: s }
    })
    trendLabelsDash.value = labels
    compareTrends.value = series
  } catch (e) {
    console.log('[Dashboard] refreshDashboardTrend7 error:', e)
    trendLabelsDash.value = []
    compareTrends.value = []
  }
}
function toggleShowTrend7(){
  showTrend7.value = !showTrend7.value
  if (showTrend7.value && !trendLabelsDash.value.length) { refreshDashboardTrend7() }
}

// 移除概览解析函数（已改为 compare 接口）
// 合并表格：为每个短码计算在当前过滤列表中的占比（总访问次数比）
const percentMap = computed(() => {
  const arr = filteredList.value || []
  const sum = arr.reduce((a,b)=> a + Number(b.totalVisits||0), 0) || 1
  const map = {}
  arr.forEach(it => { map[it.shortCode] = (Number(it.totalVisits||0) / sum) * 100 })
  return map
})

// 将点击统计合并到列表项中，确保排序依据 totalVisits 有效
const statsMap = computed(() => {
  const map = {}
  ;(clickStatsRef.value || []).forEach(s => {
    map[s.shortCode] = {
      totalVisits: Number(s.totalVisits || 0),
      todayVisits: Number(s.todayVisits || 0)
    }
  })
  return map
})
const combinedList = computed(() => {
  return (list.value || []).map(it => {
    const stat = statsMap.value[it.shortCode] || {}
    return {
      ...it,
      totalVisits: stat.totalVisits ?? Number(it.totalVisits || 0),
      todayVisits: stat.todayVisits ?? Number(it.todayVisits || 0)
    }
  })
})

function exportCSV(){
  const header = [
    t('dashboard.table.shortCode'),
    t('dashboard.table.total'),
    t('dashboard.table.today'),
    t('dashboard.table.share'),
    t('dashboard.table.createdAt'),
    t('dashboard.table.url')
  ]
  const arr = filteredList.value || []
  const rows = arr.map(it => [
    it.shortCode,
    it.totalVisits ?? '-',
    it.todayVisits ?? '-',
    (percentMap.value[it.shortCode]||0).toFixed(2),
    formatDate(it.createdAt),
    it.longUrl || ''
  ])
  const csv = [header.join(','), ...rows.map(r=> r.join(','))].join('\n')
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${t('history.title')}.csv`
  a.click()
  URL.revokeObjectURL(url)
}

const filteredList = computed(() => {
  const q = query.value.trim().toLowerCase()
  const source = combinedList.value || []
  const base = q
    ? source.filter(it => (it.shortCode||'').toLowerCase().includes(q) || (it.longUrl||'').toLowerCase().includes(q))
    : [...source]
  // 按总点击次数降序排序，提升首页历史记录的可读性
  return base.sort((a,b) => Number(b.totalVisits||0) - Number(a.totalVisits||0))
})

const totalPages = computed(() => Math.max(1, Math.ceil(filteredList.value.length / pageSize)))
const pagedList = computed(() => filteredList.value.slice((page.value-1)*pageSize, page.value*pageSize))
function prev(){ page.value = Math.max(1, page.value-1) }
function next(){ page.value = Math.min(totalPages.value, page.value+1) }

// 已移除趋势与Top5对比相关逻辑

function formatDate(ts){ try { return new Date(ts).toLocaleString() } catch { return String(ts) } }

onMounted(async () => {
  await Promise.all([refresh(), refreshClickStats()])
})

function copy(text){
  try { navigator.clipboard.writeText(String(text)) } catch (e) { console.log('copy failed', e) }
}
async function remove(code){
  if (!code) return
  try {
    await axios.delete('/api/' + encodeURIComponent(code))
    await Promise.all([refresh(), refreshClickStats()])
  } catch (e) {
    console.log('remove failed', e)
    alert('删除失败，请稍后重试')
  }
}
</script>

<style scoped>
</style>
