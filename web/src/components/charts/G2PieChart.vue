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

  chart = new Chart({
    container: chartId.value,
    autoFit: true,
    height: props.height,
  })

  chart.coordinate({ type: 'theta', outerRadius: 0.8, innerRadius: 0.5 })

  chart
    .interval()
    .data(props.data)
    .transform({ type: 'stackY' })
    .encode('y', props.valueField)
    .encode('color', props.labelField)
    .scale('color', { range: props.colors })
    .legend('color', {
      position: 'right',
      layout: { justifyContent: 'center' }
    })
    .label({
      text: (d) => {
        const total = props.data.reduce((sum, item) => sum + item[props.valueField], 0)
        const percent = ((d[props.valueField] / total) * 100).toFixed(1)
        return `${d[props.labelField]}\n${percent}%`
      },
      position: 'outside',
      fontSize: 12,
      fill: '#64748b'
    })
    .tooltip({
      items: [(d) => ({ 
        name: d[props.labelField], 
        value: d[props.valueField] 
      })]
    })
    .animate('enter', { type: 'waveIn', duration: 800 })
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
/* G2 饼图容器样式 */
</style>
