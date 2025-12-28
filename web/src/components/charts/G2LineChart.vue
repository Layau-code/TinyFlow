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

  // 折线
  chart
    .line()
    .data(props.data)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('shape', props.smooth ? 'smooth' : 'line')
    .style('stroke', props.color)
    .style('lineWidth', 2)
    .tooltip({
      title: (d) => d[props.xField],
      items: [(d) => ({ name: '访问量', value: d[props.yField] })]
    })
    .animate('enter', { type: 'pathIn', duration: 800 })

  // 面积填充
  chart
    .area()
    .data(props.data)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('shape', props.smooth ? 'smooth' : 'area')
    .style('fill', props.color)
    .style('fillOpacity', 0.15)
    .tooltip(false)
    .animate('enter', { type: 'fadeIn', duration: 800 })

  // 数据点
  chart
    .point()
    .data(props.data)
    .encode('x', props.xField)
    .encode('y', props.yField)
    .encode('size', 4)
    .encode('shape', 'point')
    .style('fill', '#fff')
    .style('stroke', props.color)
    .style('lineWidth', 2)
    .tooltip(false)
    .state('active', { fill: props.color })
    .interaction('elementHighlight', true)

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
/* G2 折线图容器样式 */
</style>
