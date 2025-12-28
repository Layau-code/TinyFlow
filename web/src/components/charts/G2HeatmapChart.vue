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
    default: 200
  }
})

const chartId = ref(`g2-heatmap-${Math.random().toString(36).substr(2, 9)}`)
let chart = null

function initChart() {
  if (chart) {
    chart.destroy()
  }

  if (!props.data || props.data.length === 0) {
    return
  }

  chart = new Chart({
    container: chartId.value,
    autoFit: true,
    height: props.height,
  })

  chart
    .cell()
    .data(props.data)
    .encode('x', 'hour')
    .encode('y', () => 1)
    .encode('color', 'value')
    .scale('color', {
      palette: 'blues',
      offset: (t) => t * 0.8 + 0.1
    })
    .style('inset', 2)
    .label({
      text: 'value',
      position: 'inside',
      fill: (d) => d.value > 50 ? '#fff' : '#334155',
      fontSize: 12,
      fontWeight: 600
    })
    .tooltip({
      title: (d) => `${d.hour}:00`,
      items: [(d) => ({ name: '访问量', value: d.value })]
    })
    .animate('enter', { type: 'fadeIn', duration: 600 })
    .interaction('elementHighlight', true)

  chart.axis('x', {
    title: false,
    label: {
      formatter: (v) => v + ':00'
    }
  })
  
  chart.axis('y', false)

  chart.render()
}

onMounted(() => {
  initChart()
})

watch(() => props.data, () => {
  initChart()
}, { deep: true })

onBeforeUnmount(() => {
  if (chart) {
    chart.destroy()
  }
})
</script>
