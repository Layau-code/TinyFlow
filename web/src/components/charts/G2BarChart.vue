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
    default: 400
  },
  xField: {
    type: String,
    default: 'name'
  },
  yField: {
    type: String,
    default: 'value'
  },
  color: {
    type: String,
    default: '#3b82f6'
  }
})

const chartId = ref(`g2-bar-${Math.random().toString(36).substr(2, 9)}`)
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
    .interval()
    .data(props.data)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('color', () => props.color)
    .axis('y', {
      labelFormatter: (val) => {
        if (val >= 1000) return (val / 1000).toFixed(1) + 'k'
        return val
      }
    })
    .tooltip({
      title: (d) => d[props.xField],
      items: [(d) => ({ name: '点击量', value: d[props.yField] })]
    })
    .animate('enter', { type: 'scaleInY', duration: 600 })
    .state('active', { fill: '#2563eb' })
    .interaction('elementHighlight', true)

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

<style scoped>
/* G2 图表容器样式 */
</style>
