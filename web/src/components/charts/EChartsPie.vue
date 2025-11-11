<template>
  <div style="position:relative;width:100%;height:280px">
    <div ref="el" style="position:absolute;inset:0;width:100%;height:100%;background:#fafafa;border:1px dashed #ddd;border-radius:8px"></div>
    <div v-if="noData" style="position:absolute;inset:0;display:flex;align-items:center;justify-content:center;color:#9CA3AF;font-size:14px;pointer-events:none">暂无数据</div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'

const props = defineProps({ data: { type: Array, default: () => [] }, colors: { type: Array, default: () => [] } })

const el = ref(null)
let chart = null
let echartsMod = null
let ro = null

const noData = computed(()=> {
  const arr = (props.data||[]).map(d=> Number(d?.value||0))
  const sum = arr.reduce((a,b)=> a+b, 0)
  return (arr.length === 0 || sum === 0)
})
const getColors = ()=> (props.colors && props.colors.length ? props.colors : ['#6E44FF','#8A7CFD','#7A5BFF','#9D8BFF','#B0A4FF','#C7BEFF','#D6D1FF','#E0DCFF','#EAE8FF'])
const buildOption = ()=> {
  const validData = (props.data||[])
    .filter(d => Number(d?.value||0) > 0)
    .map(d => ({ value: Number(d?.value||0), name: String(d?.label || d?.name || '') }))
  const total = validData.reduce((a,b)=> a + (b.value||0), 0)
  const noData = !validData.length || total === 0
  const valueMap = Object.fromEntries(validData.map(d => [d.name, d.value]))
  console.log('[EChartsPie] buildOption validData length:', validData.length, 'total:', total)
  return {
    tooltip: { trigger: 'item', formatter: ({ name, value, percent }) => `${name}：${value} (${percent}%)` },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      formatter: (name) => {
        const v = valueMap[name] ?? 0
        return `${name}  ${v}`
      }
    },
    color: getColors(),
    title: noData ? { text: '暂无数据', left: 'center', top: 'middle', textStyle: { color: '#9CA3AF', fontSize: 16 } } : undefined,
    series: [{
      name: '短链点击分布', type: 'pie', radius: ['40%','70%'], avoidLabelOverlap: true,
      itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
      label: { show: false, position: 'center' },
      emphasis: { label: { show: true, fontSize: 24, fontWeight: 'bold' } },
      labelLine: { show: false },
      data: validData
    }]
  }
}

onMounted(async () => {
  console.log('=== [EChartsPie] Mounted ===')
  console.log('[EChartsPie] props.data:', props.data)
  await nextTick()
  console.log('[EChartsPie] el.value:', el.value)
  if (!el.value) { console.warn('[EChartsPie] el.value is null'); return }
  const w = el.value?.clientWidth || 0, h = el.value?.clientHeight || 0
  console.log('[EChartsPie] mounted size:', w, h)
  try {
    const mod = await import('echarts')
    echartsMod = mod.default || mod
    console.log('[EChartsPie] ECharts loaded')
  } catch (err) {
    console.error('[EChartsPie] ECharts load error:', err)
    return
  }
  try {
    chart = echartsMod.init(el.value, null, { renderer: 'svg' })
    console.log('[EChartsPie] chart initialized')
  } catch (e) {
    console.error('[EChartsPie] chart init error:', e)
    return
  }
  const option = buildOption()
  console.log('[EChartsPie] init series length:', option?.series?.[0]?.data?.length)
  chart.setOption(option, true)
  chart.resize()
  setTimeout(() => { try { chart && chart.resize() } catch {} }, 120)
  ro = new ResizeObserver(() => { try { chart && chart.resize() } catch {} })
  ro.observe(el.value)
})

watch(()=> props.data, (val) => {
  console.log('[EChartsPie] data changed:', val)
  if (!chart) return
  const option = buildOption()
  chart.setOption(option, true)
  chart.resize()
}, { deep: true })

onUnmounted(()=> { try { ro && ro.disconnect() } catch {} ro = null; try { chart && chart.dispose() } catch {} chart = null })
</script>

<style scoped>
</style>