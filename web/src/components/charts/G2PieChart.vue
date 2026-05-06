<template>
  <div :id="chartId" :style="{ width: '100%', height: height + 'px' }"></div>
</template>

<script setup>
import { onMounted, watch, onBeforeUnmount, ref } from 'vue'
import { Chart } from '@antv/g2'

const props = defineProps({
  data: {
    type: Array,
    default: () => []
  },
  height: {
    type: Number,
    default: 300
  },
  valueField: {
    type: String,
    default: 'value'
  },
  labelField: {
    type: String,
    default: 'label'
  },
  colors: {
    type: Array,
    default: () => ['#3b82f6', '#60a5fa', '#93c5fd', '#bfdbfe']
  }
})

const chartId = ref(`g2-pie-${Math.random().toString(36).substr(2, 9)}`)
let chart = null

function initChart() {
  if (chart) {
    chart.destroy()
  }

  if (!props.data || props.data.length === 0) {
    return
  }

  // 确保 DOM 元素存在
  const container = document.getElementById(chartId.value)
  if (!container) {
    console.warn('Chart container not found:', chartId.value)
    return
  }

  // 数据验证
  const validData = props.data.filter(d => 
    d && 
    typeof d === 'object' && 
    d[props.valueField] !== undefined && 
    d[props.labelField] !== undefined &&
    typeof d[props.valueField] === 'number'
  )

  if (validData.length === 0) {
    console.warn('No valid data for pie chart')
    return
  }

  chart = new Chart({
    container: chartId.value,
    autoFit: true,
    height: props.height,
  })

  chart.coordinate({ type: 'theta', outerRadius: 0.75, innerRadius: 0.45 })

  chart
    .interval()
    .data(validData)
    .transform({ type: 'stackY' })
    .encode('y', props.valueField)
    .encode('color', props.labelField)
    .scale('color', { range: props.colors })
    .legend('color', {
      position: 'right',
      layout: { justifyContent: 'center' },
      itemMarker: {
        symbol: 'circle',
        style: { r: 5 }
      }
    })
    .label({
      text: (d) => {
        const total = validData.reduce((sum, item) => sum + item[props.valueField], 0)
        const percent = ((d[props.valueField] / total) * 100).toFixed(1)
        return `${percent}%`
      },
      position: 'outside',
      fontSize: 14,
      fontWeight: 600,
      fill: '#334155',
      connectorDistance: 8,
      connectorStroke: '#cbd5e1',
      connectorStrokeWidth: 1
    })
    .tooltip({
      items: [(d) => ({ 
        name: d[props.labelField], 
        value: d[props.valueField] + ' (' + ((d[props.valueField] / validData.reduce((sum, item) => sum + item[props.valueField], 0)) * 100).toFixed(1) + '%)'
      })]
    })
    .style('stroke', '#fff')
    .style('lineWidth', 3)
    .animate('enter', { type: 'waveIn', duration: 1000 })
    .state('active', { 
      style: { 
        lineWidth: 4,
        stroke: '#2563eb'
      } 
    })
    .interaction('elementHighlight', true)

  chart.render()
}

onMounted(() => {
  setTimeout(() => {
    initChart()
  }, 100)
})

watch(() => props.data, () => {
  setTimeout(() => {
    initChart()
  }, 50)
}, { deep: true })

onBeforeUnmount(() => {
  if (chart) {
    chart.destroy()
  }
})
</script>

<style scoped>
/* G2 饼图容器样式 */
</style>
