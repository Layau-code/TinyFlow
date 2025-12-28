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
    default: 280
  },
  xField: {
    type: String,
    default: 'date'
  },
  yField: {
    type: String,
    default: 'value'
  },
  color: {
    type: String,
    default: '#3b82f6'
  },
  smooth: {
    type: Boolean,
    default: true
  }
})

const chartId = ref(`g2-line-${Math.random().toString(36).substr(2, 9)}`)
const chartRef = ref(null)
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
    d[props.yField] !== undefined
  )

  if (validData.length === 0) {
    console.warn('No valid data for chart')
    return
  }

  chart = new Chart({
    container: chartId.value,
    autoFit: true,
    height: props.height,
  })

  // 折线
  chart
    .line()
    .data(validData)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('shape', props.smooth ? 'smooth' : 'line')
    .style('stroke', props.color)
    .style('lineWidth', 3)
    .tooltip({
      title: (d) => d[props.xField],
      items: [(d) => ({ name: '访问量', value: d[props.yField] })]
    })
    .animate('enter', { type: 'pathIn', duration: 1000 })

  // 面积填充
  chart
    .area()
    .data(validData)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('shape', props.smooth ? 'smooth' : 'area')
    .style('fill', props.color)
    .style('fillOpacity', 0.2)
    .tooltip(false)
    .animate('enter', { type: 'fadeIn', duration: 1000 })

  // 数据点 - 增大尺寸，更明显
  chart
    .point()
    .data(validData)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('size', 6)
    .encode('shape', 'point')
    .style('fill', props.color)
    .style('stroke', '#fff')
    .style('lineWidth', 3)
    .tooltip(false)
    .state('active', { r: 8 })
    .interaction('elementHighlight', true)
    .animate('enter', { type: 'scaleInY', duration: 800, delay: 500 })

  // 坐标轴配置
  chart.axis('y', {
    labelFormatter: (val) => {
      if (val >= 1000) return (val / 1000).toFixed(1) + 'k'
      return val
    }
  })

  chart.render()
}

onMounted(() => {
  // 延迟初始化，确保 DOM 已挂载
  setTimeout(() => {
    initChart()
  }, 100)
})

watch(() => props.data, () => {
  // 延迟更新，避免频繁重渲染
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
/* G2 折线图容器样式 */
</style>
