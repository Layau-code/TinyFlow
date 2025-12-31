<template>
  <div class="trend-echarts" ref="el" :style="{ height: height + 'px' }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, defineExpose } from 'vue'
import { loadECharts } from '../../utils/echarts-loader'
const props = defineProps({
  labels: { type: Array, default: () => [] },
  values: { type: Array, default: () => [] },
  height: { type: Number, default: 320 },
  theme: { type: Object, default: () => ({ color: ['#2563eb', '#60a5fa'] }) },
  showArea: { type: Boolean, default: true },
  enableZoom: { type: Boolean, default: true },
})
const emit = defineEmits(['point-click', 'zoom', 'brush', 'ready', 'error'])

const el = ref(null)
let chart = null
let echarts = null

async function init() {
  try {
    const mod = await loadECharts()
    echarts = mod.default || mod
    if (!el.value) return
    chart = echarts.init(el.value)
    render()
    window.addEventListener('resize', resize)

    // 交互事件
    chart.on('click', params => {
      if (params && params.dataIndex != null) {
        emit('point-click', { index: params.dataIndex, label: props.labels[params.dataIndex], value: params.value, raw: params })
      }
    })

    chart.on('dataZoom', (params) => { emit('zoom', params) })
    // brush 需要额外注册组件：后续按需加载 brush component
    chart.on('brushSelected', (params) => { emit('brush', params) })

    emit('ready', chart)
  } catch (e) {
    console.error('echarts init error', e)
    emit('error', e)
  }
}

function resize(){ try { chart && chart.resize() } catch {} }

function render(){
  if (!chart) return
  const option = {
    tooltip: { trigger: 'axis' },
    toolbox: { feature: { saveAsImage: { title: '保存为图片' } } },
    xAxis: { type: 'category', data: props.labels || [], boundaryGap: false },
    yAxis: { type: 'value' },
    grid: { left: 10, right: 10, top: 20, bottom: 40 },
    color: (props.theme && props.theme.color) || ['#2563eb'],
    series: [{ data: props.values || [], type: 'line', smooth: true, areaStyle: props.showArea ? {} : undefined, animation: true }],
  }
  if (props.enableZoom) {
    option.dataZoom = [{ type: 'slider', start: 0, end: 100 }, { type: 'inside' }]
  }
  chart.setOption(option, { notMerge: false })
}

watch([() => props.labels, () => props.values, () => props.theme], () => { render() }, { deep: true })

onMounted(() => init())
onBeforeUnmount(() => { try { chart && chart.dispose(); window.removeEventListener('resize', resize) } catch {} })

// 方法
function getDataURL(options = { type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' }){
  try { if (!chart) return null; return chart.getDataURL(options) } catch (e) { console.error('getDataURL error', e); return null }
}

function appendData({ labels = [], values = [] } = {}){
  try {
    if (!chart) return
    // 简单替换数据：外部可调用 fetchTrend 后 set props
    // 这里触发 setOption 更新
    const newLabels = (props.labels || []).concat(labels)
    const newValues = (props.values || []).concat(values)
    chart.setOption({ xAxis: { data: newLabels }, series: [{ data: newValues }] })
  } catch (e) { console.error('appendData error', e) }
}

function updateOptions(opt = {}){ try { if (!chart) return; chart.setOption(opt) } catch (e) { console.error(e) } }

defineExpose({ getDataURL, appendData, resize, updateOptions })
</script>

<style scoped>
.trend-echarts { width: 100%; }
</style>
