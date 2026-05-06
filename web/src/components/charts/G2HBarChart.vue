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
  xField: {
    type: String,
    default: 'value'
  },
  yField: {
    type: String,
    default: 'name'
  }
})

const chartId = ref(`g2-hbar-${Math.random().toString(36).substr(2, 9)}`)
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
    d[props.xField] !== undefined && 
    d[props.yField] !== undefined &&
    typeof d[props.xField] === 'number'
  )

  if (validData.length === 0) {
    console.warn('No valid data for bar chart')
    return
  }

  chart = new Chart({
    container: chartId.value,
    autoFit: true,
    height: props.height,
  })

  chart.coordinate({ transform: [{ type: 'transpose' }] })

  chart
    .interval()
    .data(validData)
    .encode('x', props.yField)
    .encode('y', props.xField)
    .encode('color', (d, idx) => {
      // TOP 3 使用深蓝色，其他使用浅蓝色
      return idx < 3 ? '#3b82f6' : '#93c5fd'
    })
    .axis('x', {
      label: {
        autoRotate: false,
        formatter: (text) => {
          return text.length > 15 ? text.substring(0, 15) + '...' : text
        }
      }
    })
    .axis('y', {
      labelFormatter: (val) => {
        if (val >= 1000) return (val / 1000).toFixed(1) + 'k'
        return val
      }
    })
    .label({
      text: props.xField,
      position: 'right',
      fontSize: 11,
      fill: '#64748b',
      dx: 5
    })
    .tooltip({
      title: (d) => d[props.yField],
      items: [(d) => ({ name: '访问量', value: d[props.xField] })]
    })
    .animate('enter', { type: 'scaleInX', duration: 600 })
    .state('active', { stroke: '#1e40af', lineWidth: 2 })
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
