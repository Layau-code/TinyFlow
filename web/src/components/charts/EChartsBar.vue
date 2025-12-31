<template>
  <div class="echarts-bar" ref="root" :style="{ height: height + 'px', width: '100%' }"></div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, defineExpose } from 'vue'
import { loadECharts } from '../../utils/echarts-loader'

const props = defineProps({ labels: { type: Array, default: () => [] }, values: { type: Array, default: () => [] }, height: { type: Number, default: 320 }, color: { type: String, default: '#2563eb' } })
const emit = defineEmits(['point-click', 'ready', 'error'])

const root = ref(null)
let chart = null
let echarts = null

async function init() {
  try {
    const mod = await loadECharts()
    echarts = mod.default || mod
    if (!root.value) return
    chart = echarts.init(root.value)
    render()
    window.addEventListener('resize', resize)
    chart.on('click', params => {
      if (params && params.dataIndex != null) {
        emit('point-click', { index: params.dataIndex, label: props.labels[params.dataIndex], value: params.value, raw: params })
      }
    })
    emit('ready', chart)
  } catch (e) { console.error('EChartsBar init error', e); emit('error', e) }
}

function resize(){ try { chart && chart.resize() } catch {} }

function render(){
  if (!chart) return
  const option = {
    tooltip: { trigger: 'axis' },
    toolbox: { feature: { saveAsImage: { title: '保存为图片' } } },
    xAxis: { type: 'category', data: props.labels || [], axisLabel: { rotate: 0, interval: 0 } },
    yAxis: { type: 'value' },
    grid: { left: '6%', right: '6%', top: '12%', bottom: '10%' },
    series: [{ type: 'bar', data: props.values || [], itemStyle: { color: props.color }, barMaxWidth: 36 }]
  }
  chart.setOption(option, { notMerge: false })
}

watch([() => props.labels, () => props.values], () => { render() }, { deep: true })

onMounted(() => init())
onBeforeUnmount(() => { try { chart && chart.dispose(); window.removeEventListener('resize', resize) } catch {} })

function getDataURL(opts = { type: 'png', pixelRatio: 2, backgroundColor: '#ffffff' }){ try { if (!chart) return null; return chart.getDataURL(opts) } catch (e) { console.error(e); return null } }

defineExpose({ getDataURL, resize })
</script>

<style scoped>
.echarts-bar { width: 100%; }
</style>

